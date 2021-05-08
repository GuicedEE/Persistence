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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.logger.LogFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.lang.annotation.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class CustomJpaPersistService
		implements Provider<EntityManager>, UnitOfWork, PersistService
{
	/**
	 * Field log
	 */
	private static final Logger log = LogFactory.getLog("PersistService");

	/**
	 * Thread Local instances of Entity Managers
	 */
	private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();
	/**
	 * The assigned persistence unit name
	 */
	private String persistenceUnitName;
	/**
	 * The given properties object
	 */
	private Map<?, ?> persistenceProperties;
	/**
	 * The assigned annotation for the entity manager
	 */
	private Class<? extends Annotation> annotation;
	/**
	 * The service em factory
	 */
	private volatile EntityManagerFactory emFactory;

	public CustomJpaPersistService()
	{
		//No configuration
	}

	public CustomJpaPersistService(
			String persistenceUnitName, Map<?, ?> persistenceProperties, Class<? extends Annotation> annotation)
	{
		this.persistenceUnitName = persistenceUnitName;
		this.persistenceProperties = persistenceProperties;
		this.annotation = annotation;
	}

	@Override
	public EntityManager get()
	{
		if (!isWorking())
		{
			begin();
		}

		EntityManager em = entityManager.get();
		Preconditions.checkState(
				null != em,
				"Requested EntityManager outside work unit. "
				+ "Try calling UnitOfWork.begin() first, or use a PersistFilter if you "
				+ "are inside a servlet environment.");

		em.setProperty("annotation", annotation);
		return em;
	}

	public boolean isWorking()
	{
		return entityManager.get() != null;
	}

	@Override
	public void begin()
	{
		if (entityManager.get() != null)
		{
			log.warning("Work already begun on this thread. Looks like you have called UnitOfWork.begin() twice"
			            + " without a balancing call to end() in between.");
		}
		if (emFactory == null)
		{
			start();
		}
		else if (!emFactory.isOpen())
		{
			start();
		}
		entityManager.set(emFactory.createEntityManager());
	}

	@Override
	public void end()
	{
		EntityManager em = entityManager.get();

		// Let's not penalize users for calling end() multiple times.
		if (null == em)
		{
			return;
		}

		try
		{
			em.close();
		}
		finally
		{
			entityManager.remove();
		}
	}

	/**
	 * Starts up the Entity Manager Factory
	 */
	@Override
	public void start()
	{
		if (emFactory != null && emFactory.isOpen())
		{
			return;
		}
		try
		{
			CustomJpaPersistService.log.finer("Starting up Persist Service - " + persistenceUnitName);
			if (null != persistenceProperties)
			{
				emFactory =
						Persistence.createEntityManagerFactory(persistenceUnitName, persistenceProperties);
			}
			else
			{
				emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
			}
			CustomJpaPersistService.log.finer("Persist Service Started - " + persistenceUnitName);
		}
		catch (Throwable T)
		{
			CustomJpaPersistService.log.log(Level.SEVERE, "Unable to get entity factory : " + T.getMessage(), T);
		}
	}

	/**
	 * Stops the Entity Manager Factory
	 */
	@Override
	public void stop()
	{
		if (emFactory == null || !emFactory.isOpen())
		{
			return;
		}
		emFactory.close();
		log.finer("Entity Manager Factory for " + persistenceUnitName + " has been closed on the current thread");
	}

	@VisibleForTesting
	synchronized void start(EntityManagerFactory emFactory)
	{
		this.emFactory = emFactory;
	}

	public String getPersistenceUnitName()
	{
		return persistenceUnitName;
	}

	public void setPersistenceUnitName(String persistenceUnitName)
	{
		this.persistenceUnitName = persistenceUnitName;
	}

	public Map<?, ?> getPersistenceProperties()
	{
		return persistenceProperties;
	}

	public void setPersistenceProperties(Map<?, ?> persistenceProperties)
	{
		this.persistenceProperties = persistenceProperties;
	}

	public Class<? extends Annotation> getAnnotation()
	{
		return annotation;
	}

	public void setAnnotation(Class<? extends Annotation> annotation)
	{
		this.annotation = annotation;
	}

	public EntityManagerFactory getEmFactory()
	{
		return emFactory;
	}

	public void setEmFactory(EntityManagerFactory emFactory)
	{
		this.emFactory = emFactory;
	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	private @interface Nullable {}

	public static class EntityManagerFactoryProvider
			implements Provider<EntityManagerFactory>
	{
		private CustomJpaPersistService emProvider;

		public EntityManagerFactoryProvider()
		{
		}

		@Inject
		public EntityManagerFactoryProvider(CustomJpaPersistService emProvider)
		{
			this.emProvider = emProvider;
		}

		public CustomJpaPersistService getEmProvider()
		{
			return emProvider;
		}

		public void setEmProvider(CustomJpaPersistService emProvider)
		{
			this.emProvider = emProvider;
		}

		@Override
		public EntityManagerFactory get()
		{
			assert null != emProvider.emFactory;
			return emProvider.emFactory;
		}


	}
}
