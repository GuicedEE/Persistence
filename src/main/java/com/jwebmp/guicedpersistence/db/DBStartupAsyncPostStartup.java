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
			ExecutorService loadAsync = Executors.newFixedThreadPool(Runtime.getRuntime()
			                                                                .availableProcessors());
			for (IDBStartup startup : loader)
			{
				log.config("Scheduling IDBStartup - " + startup.getClass());
				loadAsync.execute(() ->
				                  {
					                  log.config("Loading IDBStartup - " + startup.getClass());
					                  try
					                  {
						                  GuiceContext.getInstance(startup.getClass());
					                  }
					                  catch (Throwable T)
					                  {
						                  log.log(Level.SEVERE, "Unable to inject " + startup.getClass(), T);
					                  }
				                  });
			}
			loadAsync.shutdown();
			try
			{
				loadAsync.awaitTermination(1, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				log.log(Level.SEVERE, "Timeout starting databases in executor service");
			}
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}
}
