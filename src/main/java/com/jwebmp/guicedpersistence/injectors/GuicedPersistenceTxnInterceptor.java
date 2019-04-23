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

import com.google.inject.Key;
import com.google.inject.persist.UnitOfWork;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedpersistence.db.annotations.Transactional;
import com.jwebmp.guicedpersistence.services.ITransactionHandler;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.PersistenceUnit;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jwebmp.guicedpersistence.scanners.PersistenceServiceLoadersBinder.*;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */

public class GuicedPersistenceTxnInterceptor
		implements MethodInterceptor
{
	/**
	 * Tracks if the unit of work was begun implicitly by this transaction.
	 */
	private final ThreadLocal<Boolean> didWeStartWork = new ThreadLocal<>();
	private static final Logger log = LogFactory.getLog("GuicedPersistenceTxnIntercepter");

	public GuicedPersistenceTxnInterceptor()
	{
		//No config required
	}

	@Override
	@SuppressWarnings("Duplicates")
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		Transactional transactional = readTransactionMetadata(methodInvocation);

		CustomJpaPersistService emProvider = GuiceContext.get(Key.get(CustomJpaPersistService.class, transactional.entityManagerAnnotation()));
		UnitOfWork unitOfWork = GuiceContext.get(Key.get(UnitOfWork.class, transactional.entityManagerAnnotation()));
		PersistenceUnit unit = GuiceContext.get(Key.get(PersistenceUnit.class, transactional.entityManagerAnnotation()));

		if (!emProvider.isWorking())
		{
			emProvider.begin();
			didWeStartWork.set(true);
		}
		Boolean startedWork = didWeStartWork.get() == null ? false : didWeStartWork.get();
		EntityManager em = emProvider.get();

		boolean transactionAlreadyStarted = false;
		for (ITransactionHandler handler : GuiceContext.get(ITransactionHandlerReader))
		{
			if (handler.active(unit) && handler.transactionExists(em, unit))
			{
				transactionAlreadyStarted = true;
				break;
			}
		}

		if (!startedWork && transactionAlreadyStarted)
		{
			return methodInvocation.proceed();
		}

		for (ITransactionHandler handler : GuiceContext.get(ITransactionHandlerReader))
		{
			if (handler.active(unit))
			{
				handler.setTransactionTimeout(transactional.timeout(),em,unit);
				handler.beginTransacation(false, em, unit);
			}
		}

		Object result;
		try
		{
			result = methodInvocation.proceed();
		}
		catch (Exception e)
		{
			if (rollbackIfNecessary(transactional, e, unit, em))
			{
				for (ITransactionHandler handler : GuiceContext.get(ITransactionHandlerReader))
				{
					if (handler.active(unit))
					{
						handler.commitTransacation(false, em, unit);
					}
				}
			}

			if (startedWork)
			{
				didWeStartWork.remove();
				unitOfWork.end();
			}
			log.log(Level.SEVERE, "Unable to commit : ", e);
			throw e;
		}
		try
		{
			for (ITransactionHandler handler : GuiceContext.get(ITransactionHandlerReader))
			{
				if (handler.active(unit))
				{
					handler.commitTransacation(false, em, unit);
				}
			}
		}
		finally
		{
			if (startedWork)
			{
				em.clear();
				em.close();
				didWeStartWork.remove();
				unitOfWork.end();
			}
		}
		return result;
	}

	/**
	 * Method readTransactionMetadata ...
	 *
	 * @param methodInvocation
	 * 		of type MethodInvocation
	 *
	 * @return Transactional
	 */
	@SuppressWarnings("Duplicates")
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
		return transactional;
	}

	/**
	 * Returns True if rollback DID NOT HAPPEN (i.e. if commit should continue).
	 *
	 * @param transactional
	 * 		The metadata annotation of the method
	 * @param e
	 * 		The exception to test for rollback
	 * @param em
	 * 		Entity Manager
	 * @param unit
	 * 		The associated persistence unit
	 */
	@SuppressWarnings("Duplicates")
	private boolean rollbackIfNecessary(Transactional transactional, Exception e, PersistenceUnit unit, EntityManager em)
	{
		boolean commit = true;

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
					for (ITransactionHandler handler : GuiceContext.get(ITransactionHandlerReader))
					{
						if (handler.active(unit))
						{
							handler.rollbackTransacation(false, em, unit);
						}
					}
				}
				break;
			}
		}

		return commit;
	}
}
