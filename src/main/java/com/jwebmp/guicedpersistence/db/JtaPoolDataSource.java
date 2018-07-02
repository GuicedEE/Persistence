package com.jwebmp.guicedpersistence.db;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import javax.sql.DataSource;

/**
 * Provides the DataSource.
 */
@Singleton
public class JtaPoolDataSource
		implements Provider<DataSource>, CustomPoolDataSource
{

	private transient DataSource providedDataSource;
	/**
	 * Intentional double assignment
	 */
	private transient PoolingDataSource pds;

	public JtaPoolDataSource()
	{
		//No config required
	}

	@Override
	public void configure(ConnectionBaseInfo cbi)
	{
		if (cbi.isXa())
		{
			processXs(cbi);
		}
		else
		{
			processNonXa(cbi);
		}
	}

	private void processXs(ConnectionBaseInfo cbi)
	{
		pds = cbi.toPooledDatasource();

		if (cbi.getDatabaseName() != null)
		{
			pds.getDriverProperties()
			   .setProperty("DatabaseName", cbi.getDatabaseName());
		}
		if (cbi.getUsername() != null)
		{
			pds.getDriverProperties()
			   .setProperty("User", cbi.getUsername());
		}
		if (cbi.getPassword() != null)
		{
			pds.getDriverProperties()
			   .setProperty("Password", cbi.getPassword());
		}
		if (cbi.getServerName() != null)
		{
			pds.getDriverProperties()
			   .setProperty("ServerName", cbi.getServerName());
		}
		if (cbi.getPort() != null)
		{
			pds.getDriverProperties()
			   .setProperty("Port", cbi.getPort());
		}
		if (cbi.getInstanceName() != null)
		{
			pds.getDriverProperties()
			   .setProperty(cbi.getServerInstanceNameProperty(), cbi.getInstanceName());
		}

		pds.init();

		providedDataSource = pds;
	}

	private void processNonXa(ConnectionBaseInfo cbi)
	{
		pds = cbi.toPooledDatasource();
		if (cbi.getTransactionIsolation() != null)
		{
			pds.setIsolationLevel(cbi.getTransactionIsolation()
			                         .name());
		}
		pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
		if (cbi.getDriverClass() != null)
		{
			pds.getDriverProperties()
			   .setProperty("driverClassName", cbi.getDriverClass());
		}
		if (cbi.getUrl() != null)
		{
			pds.getDriverProperties()
			   .setProperty("url", cbi.getUrl());
		}
		if (cbi.getUsername() != null)
		{
			pds.getDriverProperties()
			   .setProperty("user", cbi.getUsername());
		}
		if (cbi.getPassword() != null)
		{
			pds.getDriverProperties()
			   .setProperty("password", cbi.getPassword());
		}

		pds.init();
		providedDataSource = pds;
	}

	@Override
	public DataSource get()
	{
		return providedDataSource;
	}
}
