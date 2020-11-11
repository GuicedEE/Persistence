package com.guicedee.guicedpersistence.implementations;

import com.google.inject.persist.PersistService;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.guicedinjection.interfaces.JobService;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;
import com.guicedee.logger.LogFactory;

import jakarta.persistence.EntityManager;
import java.util.logging.Level;

public class EntityManagerPostStartup
		implements IGuicePostStartup<EntityManagerPostStartup>
{
	private static boolean blocking = true;
	private static boolean startPersistenceServices = true;

	@Override
	public void postLoad()
	{
		if(startPersistenceServices) {
			LogFactory.getLog(EntityManagerPostStartup.class)
					.log(Level.CONFIG, "Starting up Entity Managers");
			if(blocking) {
				PersistenceServicesModule.getModules()
						.entrySet()
						.parallelStream()
						.forEach(entry ->
								{
									PersistService ps = GuiceContext.get(PersistService.class, entry.getKey());
									ps.start();
									LogFactory.getLog(EntityManagerPostStartup.class)
											.log(Level.CONFIG, "Started " + entry);
								}
						);
			}else
			{
				PersistenceServicesModule.getModules().forEach((key,value)->{
					JobService.getInstance().addJob("DatabaseStartups",()->{
						PersistService ps = GuiceContext.get(PersistService.class, key);
						ps.start();
						LogFactory.getLog(EntityManagerPostStartup.class)
								.log(Level.CONFIG, "Started " + key);
					});
				});
			}
		}
	}

	/**
	 * If loading the entity managers should block the load
	 *
	 */
	public static boolean isBlocking() {
		return blocking;
	}

	/**
	 * If the service starting should block the load sequence
	 * @param blocking
	 */
	public static void setBlocking(boolean blocking) {
		EntityManagerPostStartup.blocking = blocking;
	}

	/**
	 * If the persistence services should start on boot, or when you want them too
	 */
	public static boolean isStartPersistenceServices() {
		return startPersistenceServices;
	}

	/**
	 * If the persistence services should start on boot, or when you want them too
	 * @param startPersistenceServices default true
	 */
	public static void setStartPersistenceServices(boolean startPersistenceServices) {
		EntityManagerPostStartup.startPersistenceServices = startPersistenceServices;
	}

	@Override
	public Integer sortOrder()
	{
		return Integer.MIN_VALUE + 500;
	}
}
