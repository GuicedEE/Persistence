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
	private static final ServiceLoader<IAsyncStartup> loader = ServiceLoader.load(IAsyncStartup.class);

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
		Iterator<IAsyncStartup> iterator = AsyncPostStartup.loader.iterator();
		if (iterator.hasNext())
		{
			AsyncPostStartup.log.config("Loading AsyncPostStartup - " + Runtime.getRuntime()
			                                                                   .availableProcessors() + " threads");
			for (IAsyncStartup startup : AsyncPostStartup.loader)
			{
				AsyncPostStartup.log.config("Scheduling IAsyncStartup - " + startup.getClass());
				AsyncPostStartup.dbAutoStartupExecutors.execute(() ->
				                                                {
					                                                AsyncPostStartup.log.fine("Loading IAsyncStartup - " + startup.getClass());
					                                                try
					                                                {
						                                                GuiceContext.getInstance(startup.getClass());
						                                                AsyncPostStartup.log.fine("Started IAsyncStartup - " + startup.getClass());
					                                                }
					                                                catch (Throwable T)
					                                                {
						                                                AsyncPostStartup.log.log(Level.SEVERE, "Unable to inject " + startup.getClass(), T);
					                                                }
				                                                });
			}
			AsyncPostStartup.dbAutoStartupExecutors.shutdown();
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}
}
