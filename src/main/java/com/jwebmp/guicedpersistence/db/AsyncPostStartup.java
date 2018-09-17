package com.jwebmp.guicedpersistence.db;

import com.jwebmp.guicedinjection.interfaces.IDefaultService;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.guicedpersistence.services.IAsyncStartup;
import com.jwebmp.logger.LogFactory;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This class loads up stuff asynchronously during boot
 */
public final class AsyncPostStartup
		implements IGuicePostStartup
{
	/**
	 * This logger
	 */
	private static final Logger log = LogFactory.getLog("AsyncPostStartup");
	/**
	 * The execution service
	 */
	private static final ExecutorService dbAutoStartupExecutors = Executors.newFixedThreadPool(Runtime.getRuntime()
	                                                                                                  .availableProcessors());

	/**
	 * Default constructor for async post startup
	 */
	public AsyncPostStartup()
	{
		//No Config
	}

	/**
	 * Method getExecutionService returns the executionService of this AsyncPostStartup object.
	 *
	 * @return the executionService (type ExecutorService) of this AsyncPostStartup object.
	 */
	public static ExecutorService getExecutionService()
	{
		return dbAutoStartupExecutors;
	}

	/**
	 * Starts the persistence service, should be threaded if the sort orders are properly applied
	 *
	 * @see com.jwebmp.guicedinjection.interfaces.IGuicePostStartup#postLoad()
	 */
	@Override
	public void postLoad()
	{
		Set<IAsyncStartup> startups = IDefaultService.loaderToSet(ServiceLoader.load(IAsyncStartup.class));//GuiceContext.get(IAsyncStartupReader);

		for (Class<? extends Annotation> boundAnnotation : AbstractDatabaseProviderModule.getBoundAnnotations())
		{
			startups.add(new DbStartupThread(boundAnnotation));
		}

		Iterator<IAsyncStartup> iterator = startups.iterator();
		if (iterator.hasNext())
		{
			AsyncPostStartup.log.config("Loading AsyncPostStartup - " + Runtime.getRuntime()
			                                                                   .availableProcessors() + " threads");
			for (IAsyncStartup startup : startups)
			{
				AsyncPostStartup.log.config("Scheduling IAsyncStartup - " + startup.name());
				AsyncPostStartup.dbAutoStartupExecutors.execute(startup);
			}
		}
		AsyncPostStartup.dbAutoStartupExecutors.shutdownNow();
	}

	/**
	 * Sets the order in which this must run, default 50.
	 *
	 * @return the sort order to return
	 *
	 * @see com.jwebmp.guicedinjection.interfaces.IGuicePostStartup#sortOrder()
	 */
	@Override
	public Integer sortOrder()
	{
		return 50;
	}

}
