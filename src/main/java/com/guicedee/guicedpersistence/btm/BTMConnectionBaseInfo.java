package com.guicedee.guicedpersistence.btm;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;

import javax.sql.DataSource;

/**
 * This class is a basic container (mirror) for the database jtm builder string.
 * Exists to specify the default properties for connections that a jtm should implement should btm be switched for a different
 * implementation
 */
public class BTMConnectionBaseInfo
		extends ConnectionBaseInfo
		implements Cloneable
{
	/**
	 * Constructor BTMConnectionBaseInfo creates a new BTMConnectionBaseInfo instance with XA enabled
	 */
	public BTMConnectionBaseInfo()
	{
		setServerInstanceNameProperty("Instance");
	}

	/**
	 * Configures this handler as either an XA or Non-XA Resource
	 *
	 * @param xa
	 * 		If the connection is XA
	 */
	public BTMConnectionBaseInfo(boolean xa)
	{
		setXa(xa);
	}

	/**
	 * Returns the BTM Pooling Data Source Configured
	 *
	 * @return The datasource
	 */
	@Override
	public DataSource toPooledDatasource()
	{
		PoolingDataSource pds = new PoolingDataSource();
		if (getTransactionIsolation() != null)
		{
			pds.setIsolationLevel(getTransactionIsolation());
		}
		if (getMinPoolSize() != null)
		{
			pds.setMinPoolSize(getMinPoolSize());
		}
		if (getMaxPoolSize() != null)
		{
			pds.setMaxPoolSize(getMaxPoolSize());
		}
		if (getMaxIdleTime() != null)
		{
			pds.setMaxIdleTime(getMaxIdleTime());
		}
		if (getPreparedStatementCacheSize() != null)
		{
			pds.setPreparedStatementCacheSize(getPreparedStatementCacheSize());
		}

		if (getAcquireIncrement() != null)
		{
			pds.setAcquireIncrement(getAcquireIncrement());
		}
		if (getAcquisitionInterval() != null)
		{
			pds.setAcquisitionInterval(getAcquisitionInterval());
		}
		if (getAcquisitionTimeout() != null)
		{
			pds.setAcquisitionTimeout(getAcquisitionTimeout());
		}

		if (getAllowLocalTransactions() != null)
		{
			pds.setAllowLocalTransactions(getAllowLocalTransactions());
		}
		if (getApplyTransactionTimeout() != null)
		{
			pds.setApplyTransactionTimeout(getApplyTransactionTimeout());
		}
		if (getAutomaticEnlistingEnabled() != null)
		{
			pds.setAutomaticEnlistingEnabled(getAutomaticEnlistingEnabled());
		}
		if (getEnableJdbc4ConnectionTest() != null)
		{
			pds.setEnableJdbc4ConnectionTest(getEnableJdbc4ConnectionTest());
		}
		if (getIgnoreRecoveryFailures() != null)
		{
			pds.setIgnoreRecoveryFailures(getIgnoreRecoveryFailures());
		}

		if (getShareTransactionConnections() != null)
		{
			pds.setShareTransactionConnections(getShareTransactionConnections());
		}

		if (pds.getTestQuery() != null)
		{
			pds.setTestQuery(getTestQuery());
		}

		if (getJndiName() == null)
		{
			throw new UnsupportedOperationException(
					"JTA requires JNDI name to be specified, when inheriting from AbstractDatabaseModule make sure to provide a valid " + "value for getJndiMapping()");
		}
		pds.setUniqueName(getJndiName());
		if (getDriverClass() == null)
		{
			throw new UnsupportedOperationException("Please make sure to specify a driver class to use in the persistence.xml file or manually in this configuration " + "object.");
		}

		if (isXa())
		{
			processXa(this, pds);
		}
		else
		{
			processNonXa(this, pds);
		}
		
		try
		{
			pds.init();
		}catch (IllegalStateException ise)
		{
			//expected for duplicates
		}

		return pds;
	}

	/**
	 * Method processXa ...
	 *
	 * @param cbi
	 * 		of type ConnectionBaseInfo
	 * @param pds
	 * 		of type PoolingDataSource
	 *
	 * @return PoolingDataSource
	 */
	@SuppressWarnings("UnusedReturnValue")
	private PoolingDataSource processXa(ConnectionBaseInfo cbi, PoolingDataSource pds)
	{
		if (cbi.getClassName() != null)
		{
			pds.setClassName(cbi.getClassName());
		}

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
		getCustomProperties().forEach((a,b) -> pds.getDriverProperties()
	                                          .setProperty(a, b));
		return pds;
	}

	/**
	 * Method processNonXa ...
	 *
	 * @param cbi
	 * 		of type ConnectionBaseInfo
	 * @param pds
	 * 		of type PoolingDataSource
	 *
	 * @return PoolingDataSource
	 */
	@SuppressWarnings("UnusedReturnValue")
	private PoolingDataSource processNonXa(ConnectionBaseInfo cbi, PoolingDataSource pds)
	{
		if (cbi.getTransactionIsolation() != null)
		{
			pds.setIsolationLevel(cbi.getTransactionIsolation());
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
		/*if (cbi.getInstanceName() != null)
		{
			pds.getDriverProperties()
			   .setProperty(cbi.getServerInstanceNameProperty(), cbi.getInstanceName());
		}*/
		getCustomProperties().forEach((a,b) -> pds.getDriverProperties()
		                                          .setProperty(a, b));
		return pds;
	}

	/**
	 * Method clone ...
	 *
	 * @return Object
	 *
	 * @throws CloneNotSupportedException
	 * 		when
	 */
	@Override
	protected BTMConnectionBaseInfo clone() throws CloneNotSupportedException
	{
		return (BTMConnectionBaseInfo) super.clone();
	}
}
