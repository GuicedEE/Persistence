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

import com.google.common.annotations.*;
import com.google.common.base.*;
import com.google.inject.*;
import com.google.inject.persist.*;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.lang.annotation.*;
import java.util.*;
import java.util.logging.*;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Log
public class CustomJpaPersistService
		implements Provider<EntityManager>, UnitOfWork, PersistService
{
	/**
	 * Thread Local instances of Entity Managers
	 */
	@Getter
	private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();
	/**
	 * The assigned persistence unit name
	 */
	@Getter
	@Setter
	private String persistenceUnitName;
	/**
	 * The given properties object
	 */
	@Getter
	@Setter
	private Map<?, ?> persistenceProperties;
	/**
	 * The assigned annotation for the entity manager
	 */
	@Getter
	@Setter
	private Class<? extends Annotation> annotation;
	/**
	 * The service em factory
	 */
	@Getter
	@Setter
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
			log.finer("Work already begun on this thread. Looks like you have called UnitOfWork.begin() twice"
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
