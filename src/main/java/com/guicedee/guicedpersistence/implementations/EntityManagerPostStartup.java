package com.guicedee.guicedpersistence.implementations;

import com.google.inject.persist.PersistService;
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
		                         .forEach(entry ->
		                                  {
			                                  PersistService ps = GuiceContext.get(PersistService.class, entry.getKey());
			                                  ps.start();
		                                  }
		                                 );
	}

	@Override
	public Integer sortOrder()
	{
		return Integer.MIN_VALUE + 500;
	}
}
