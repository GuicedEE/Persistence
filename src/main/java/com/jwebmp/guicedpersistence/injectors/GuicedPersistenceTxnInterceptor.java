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
import com.oracle.jaxb21.PersistenceUnit;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private final ThreadLocal<Map<Class<? extends Annotation>, Boolean>> didWeStartWork = new ThreadLocal<>();

	public GuicedPersistenceTxnInterceptor()
	{
		//No config required
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		if (didWeStartWork.get() == null)
		{
			didWeStartWork.set(new ConcurrentHashMap<>(5, 2, 1));
		}

		Transactional transactional = readTransactionMetadata(methodInvocation);

		CustomJpaPersistService emProvider = GuiceContext.get(Key.get(CustomJpaPersistService.class, transactional.entityManagerAnnotation()));
		UnitOfWork unitOfWork = GuiceContext.get(Key.get(UnitOfWork.class, transactional.entityManagerAnnotation()));
		PersistenceUnit unit = GuiceContext.get(Key.get(PersistenceUnit.class, transactional.entityManagerAnnotation()));

		// Should we start a unit of work?
		if (!emProvider.isWorking())
		{
			emProvider.begin();
			Map<Class<? extends Annotation>, Boolean> runningMap = didWeStartWork.get();
			runningMap.put(transactional.entityManagerAnnotation(), true);
		}
		Map<Class<? extends Annotation>, Boolean> mappedOut = didWeStartWork.get();
		Boolean startedWork = mappedOut != null && mappedOut.get(transactional.entityManagerAnnotation()) != null;

		EntityManager em = emProvider.get();

		// Allow 'joining' of transactions if there is an enclosing @Transactional method.
		boolean transactionAlreadyStarted = false;
		for (ITransactionHandler handler : GuiceContext.get(ITransactionHandlerReader))
		{
			if (handler.transactionExists(em, unit))
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
			//commit transaction only if rollback didn't occur
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
			//close the em if necessary
			if (startedWork)
			{
				mappedOut.remove(transactional.entityManagerAnnotation());
				unitOfWork.end();
			}
			//propagate whatever exception is thrown anyway
			throw e;
		}

		//everything was normal so commit the txn (do not move into try block above as it
		//  interferes with the advised method's throwing semantics)
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
			//close the em if necessary
			if (startedWork)
			{
				mappedOut.remove(transactional.entityManagerAnnotation());
				unitOfWork.end();
			}
		}

		//or return result
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
	private Transactional readTransactionMetadata(MethodInvocation methodInvocation)
	{
		Transactional transactional;
		Method method = methodInvocation.getMethod();
		Class<?> targetClass = methodInvocation.getThis()
		                                       .getClass();

		transactional = method.getAnnotation(Transactional.class);
		if (null == transactional)
		{
			// If none on method, try the class.
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
	private boolean rollbackIfNecessary(Transactional transactional, Exception e, PersistenceUnit unit, EntityManager em)
	{
		boolean commit = true;

		//check rollback clauses
		for (Class<? extends Exception> rollBackOn : transactional.rollbackOn())
		{

			//if one matched, try to perform a rollback
			if (rollBackOn.isInstance(e))
			{
				commit = false;

				//check ignore clauses (supercedes rollback clause)
				for (Class<? extends Exception> exceptOn : transactional.ignore())
				{
					//An exception to the rollback clause was found, DON'T rollback
					// (i.e. commit and throw anyway)
					if (exceptOn.isInstance(e))
					{
						commit = true;
						break;
					}
				}

				//rollback only if nothing matched the ignore check
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
				//otherwise continue to commit
				break;
			}
		}

		return commit;
	}
}
