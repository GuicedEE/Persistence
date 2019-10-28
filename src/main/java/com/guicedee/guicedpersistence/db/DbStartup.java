package com.guicedee.guicedpersistence.db;

import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.logger.LogFactory;

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
		implements IGuicePostStartup<DbStartup>, Callable<DbStartup>, Runnable, Provider<DataSource>
{
	private static final Logger log = LogFactory.getLog("DbStartup");
	/**
	 * A list of already loaded data sources identified by JNDI Name
	 */
	private static final Map<String, ConnectionBaseInfo> loadedConnectionBaseInfos = new ConcurrentHashMap<>();
	private static final Map<Class<? extends Annotation>, DataSource> loadedDataSources = new ConcurrentHashMap<>();
	private static final Map<String, DataSource> jndiDataSources = new ConcurrentHashMap<>();

	private static final List<Class<? extends Annotation>> availableDataSources = new CopyOnWriteArrayList<>();

	private final Class<? extends Annotation> annotation;
	private final String jndiName;

	public DbStartup(Class<? extends Annotation> annotation,String jndiName)
	{
		this.annotation = annotation;
		this.jndiName = jndiName;
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
	 *
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
		log.log(Level.CONFIG, "DataSource Init - " + annotation.getSimpleName());

		if (availableDataSources.contains(annotation))
		{
			if(jndiDataSources.containsKey(jndiName))
			{
				getLoadedDataSources().put(annotation, jndiDataSources.get(jndiName));
			}
			else if(!getLoadedDataSources().containsKey(annotation))
			{
				ConnectionBaseInfo cbi = getLoadedConnectionBaseInfos().get(jndiName);
				try
				{
					log.log(Level.CONFIG, "DataSource Starting - " + annotation.getSimpleName());
					DataSource ds = cbi.toPooledDatasource();
					if(ds == null)
					{
						log.log(Level.WARNING, "No Data Source Registered - [" + annotation + "]. Consider adding a pool");
					}
					else
					{
						jndiDataSources.put(jndiName, ds);
						getLoadedDataSources().put(annotation, ds);
					}
				}catch(IllegalArgumentException t)
				{
					log.log(Level.CONFIG, "DataSource Illegal Argument (Perhaps this resource is already registered?) - [" + annotation + "]", t);
					getLoadedDataSources().put(annotation, jndiDataSources.get(cbi.getJndiName()));
				}
				catch(Throwable t)
				{
					log.log(Level.SEVERE, "Cannot start data source [" + annotation + "]", t);
				}
			}
		}
		log.log(Level.CONFIG, "Entity Manager/Persist Service Starting - " + annotation.getSimpleName());
		try
		{
			PersistService ps = GuiceContext.get(PersistService.class, annotation);
			ps.start();
			UnitOfWork ow = GuiceContext.get(UnitOfWork.class, annotation);
			ow.end();
		}
		catch (Throwable T)
		{
			log.log(Level.SEVERE, "Persist Service Unable to start/end", T);
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

	@Override
	public DataSource get()
	{
		return getLoadedDataSources().get(annotation);
	}
}
