package com.jwebmp.guicedpersistence.db;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedpersistence.services.IPropertiesConnectionInfoReader;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.PersistenceUnit;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

/**
 * This class is a basic container (mirror) for the database jtm builder string.
 * Exists to specify the default properties for connections that a jtm should implement should btm be switched for a different
 * implementation
 */
@JsonAutoDetect(fieldVisibility = ANY,
		getterVisibility = NONE,
		setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ConnectionBaseInfo
		implements Cloneable
{
	/**
	 * The persistence unit name applied to this cbi
	 */
	private String persistenceUnitName;
	/**
	 * If this is running in XA mode
	 */
	private boolean xa;
	/**
	 * The URL to connect to
	 */
	private String url;
	private String serverName;
	private String port;
	private String instanceName;
	private String driver;
	private String driverClass;
	private String username;
	@JsonIgnore
	private String password;

	private String transactionIsolation;

	private String databaseName;
	private String jndiName;
	private String jdbcIdentifier;

	private Integer minPoolSize = 1;
	private Integer maxPoolSize = 5;
	private Integer maxIdleTime;
	private Integer maxLifeTime;
	private Integer preparedStatementCacheSize;

	private Boolean prefill = false;
	private Boolean useStrictMin = false;

	private Integer acquireIncrement;
	private Integer acquisitionInterval;
	private Integer acquisitionTimeout;

	private Boolean allowLocalTransactions;
	private Boolean applyTransactionTimeout;
	private Boolean automaticEnlistingEnabled;
	private Boolean enableJdbc4ConnectionTest;
	private Boolean ignoreRecoveryFailures;
	private Boolean shareTransactionConnections;

	private String testQuery;

	private String serverInstanceNameProperty;

	public ConnectionBaseInfo()
	{
		//No config needed
		serverInstanceNameProperty = "Instance";
	}

	public ConnectionBaseInfo populateFromProperties(PersistenceUnit unit, Properties filteredProperties)
	{
		for (IPropertiesConnectionInfoReader connectionInfoReader : GuiceContext.instance()
		                                                                        .getLoader(IPropertiesConnectionInfoReader.class, true, ServiceLoader.load(
				                                                                       IPropertiesConnectionInfoReader.class)))
		{
			connectionInfoReader.populateConnectionBaseInfo(unit, filteredProperties, this);
		}
		return this;
	}

	/**
	 * Returns the BTM Pooling Data Source Configured
	 *
	 * @return
	 */
	public abstract DataSource toPooledDatasource();

	/**
	 * Gets the transaction isolation
	 *
	 * @return
	 */
	public String getTransactionIsolation()
	{
		return transactionIsolation;
	}

	/**
	 * Sets the transaction isolation
	 *
	 * @param transactionIsolation
	 */
	public ConnectionBaseInfo setTransactionIsolation(String transactionIsolation)
	{
		this.transactionIsolation = transactionIsolation;
		return this;
	}

	/**
	 * Gets the minimum pool size
	 *
	 * @return
	 */
	public Integer getMinPoolSize()
	{
		return minPoolSize;
	}

	/**
	 * Sets the minimum pool size
	 *
	 * @param minPoolSize
	 *
	 * @return
	 */
	public ConnectionBaseInfo setMinPoolSize(Integer minPoolSize)
	{
		this.minPoolSize = minPoolSize;
		return this;
	}

	/**
	 * Sets the maximum pool size
	 *
	 * @return
	 */
	public Integer getMaxPoolSize()
	{
		return maxPoolSize;
	}

	/**
	 * Sets the maximum pool size
	 *
	 * @param maxPoolSize
	 *
	 * @return
	 */
	public ConnectionBaseInfo setMaxPoolSize(Integer maxPoolSize)
	{
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	/**
	 * When the pool is above the minPoolSize, this parameter controls how long (in seconds) an idle connection will stay in the pool
	 * before being retired. However, even if a connection is idle for longer than maxIdleTime, the connection will not be retired if it
	 * would bring the pool below the minPoolSize. Default value: 60.
	 *
	 * @return
	 */
	public Integer getMaxIdleTime()
	{
		return maxIdleTime;
	}

	/**
	 * When the pool is above the minPoolSize, this parameter controls how long (in seconds) an idle connection will stay in the pool
	 * before being retired. However, even if a connection is idle for longer than maxIdleTime, the connection will not be retired if it
	 * would bring the pool below the minPoolSize. Default value: 60.
	 *
	 * @param maxIdleTime
	 */
	public ConnectionBaseInfo setMaxIdleTime(Integer maxIdleTime)
	{
		this.maxIdleTime = maxIdleTime;
		return this;
	}

	/**
	 * Controls how many Prepared Statements are cached (per connection) by BTM. A value of 0 means that statement caching is disabled.
	 * Default value: 0.
	 *
	 * @return
	 */
	public Integer getPreparedStatementCacheSize()
	{
		return preparedStatementCacheSize;
	}

	/**
	 * Controls how many Prepared Statements are cached (per connection) by BTM. A value of 0 means that statement caching is disabled.
	 * Default value: 0.
	 *
	 * @param preparedStatementCacheSize
	 */
	public ConnectionBaseInfo setPreparedStatementCacheSize(Integer preparedStatementCacheSize)
	{
		this.preparedStatementCacheSize = preparedStatementCacheSize;
		return this;
	}

	/**
	 * This parameter controls how many connections are filled into the pool when the pool is empty but the maxPoolSize has not been
	 * reached. If there aren't enough connections in the pool to fulfill a request, new connections will be created, by increments of
	 * acquireIncrement at a time. Default value: 1.
	 *
	 * @return
	 */
	public Integer getAcquireIncrement()
	{
		return acquireIncrement;
	}

	/**
	 * This parameter controls how many connections are filled into the pool when the pool is empty but the maxPoolSize has not been
	 * reached. If there aren't enough connections in the pool to fulfill a request, new connections will be created, by increments of
	 * acquireIncrement at a time. Default value: 1.
	 *
	 * @param acquireIncrement
	 *
	 * @return
	 */
	public ConnectionBaseInfo setAcquireIncrement(Integer acquireIncrement)
	{
		this.acquireIncrement = acquireIncrement;
		return this;
	}

	/**
	 * This parameter controls how long (in seconds) the pool waits between attempts to create new connections. This time in included
	 * within in the acquisitionTimeout. Default value: 1.
	 *
	 * @return
	 */
	public Integer getAcquisitionInterval()
	{
		return acquisitionInterval;
	}

	/**
	 * This parameter controls how long (in seconds) the pool waits between attempts to create new connections. This time in included
	 * within in the acquisitionTimeout. Default value: 1.
	 *
	 * @param acquisitionInterval
	 *
	 * @return
	 */
	public ConnectionBaseInfo setAcquisitionInterval(Integer acquisitionInterval)
	{
		this.acquisitionInterval = acquisitionInterval;
		return this;
	}

	/**
	 * This parameter controls how long (in seconds) a request for a connection from the pool will block before being aborted (and an
	 * exception thrown). Default value: 30.
	 *
	 * @return
	 */
	public Integer getAcquisitionTimeout()
	{
		return acquisitionTimeout;
	}

	/**
	 * This parameter controls how long (in seconds) a request for a connection from the pool will block before being aborted (and an
	 * exception thrown). Default value: 30.
	 *
	 * @param acquisitionTimeout
	 *
	 * @return
	 */
	public ConnectionBaseInfo setAcquisitionTimeout(Integer acquisitionTimeout)
	{
		this.acquisitionTimeout = acquisitionTimeout;
		return this;
	}

	/**
	 * This parameter is used to adjust whether or not you want to be able to run SQL statements outside of XA transactions scope.
	 * Defaults value: false.
	 *
	 * @return
	 */
	public Boolean getAllowLocalTransactions()
	{
		return allowLocalTransactions;
	}

	/**
	 * This parameter is used to adjust whether or not you want to be able to run SQL statements outside of XA transactions scope.
	 * Defaults value: false.
	 *
	 * @param allowLocalTransactions
	 *
	 * @return
	 */
	public ConnectionBaseInfo setAllowLocalTransactions(Boolean allowLocalTransactions)
	{
		this.allowLocalTransactions = allowLocalTransactions;
		return this;
	}

	/**
	 * Should the transaction timeout be passed to the resource via XAResource.setTransactionTimeout()? Default value: false.
	 *
	 * @return
	 */
	public Boolean getApplyTransactionTimeout()
	{
		return applyTransactionTimeout;
	}

	/**
	 * Should the transaction timeout be passed to the resource via XAResource.setTransactionTimeout()? Default value: false.
	 *
	 * @param applyTransactionTimeout
	 *
	 * @return
	 */
	public ConnectionBaseInfo setApplyTransactionTimeout(Boolean applyTransactionTimeout)
	{
		this.applyTransactionTimeout = applyTransactionTimeout;
		return this;
	}

	/**
	 * This parameter controls whether connections from the PoolingDataSource are automatically enlisted/delisted into the XA transactions
	 * . If this is set to false then have to enlist XAResource objects manually into the Transaction objects for them to participate in
	 * XA transactions. Default value: true.
	 *
	 * @return
	 */
	public Boolean getAutomaticEnlistingEnabled()
	{
		return automaticEnlistingEnabled;
	}

	/**
	 * This parameter controls whether connections from the PoolingDataSource are automatically enlisted/delisted into the XA transactions
	 * . If this is set to false then have to enlist XAResource objects manually into the Transaction objects for them to participate in
	 * XA transactions. Default value: true.
	 *
	 * @param automaticEnlistingEnabled
	 *
	 * @return
	 */
	public ConnectionBaseInfo setAutomaticEnlistingEnabled(Boolean automaticEnlistingEnabled)
	{
		this.automaticEnlistingEnabled = automaticEnlistingEnabled;
		return this;
	}

	/**
	 * If your JDBC driver supports JDBC4, this method of testing the connection is likely much more efficient than using the testQuery
	 * parameter. In the case of a testQuery, the query must be sent to the DB server, parsed, and executed before the connection can be
	 * used. JDBC4 exposes a method by which a driver can make its own determination of connectivity (possibly whether the socket is still
	 * connected, etc.). Default value: false.
	 *
	 * @return
	 */
	public Boolean getEnableJdbc4ConnectionTest()
	{
		return enableJdbc4ConnectionTest;
	}

	/**
	 * If your JDBC driver supports JDBC4, this method of testing the connection is likely much more efficient than using the testQuery
	 * parameter. In the case of a testQuery, the query must be sent to the DB server, parsed, and executed before the connection can be
	 * used. JDBC4 exposes a method by which a driver can make its own determination of connectivity (possibly whether the socket is still
	 * connected, etc.). Default value: false.
	 *
	 * @param enableJdbc4ConnectionTest
	 *
	 * @return
	 */
	public ConnectionBaseInfo setEnableJdbc4ConnectionTest(Boolean enableJdbc4ConnectionTest)
	{
		this.enableJdbc4ConnectionTest = enableJdbc4ConnectionTest;
		return this;
	}

	/**
	 * Should recovery errors be ignored? Ignoring recovery errors jeopardizes the failed transactions atomicity so only set this
	 * parameter to true when you know what you're doing. This is mostly useful in a development environment. Default value: false.
	 *
	 * @return
	 */
	public Boolean getIgnoreRecoveryFailures()
	{
		return ignoreRecoveryFailures;
	}

	/**
	 * Should recovery errors be ignored? Ignoring recovery errors jeopardizes the failed transactions atomicity so only set this
	 * parameter to true when you know what you're doing. This is mostly useful in a development environment. Default value: false.
	 *
	 * @param ignoreRecoveryFailures
	 *
	 * @return
	 */
	public ConnectionBaseInfo setIgnoreRecoveryFailures(Boolean ignoreRecoveryFailures)
	{
		this.ignoreRecoveryFailures = ignoreRecoveryFailures;
		return this;
	}

	/**
	 * By default, whenever a thread requests a connection from the DataSource, BTM will issue a new connection. All connections issued
	 * are bound into the same transaction context. Depending on the design of the user's application, this behavior can result in a large
	 * number of connections to the database -- and in the case of a database such as PostgreSQL, which uses one process per-connection
	 * this places a fairly heavy burden on the database. Setting this option to true will enable a thread-associated connection cache.
	 * With this option enabled, no matter how many times a thread requests a connection from the DataSource, BTM will return a single
	 * connection. Because connections can be shared within the context of a transaction, this provides a more efficient use of connection
	 * resources. A positive benefit of a single connection per thread is that the prepared statement cache (which is per-connection) is
	 * also made more efficient. Lastly, another benefit is that because connections are shared within the same thread, the overhead of
	 * establishing and testing a new connection to the database is avoided, which significantly improves the performance of some access
	 * patterns. Of course, BTM will still ensure correctness whenever this parameter is set to true. While the default value of this
	 * property is false for backward compatibility, the recommended setting is true. Default value: false.
	 *
	 * @return
	 */
	public Boolean getShareTransactionConnections()
	{
		return shareTransactionConnections;
	}

	/**
	 * By default, whenever a thread requests a connection from the DataSource, BTM will issue a new connection. All connections issued
	 * are bound into the same transaction context. Depending on the design of the user's application, this behavior can result in a large
	 * number of connections to the database -- and in the case of a database such as PostgreSQL, which uses one process per-connection
	 * this places a fairly heavy burden on the database. Setting this option to true will enable a thread-associated connection cache.
	 * With this option enabled, no matter how many times a thread requests a connection from the DataSource, BTM will return a single
	 * connection. Because connections can be shared within the context of a transaction, this provides a more efficient use of connection
	 * resources. A positive benefit of a single connection per thread is that the prepared statement cache (which is per-connection) is
	 * also made more efficient. Lastly, another benefit is that because connections are shared within the same thread, the overhead of
	 * establishing and testing a new connection to the database is avoided, which significantly improves the performance of some access
	 * patterns. Of course, BTM will still ensure correctness whenever this parameter is set to true. While the default value of this
	 * property is false for backward compatibility, the recommended setting is true. Default value: false.
	 *
	 * @param shareTransactionConnections
	 *
	 * @return
	 */
	public ConnectionBaseInfo setShareTransactionConnections(Boolean shareTransactionConnections)
	{
		this.shareTransactionConnections = shareTransactionConnections;
		return this;
	}

	/**
	 * This parameters sets the SQL statement that is used to test whether a connection is still alive before returning it from the
	 * connection pool.
	 *
	 * @return
	 */
	public String getTestQuery()
	{
		return testQuery;
	}

	/**
	 * This parameters sets the SQL statement that is used to test whether a connection is still alive before returning it from the
	 * connection pool.
	 *
	 * @param testQuery
	 *
	 * @return
	 */
	public ConnectionBaseInfo setTestQuery(String testQuery)
	{
		this.testQuery = testQuery;
		return this;
	}

	/**
	 * Gets the jndi name
	 *
	 * @return
	 */
	public String getJndiName()
	{
		return jndiName;
	}

	/**
	 * Sets the jndi name
	 *
	 * @param jndiName
	 */
	public ConnectionBaseInfo setJndiName(String jndiName)
	{
		this.jndiName = jndiName;
		return this;
	}

	/**
	 * Gets a driver class
	 *
	 * @return
	 */
	public String getDriverClass()
	{
		return driverClass;
	}

	/**
	 * Sets a driver class
	 *
	 * @param driverClass
	 */
	public ConnectionBaseInfo setDriverClass(String driverClass)
	{
		this.driverClass = driverClass;
		return this;
	}

	/**
	 * If the connection is an XA resource
	 *
	 * @return
	 */
	public boolean isXa()
	{
		return xa;
	}

	/**
	 * If the connection ins an XA Resource
	 *
	 * @param xa
	 */
	public ConnectionBaseInfo setXa(boolean xa)
	{
		this.xa = xa;
		return this;
	}

	/**
	 * Returns a provided URL
	 *
	 * @return
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Sets a provided URL
	 *
	 * @param url
	 */
	public ConnectionBaseInfo setUrl(String url)
	{
		this.url = url;
		return this;
	}

	/**
	 * Returns the server name
	 *
	 * @return
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * Sets the server name
	 *
	 * @param serverName
	 */
	public ConnectionBaseInfo setServerName(String serverName)
	{
		this.serverName = serverName;
		return this;
	}

	/**
	 * Returns the port
	 *
	 * @return
	 */
	public String getPort()
	{
		return port;
	}

	/**
	 * Sets the port
	 *
	 * @param port
	 */
	public ConnectionBaseInfo setPort(String port)
	{
		this.port = port;
		return this;
	}

	/**
	 * Gets the instance name
	 *
	 * @return
	 */
	public String getInstanceName()
	{
		return instanceName;
	}

	/**
	 * Sets the instance name
	 *
	 * @param instanceName
	 */
	public ConnectionBaseInfo setInstanceName(String instanceName)
	{
		this.instanceName = instanceName;
		return this;
	}

	/**
	 * Gets a driver
	 *
	 * @return
	 */
	public String getDriver()
	{
		return driver;
	}

	/**
	 * Sets a driver
	 *
	 * @param driver
	 */
	public ConnectionBaseInfo setDriver(String driver)
	{
		this.driver = driver;
		return this;
	}

	/**
	 * Gets a username
	 *
	 * @return
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * Sets a user name
	 *
	 * @param username
	 */
	public ConnectionBaseInfo setUsername(String username)
	{
		this.username = username;
		return this;
	}

	/**
	 * Gets a password
	 *
	 * @return
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Sets a password
	 *
	 * @param password
	 */
	public ConnectionBaseInfo setPassword(String password)
	{
		this.password = password;
		return this;
	}

	/**
	 * Gets the database name
	 *
	 * @return
	 */
	public String getDatabaseName()
	{
		return databaseName;
	}

	/**
	 * Sets the database name
	 *
	 * @param databaseName
	 */
	public ConnectionBaseInfo setDatabaseName(String databaseName)
	{
		this.databaseName = databaseName;
		return this;
	}

	/**
	 * Gets the jdbc private identifier
	 *
	 * @return
	 */
	public String getJdbcIdentifier()
	{
		return jdbcIdentifier;
	}

	/**
	 * Sets the jdbc private identifier
	 *
	 * @param jdbcIdentifier
	 *
	 * @return
	 */
	public ConnectionBaseInfo setJdbcIdentifier(String jdbcIdentifier)
	{
		this.jdbcIdentifier = jdbcIdentifier;
		return this;
	}

	/**
	 * Gets the service instance name property to use
	 *
	 * @return
	 */
	public String getServerInstanceNameProperty()
	{
		return serverInstanceNameProperty;
	}

	/**
	 * Sets the instance property name to use to specify the instance
	 *
	 * @param serverInstanceNameProperty
	 *
	 * @return
	 */
	public ConnectionBaseInfo setServerInstanceNameProperty(String serverInstanceNameProperty)
	{
		this.serverInstanceNameProperty = serverInstanceNameProperty;
		return this;
	}

	/**
	 * Sets the prefill
	 *
	 * @return
	 */
	public Boolean getPrefill()
	{
		return prefill;
	}

	/**
	 * If the connection pool must prefil
	 *
	 * @param prefill
	 *
	 * @return
	 */
	public ConnectionBaseInfo setPrefill(Boolean prefill)
	{
		this.prefill = prefill;
		return this;
	}

	/**
	 * If the minimum number of connections is strictly defined
	 *
	 * @return
	 */
	public Boolean getUseStrictMin()
	{
		return useStrictMin;
	}

	/**
	 * Sets to use strict minimum connections
	 *
	 * @param useStrictMin
	 *
	 * @return
	 */
	public ConnectionBaseInfo setUseStrictMin(Boolean useStrictMin)
	{
		this.useStrictMin = useStrictMin;
		return this;
	}

	/**
	 * This parameter controls how long (in seconds) a connection is allowed to live in the pool regardless of the minPoolSize parameter
	 * value. If a connection exceeds this time, and it is not in use, it will be retired from the pool. If the retirement of a connection
	 * causes the pool to dip below the minPoolSize, it will be immediately replaced with a new connection. This setting can be used to
	 * avoid unexpected disconnect due to database-side connection timeout. It is also useful to avoid leaks and release resources held on
	 * the database-side for open connections. Default value: 0.
	 *
	 * @return
	 */
	public Integer getMaxLifeTime()
	{
		return maxLifeTime;
	}

	/**
	 * This parameter controls how long (in seconds) a connection is allowed to live in the pool regardless of the minPoolSize parameter
	 * value. If a connection exceeds this time, and it is not in use, it will be retired from the pool. If the retirement of a connection
	 * causes the pool to dip below the minPoolSize, it will be immediately replaced with a new connection. This setting can be used to
	 * avoid unexpected disconnect due to database-side connection timeout. It is also useful to avoid leaks and release resources held on
	 * the database-side for open connections. Default value: 0.
	 *
	 * @param maxLifeTime
	 *
	 * @return
	 */
	public ConnectionBaseInfo setMaxLifeTime(Integer maxLifeTime)
	{
		this.maxLifeTime = maxLifeTime;
		return this;
	}

	public String getPersistenceUnitName()
	{
		return persistenceUnitName;
	}

	public ConnectionBaseInfo setPersistenceUnitName(String persistenceUnitName)
	{
		this.persistenceUnitName = persistenceUnitName;
		return this;
	}

	@JsonProperty("password")
	private String passwordProperty()
	{
		return password == null ? "Empty" : "*********";
	}

	@Override
	public String toString()
	{
		try
		{
			return new ObjectMapper().writeValueAsString(this);
		}
		catch (JsonProcessingException e)
		{
			LogFactory.getLog("ConnectionBaseInfo")
			          .log(Level.SEVERE, "Cannot render ConnectionBaseInfo", e);
			return "Unable to render";
		}
	}
}
