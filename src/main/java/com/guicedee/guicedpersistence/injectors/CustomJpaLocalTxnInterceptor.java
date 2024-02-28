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

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.client.*;
import com.guicedee.guicedpersistence.scanners.PersistenceServiceLoadersBinder;
import com.guicedee.guicedpersistence.services.ITransactionHandler;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import jakarta.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class CustomJpaLocalTxnInterceptor
		implements MethodInterceptor
{
	/**
	 * Is this the starting @Transactional?
	 */
	private final ThreadLocal<Boolean> didWeStartWork = new ThreadLocal<>();
	/**
	 * Injected provider for em
	 */
	@Inject
	private CustomJpaPersistService emProvider = null;

	@Override
	@SuppressWarnings("Duplicates")
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		if (!emProvider.isWorking())
		{
			emProvider.begin();
			didWeStartWork.set(true);
		}

		Transactional transactional = readTransactionMetadata(methodInvocation);
		EntityManager em = emProvider.get();
		Class<? extends Annotation> providedAnnotation = emProvider.getAnnotation();
		UnitOfWork unitOfWork = IGuiceContext.get(Key.get(UnitOfWork.class, emProvider.getAnnotation()));
		CustomJpaPersistService persistService =IGuiceContext.get(Key.get(CustomJpaPersistService.class, emProvider.getAnnotation()));

		ParsedPersistenceXmlDescriptor unit = IGuiceContext.get(Key.get(ParsedPersistenceXmlDescriptor.class, providedAnnotation));
		Boolean startedWork = didWeStartWork.get() == null ? false : didWeStartWork.get();
		if (startedWork) {
			persistService.start();
			unitOfWork.begin();
		}

		boolean transactionIsActive = false;
		for (ITransactionHandler handler : IGuiceContext.get(PersistenceServiceLoadersBinder.ITransactionHandlerReader))
		{
			if (handler.active(unit) && handler.transactionExists(em, unit))
			{
				transactionIsActive = true;
				break;
			}
		}

		if (!startedWork && transactionIsActive)
		{
			return methodInvocation.proceed();
		}

		for (ITransactionHandler handler : IGuiceContext.get(PersistenceServiceLoadersBinder.ITransactionHandlerReader))
		{
			if (handler.active(unit))
			{
				handler.beginTransacation(false, em, unit);
				break;
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
				for (ITransactionHandler handler : IGuiceContext.get(PersistenceServiceLoadersBinder.ITransactionHandlerReader))
				{
					if (handler.active(unit))
					{
						handler.commitTransacation(false, em, unit);
						break;
					}
				}
			}

			if (startedWork)
			{
				didWeStartWork.remove();
				unitOfWork.end();
				persistService.end();
			}
			throw e;
		}

		try
		{
			for (ITransactionHandler handler : IGuiceContext.get(PersistenceServiceLoadersBinder.ITransactionHandlerReader))
			{
				if (handler.active(unit))
				{
					handler.commitTransacation(false, em, unit);
					break;
				}
			}
		}
		finally
		{
			if (startedWork)
			{
				didWeStartWork.remove();
				unitOfWork.end();
				persistService.end();
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
	 * 		The metadata annotation of the method
	 * @param e
	 * 		The exception to test for rollback
	 * @param em
	 * 		Entity Manager
	 * @param unit
	 * 		The associated persistence unit
	 */
	@SuppressWarnings("Duplicates")
	private boolean rollbackIfNecessary(Transactional transactional, Exception e, ParsedPersistenceXmlDescriptor unit, EntityManager em)
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
					for (ITransactionHandler handler : IGuiceContext.get(PersistenceServiceLoadersBinder.ITransactionHandlerReader))
					{
						if (handler.active(unit))
						{
							handler.rollbackTransacation(false, em, unit);
							break;
						}
					}
				}
				break;
			}
		}

		return commit;
	}

	@Transactional
	private static class Internal {}
}