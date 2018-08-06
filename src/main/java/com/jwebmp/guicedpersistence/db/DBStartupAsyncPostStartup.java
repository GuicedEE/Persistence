package com.jwebmp.guicedpersistence.db;

import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.guicedpersistence.services.IDBStartup;
import com.jwebmp.logger.LogFactory;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class uses the
 */
public final class DBStartupAsyncPostStartup
		implements IGuicePostStartup
{
	private static final Logger log = LogFactory.getLog("DBStartupAsyncPostStartup");
	private static final ExecutorService dbAutoStartupExecutors = Executors.newFixedThreadPool(Runtime.getRuntime()
	                                                                                                  .availableProcessors());

	public DBStartupAsyncPostStartup()
	{
		//No Config
	}

	/**
	 * Starts the persistence service, should be threaded if the sort orders are properly applied
	 */
	@Override
	public void postLoad()
	{
		log.config("Loading DBStartupAsyncPostStartup - " + Runtime.getRuntime()
		                                                           .availableProcessors() + " threads");
		ServiceLoader<IDBStartup> loader = ServiceLoader.load(IDBStartup.class);
		//Backwards compat - switch to loader.findFirst()
		Iterator<IDBStartup> iterator = loader.iterator();
		if (iterator.hasNext())
		{

			for (IDBStartup startup : loader)
			{
				log.config("Scheduling IDBStartup - " + startup.getClass());
				dbAutoStartupExecutors.execute(() ->
				                               {
					                               log.config("Loading IDBStartup - " + startup.getClass());
					                               try
					                               {
						                               GuiceContext.getInstance(startup.getClass());
						                               log.config("Started IDBStartup - " + startup.getClass());
					                               }
					                               catch (Throwable T)
					                               {
						                               log.log(Level.SEVERE, "Unable to inject " + startup.getClass(), T);
					                               }
				                               });
			}
			dbAutoStartupExecutors.shutdown();
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}
}
