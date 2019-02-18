package com.jwebmp.guicedpersistence.db;

import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.logger.LogFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class DbStartup
		implements IGuicePostStartup<DbStartup>
{
	/**
	 * A list of already loaded data sources identified by JNDI Name
	 */
	private static final Map<String, ConnectionBaseInfo> loadedDataSources = new HashMap<>();
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
	public static Map<String, ConnectionBaseInfo> getLoadedDataSources()
	{
		return loadedDataSources;
	}

	public String name()
	{
		return "DBStartup - @" + annotation.getSimpleName();
	}

	@Override
	public void postLoad()
	{
		try
		{
			GuiceContext.get(DataSource.class, annotation);
		}
		catch (Throwable T)
		{
			LogFactory.getLog("DBStartup")

			          .log(Level.SEVERE, "Datasource Unable to start", T);

		}
		PersistService ps = GuiceContext.get(PersistService.class, annotation);
		ps.start();
		UnitOfWork ow = GuiceContext.get(UnitOfWork.class, annotation);
		ow.end();

		LogFactory.getLog("DBStartup")
		          .log(Level.CONFIG, "DBStartup Started - " + annotation.getSimpleName());
	}

	@Override
	public Integer sortOrder()
	{
		return 55;
	}
}
