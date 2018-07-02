package com.jwebmp.guicedpersistence.db;

import com.google.inject.persist.PersistService;
import com.jwebmp.guicedinjection.annotations.GuicePostStartup;
import com.jwebmp.guicedpersistence.annotations.DBStartup;
import com.jwebmp.logger.LogFactory;

import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

@DBStartup
public abstract class DBStartupAsync
		implements GuicePostStartup
{
	private static final Logger log = LogFactory.getLog("DBStartupAsync");

	/**
	 * The given persistService
	 */
	private PersistService persistService;
	/**
	 * The given dataSource
	 */
	@SuppressWarnings("")
	private DataSource dataSource;

	protected DBStartupAsync()
	{
		log.finer(
				"Invalid DB Startup Call to blank constructor. Hopefully this is only being called from guice context!." +
				"Make sure to " +
				"super an injected data source and/or persistence service. super(ps,ds) or super(ds)");
	}

	/**
	 * Starts up the database with the given persist service and initializes the BTM datasource,
	 * The Datasource must initialize before the persist service is started, so it must be injected into this class
	 * <p>
	 * <p>
	 * <p>
	 */
	protected DBStartupAsync(PersistService persistService, DataSource dataSource)
	{
		this(dataSource);
		this.persistService = persistService;
	}

	/**
	 * Starts up the database with the given persist service and initializes the BTM datasource,
	 * The Datasource must initialize before the persist service is started, so it must be injected into this class
	 * <p>
	 * <p>
	 * <p>
	 */
	protected DBStartupAsync(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	/**
	 * Starts the persistence service, should be threaded if the sort orders are properly applied
	 */
	@Override
	public void postLoad()
	{
		if (persistService != null)
		{
			log.config("Starting DB Startup [" + getClass() + "]");
			try
			{
				persistService.start();
			}
			catch (IllegalStateException ise)
			{
				log.log(Level.FINER, "Persistence Unit started up externally", ise);
			}
			catch (Throwable ise)
			{
				log.log(Level.SEVERE, "Persistence Unit started failed", ise);
			}
		}
		else if (dataSource == null)
		{
			log.severe("Invalid DB Startup. Persist Service and Data Source is null. This Service won't work");
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}
}
