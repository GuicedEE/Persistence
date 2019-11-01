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

public class DatasourceStartup
		implements IGuicePostStartup<DatasourceStartup>, Callable<DatasourceStartup>, Runnable, Provider<DataSource>
{
	private static final Logger log = LogFactory.getLog("DatasourceStartup");

	private final Class<? extends Annotation> annotation;
	private final String jndiName;

	public DatasourceStartup(Class<? extends Annotation> annotation,String jndiName)
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
		return DbStartup.getLoadedConnectionBaseInfos();
	}

	/**
	 * Returns the data sources associated with an annotation
	 *
	 * @return
	 */
	public static Map<Class<? extends Annotation>, DataSource> getLoadedDataSources()
	{
		return DbStartup.getLoadedDataSources();
	}

	public String name()
	{
		return "DB Startup - @" + annotation.getSimpleName();
	}

	@Override
	public void postLoad()
	{
		log.log(Level.CONFIG, "DataSource Init - " + annotation.getSimpleName());
		if (DbStartup.getAvailableDataSources().contains(annotation))
		{
			if(DbStartup.getJndiDataSources().containsKey(jndiName))
			{
				getLoadedDataSources().put(annotation, DbStartup.getJndiDataSources().get(jndiName));
			}
			else if(!getLoadedDataSources().containsKey(annotation))
			{
				ConnectionBaseInfo cbi = getLoadedConnectionBaseInfos().get(jndiName);
				try
				{
					log.log(Level.CONFIG, "DataSource Starting - " + annotation.getSimpleName());
					DataSource ds = cbi.toPooledDatasource();
					DbStartup.getJndiDataSources().put(jndiName, ds);
					getLoadedDataSources().put(annotation, ds);
				}catch(IllegalArgumentException t)
				{
					log.log(Level.CONFIG, "DataSource Illegal Argument (Perhaps this resource is already registered?) - [" + annotation + "]", t);
					getLoadedDataSources().put(annotation, DbStartup.getJndiDataSources().get(cbi.getJndiName()));
				}
				catch(Throwable t)
				{
					log.log(Level.SEVERE, "Cannot start data source [" + annotation + "]", t);
				}
			}
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 54;
	}

	@Override
	public void run()
	{
		postLoad();
	}

	@Override
	public DatasourceStartup call() throws Exception
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
