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

package com.guicedee.guicedpersistence.injectors;

import com.google.inject.Key;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedpersistence.db.annotations.Transactional;
import com.guicedee.guicedpersistence.scanners.PersistenceServiceLoadersBinder;
import com.guicedee.guicedpersistence.services.ITransactionHandler;
import jakarta.persistence.EntityManager;
import lombok.extern.java.Log;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Log
public class GuicedPersistenceTxnInterceptor
		implements MethodInterceptor
{
	/**
	 * Tracks if the unit of work was begun implicitly by this transaction.
	 */
	private final ThreadLocal<Boolean> didWeStartWork = new ThreadLocal<>();
	private final ThreadLocal<ITransactionHandler<?>> handler = new ThreadLocal<>();
	public GuicedPersistenceTxnInterceptor()
	{
		//No config required
	}
	
	@Override
	@SuppressWarnings("Duplicates")
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		Transactional transactional = readTransactionMetadata(methodInvocation);
		boolean startedWork = didWeStartWork.get() != null && didWeStartWork.get();
		
		if ((startedWork) || methodInvocation.getMethod()
		                                     .isSynthetic())
		{
			return methodInvocation.proceed();
		}
		CustomJpaPersistService emProvider = GuiceContext.get(Key.get(CustomJpaPersistService.class, transactional.entityManagerAnnotation()));
		
		UnitOfWork unitOfWork = GuiceContext.get(Key.get(UnitOfWork.class, transactional.entityManagerAnnotation()));
		ParsedPersistenceXmlDescriptor unit = GuiceContext.get(Key.get(ParsedPersistenceXmlDescriptor.class, transactional.entityManagerAnnotation()));
		EntityManager em = emProvider.get();
		ITransactionHandler<?> handle = null;
		
		if(handler.get() == null)
		{
			//active for automation handling, enabled for interception handling
			for (ITransactionHandler<?> handler : GuiceContext.get(PersistenceServiceLoadersBinder.ITransactionHandlerReader))
			{
				if (handler.enabled(unit))
				{
					handle = handler;
					break;
				}
			}
			if (handle == null)
			{
				log.log(Level.WARNING, "No transaction handler found");
				return methodInvocation.proceed();
			}
		}else {
			handle = handler.get();
		}
/*
		if (handle.transactionExists(em, unit))
		{
			transactionAlreadyStarted = true;
		}*/
		
		if (!emProvider.isWorking())
		{
			emProvider.begin();
			didWeStartWork.set(true);
		}
		
		handle.setTransactionTimeout(transactional.timeout(), em, unit);
		handle.beginTransacation(false, em, unit);
		startedWork = true;
		didWeStartWork.set(true);
		
		Object result;
		try
		{
			result = methodInvocation.proceed();
		}
		catch (Exception e)
		{
			if (rollbackIfNecessary(transactional, e, handle, unit, em))
			{
				if (startedWork)
				{
					handle.commitTransacation(false, em, unit);
				}
			}
			
			if (startedWork)
			{
				didWeStartWork.remove();
				handler.remove();
				unitOfWork.end();
			}
			log.log(Level.SEVERE, "Unable to commit : ", e);
			throw e;
		}
		try
		{
			if (startedWork)
			{
				handle.commitTransacation(false, em, unit);
			}
		}
		finally
		{
			handler.remove();
			if (startedWork)
			{
				if (em != null && em.isOpen())
				{
					em.clear();
					em.close();
				}
				didWeStartWork.remove();
				unitOfWork.end();
			}
		}
		return result;
	}
	
	/**
	 * Method readTransactionMetadata ...
	 *
	 * @param methodInvocation of type MethodInvocation
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
	 * @param transactional The metadata annotation of the method
	 * @param e             The exception to test for rollback
	 * @param em            Entity Manager
	 * @param unit          The associated persistence unit
	 */
	@SuppressWarnings("Duplicates")
	private boolean rollbackIfNecessary(Transactional transactional, Exception e, ITransactionHandler<?> handle, ParsedPersistenceXmlDescriptor unit, EntityManager em)
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
					handle.rollbackTransacation(false, em, unit);
				}
				break;
			}
		}
		
		return commit;
	}
}
