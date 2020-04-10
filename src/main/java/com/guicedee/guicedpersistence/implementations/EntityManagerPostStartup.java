package com.guicedee.guicedpersistence.implementations;

import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;
import com.guicedee.logger.LogFactory;

import javax.persistence.EntityManager;
import java.util.logging.Level;

public class EntityManagerPostStartup
		implements IGuicePostStartup<EntityManagerPostStartup>
{
	@Override
	public void postLoad()
	{
		LogFactory.getLog(EntityManagerPostStartup.class)
		          .log(Level.CONFIG, "Starting up Entity Managers");
		PersistenceServicesModule.getModules()
		                         .entrySet()
		                         .parallelStream()
		                         .forEach(entry -> GuiceContext.get(EntityManager.class, entry.getKey()));
	}

	@Override
	public Integer sortOrder()
	{
		return Integer.MAX_VALUE - 500;
	}
}
