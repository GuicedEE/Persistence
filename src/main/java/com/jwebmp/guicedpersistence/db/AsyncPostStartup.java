package com.jwebmp.guicedpersistence.db;

import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.guicedpersistence.services.IAsyncStartup;
import com.jwebmp.logger.LogFactory;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class uses the
 */
public final class AsyncPostStartup
		implements IGuicePostStartup
{
	private static final Logger log = LogFactory.getLog("AsyncPostStartup");
	private static final ExecutorService dbAutoStartupExecutors = Executors.newFixedThreadPool(Runtime.getRuntime()
	                                                                                                  .availableProcessors());

	public AsyncPostStartup()
	{
		//No Config
	}

	/**
	 * Starts the persistence service, should be threaded if the sort orders are properly applied
	 */
	@Override
	public void postLoad()
	{
		log.config("Loading AsyncPostStartup - " + Runtime.getRuntime()
		                                                  .availableProcessors() + " threads");
		ServiceLoader<IAsyncStartup> loader = ServiceLoader.load(IAsyncStartup.class);
		//Backwards compat - switch to loader.findFirst()
		Iterator<IAsyncStartup> iterator = loader.iterator();
		if (iterator.hasNext())
		{

			for (IAsyncStartup startup : loader)
			{
				log.config("Scheduling IAsyncStartup - " + startup.getClass());
				dbAutoStartupExecutors.execute(() ->
				                               {
					                               log.config("Loading IAsyncStartup - " + startup.getClass());
					                               try
					                               {
						                               GuiceContext.getInstance(startup.getClass());
						                               log.config("Started IAsyncStartup - " + startup.getClass());
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
