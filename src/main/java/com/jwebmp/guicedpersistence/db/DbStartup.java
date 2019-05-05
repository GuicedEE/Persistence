package com.jwebmp.guicedpersistence.db;

import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.logger.LogFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbStartup
		implements IGuicePostStartup<DbStartup>, Callable<DbStartup>, Runnable
{
	private static final Logger log = LogFactory.getLog("DbStartup");
	/**
	 * A list of already loaded data sources identified by JNDI Name
	 */
	private static final Map<String, ConnectionBaseInfo> loadedConnectionBaseInfos = new ConcurrentHashMap<>();
	private static final Map<Class<? extends Annotation>, DataSource> loadedDataSources = new ConcurrentHashMap<>();
	private static final List<Class<? extends Annotation>> availableDataSources = new CopyOnWriteArrayList<>();
	private Class<? extends Annotation> annotation;

	public DbStartup(Class<? extends Annotation> annotation)
	{
		this.annotation = annotation;
	}

	/**
	 * .
	 * A list of already loaded data sources identified by JNDI Name
	 * <p>
	 *
	 * @return Map String DataSource
	 */
	public static Map<String, ConnectionBaseInfo> getLoadedConnectionBaseInfos()
	{
		return loadedConnectionBaseInfos;
	}

	/**
	 * Returns the data sources associated with an annotation
	 * @return
	 */
	public static Map<Class<? extends Annotation>, DataSource> getLoadedDataSources()
	{
		return loadedDataSources;
	}

	public String name()
	{
		return "DB Startup - @" + annotation.getSimpleName();
	}

	@Override
	public void postLoad()
	{
		LogFactory.getLog("DBStartup")
		          .log(Level.CONFIG, "DB Starting - " + annotation.getSimpleName());
		LogFactory.getLog("DataSource Startup")
		          .log(Level.CONFIG, "DataSource Starting - " + annotation.getSimpleName());
		try
		{
			GuiceContext.get(DataSource.class, annotation);
		}
		catch (Throwable T)
		{
			LogFactory.getLog("DataSource Startup")
			          .log(Level.SEVERE, "Datasource Unable to start", T);
		}
		try
		{
			PersistService ps = GuiceContext.get(PersistService.class, annotation);
			ps.start();
			UnitOfWork ow = GuiceContext.get(UnitOfWork.class, annotation);
			ow.end();
		}
		catch (Throwable T)
		{
			LogFactory.getLog("DBStartup")
			          .log(Level.SEVERE, "Datasource Unable to start", T);
		}
		try
		{
			notify();
		}catch(IllegalMonitorStateException me)
		{
			log.log(Level.FINER, "Notify not applicable for this running execution", me);
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 55;
	}

	public static List<Class<? extends Annotation>> getAvailableDataSources()
	{
		return availableDataSources;
	}

	@Override
	public void run()
	{
		postLoad();
	}

	@Override
	public DbStartup call() throws Exception
	{
		postLoad();
		return this;
	}
}
