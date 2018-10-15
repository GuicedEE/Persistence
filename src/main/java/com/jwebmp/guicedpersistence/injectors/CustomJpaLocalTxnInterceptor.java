/*
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jwebmp.guicedpersistence.injectors;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.reflect.Method;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class CustomJpaLocalTxnInterceptor
		implements MethodInterceptor
{
	private final ThreadLocal<Boolean> didWeStartWork = new ThreadLocal<>();
	@Inject
	private CustomJpaPersistService emProvider = null;
	@Inject
	private UnitOfWork unitOfWork = null;

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		if (!emProvider.isWorking())
		{
			emProvider.begin();
			didWeStartWork.set(true);
		}

		Transactional transactional = readTransactionMetadata(methodInvocation);
		EntityManager em = emProvider.get();

		if (em.getTransaction()
		      .isActive())
		{
			return methodInvocation.proceed();
		}

		EntityTransaction txn = em.getTransaction();
		txn.begin();

		Object result;
		try
		{
			result = methodInvocation.proceed();

		}
		catch (Exception e)
		{
			if (rollbackIfNecessary(transactional, e, txn))
			{
				txn.commit();
			}
			throw e;
		}
		finally
		{
			if (null != didWeStartWork.get() && !txn.isActive())
			{
				didWeStartWork.remove();
				unitOfWork.end();
			}
		}
		try
		{
			txn.commit();
		}
		finally
		{
			if (null != didWeStartWork.get())
			{
				didWeStartWork.remove();
				unitOfWork.end();
			}
		}
		return result;
	}

	private Transactional readTransactionMetadata(MethodInvocation methodInvocation)
	{
		Transactional transactional;
		Method method = methodInvocation.getMethod();
		Class<?> targetClass = methodInvocation.getThis()
		                                       .getClass();

		transactional = method.getAnnotation(Transactional.class);
		if (null == transactional)
		{
			transactional = targetClass.getAnnotation(Transactional.class);
		}
		if (null == transactional)
		{
			transactional = Internal.class.getAnnotation(Transactional.class);
		}

		return transactional;
	}

	/**
	 * Returns True if rollback DID NOT HAPPEN (i.e. if commit should continue).
	 *
	 * @param transactional
	 * 		The metadata annotaiton of the method
	 * @param e
	 * 		The exception to test for rollback
	 * @param txn
	 * 		A JPA Transaction to issue rollbacks on
	 */
	private boolean rollbackIfNecessary(
			Transactional transactional, Exception e, EntityTransaction txn)
	{
		boolean commit = true;

		//check rollback clauses
		for (Class<? extends Exception> rollBackOn : transactional.rollbackOn())
		{
			if (rollBackOn.isInstance(e))
			{
				commit = false;
				for (Class<? extends Exception> exceptOn : transactional.ignore())
				{
					if (exceptOn.isInstance(e))
					{
						commit = true;
						break;
					}
				}
				if (!commit)
				{
					txn.rollback();
				}
				break;
			}
		}

		return commit;
	}

	@Transactional
	private static class Internal {}
}