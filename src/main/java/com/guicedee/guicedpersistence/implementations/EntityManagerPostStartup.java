package com.guicedee.guicedpersistence.implementations;

import com.google.inject.Module;
import com.google.inject.persist.*;
import com.guicedee.guicedinjection.*;
import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedpersistence.services.*;
import com.guicedee.logger.*;

import java.lang.annotation.*;
import java.util.*;
import java.util.logging.*;

public class EntityManagerPostStartup
		implements IGuicePostStartup<EntityManagerPostStartup>
{
	private static boolean blocking = true;
	private static boolean startPersistenceServices = true;
	
	private static final Logger log = LogFactory.getLog(".persistence.EntityManagerPostStartup");
	
	@Override
	public void postLoad()
	{
		if (startPersistenceServices)
		{
			LogFactory.getLog(EntityManagerPostStartup.class)
			          .log(Level.CONFIG, "Starting up Entity Managers");
			if (blocking)
			{
				List<Map.Entry<Class<? extends Annotation>, Module>> collect = new ArrayList<>(PersistenceServicesModule.getModules()
				                                                                                                        .entrySet());
				if (!collect.isEmpty())
				{
					Map.Entry<Class<? extends Annotation>, Module> entry = collect.stream()
					                                                              .findFirst()
					                                                              .get();
					PersistService ps = GuiceContext.get(PersistService.class, entry.getKey());
					ps.start();
					log.log(Level.CONFIG, "Started " + entry);
				}
			}
			else
			{
				List<Map.Entry<Class<? extends Annotation>, Module>> collect = new ArrayList<>(PersistenceServicesModule.getModules()
				                                                                                                        .entrySet());
				if (!collect.isEmpty())
				{
					Map.Entry<Class<? extends Annotation>, Module> entry = collect.stream()
					                                                              .findFirst()
					                                                              .get();
					
					log.log(Level.CONFIG, "Starting Async " + entry);
					JobService.getInstance()
					          .addJob("DatabaseStartups", () -> {
						          try
						          {
							          PersistService ps = GuiceContext.get(PersistService.class, entry.getKey());
							          ps.start();
							          log.log(Level.CONFIG, "Started " + entry);
						          }
						          catch (Throwable t)
						          {
							          log.log(Level.SEVERE, "Fatal exception in starting Persistence Service - " + entry, t);
						          }
					          });
					
				}
			}
		}
	}
	
	/**
	 * If loading the entity managers should block the load
	 */
	public static boolean isBlocking()
	{
		return blocking;
	}
	
	/**
	 * If the service starting should block the load sequence
	 *
	 * @param blocking
	 */
	public static void setBlocking(boolean blocking)
	{
		EntityManagerPostStartup.blocking = blocking;
	}
	
	/**
	 * If the persistence services should start on boot, or when you want them too
	 */
	public static boolean isStartPersistenceServices()
	{
		return startPersistenceServices;
	}
	
	/**
	 * If the persistence services should start on boot, or when you want them too
	 *
	 * @param startPersistenceServices default true
	 */
	public static void setStartPersistenceServices(boolean startPersistenceServices)
	{
		EntityManagerPostStartup.startPersistenceServices = startPersistenceServices;
	}
	
	@Override
	public Integer sortOrder()
	{
		return Integer.MIN_VALUE + 500;
	}
}
