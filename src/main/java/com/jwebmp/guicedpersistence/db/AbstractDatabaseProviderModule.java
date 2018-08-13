package com.jwebmp.guicedpersistence.db;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.jwebmp.guicedinjection.interfaces.IGuiceModule;
import com.jwebmp.guicedpersistence.db.exceptions.NoConnectionInfoException;
import com.jwebmp.guicedpersistence.injectors.JpaPersistPrivateModule;
import com.jwebmp.guicedpersistence.scanners.PersistenceFileHandler;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.PersistenceUnit;
import com.oracle.jaxb21.Property;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract implementation for persistence.xml
 * <p>
 * Configuration conf = TransactionManagerServices.getConfiguration(); can be used to configure the transaction manager.
 */
@SuppressWarnings("NullableProblems")
public abstract class AbstractDatabaseProviderModule
		extends AbstractModule
		implements IGuiceModule
{
	/**
	 * Field log
	 */
	private static final Logger log = LogFactory.getLog("AbstractDatabaseProviderModule");
	private static final ServiceLoader<PropertiesEntityManagerReader> propsLoader = ServiceLoader.load(PropertiesEntityManagerReader.class);

	/**
	 * Constructor AbstractDatabaseProviderModule creates a new AbstractDatabaseProviderModule instance.
	 */
	@SuppressWarnings("unchecked")
	public AbstractDatabaseProviderModule()
	{
		//Config required
	}

	/**
	 * Configures the module with the bindings
	 */
	@Override
	protected void configure()
	{
		AbstractDatabaseProviderModule.log.config("Loading Database Module - " + getClass().getCanonicalName() + " - " + getPersistenceUnitName());
		Properties jdbcProperties = getJDBCPropertiesMap();
		PersistenceUnit pu = getPersistenceUnit();
		if (pu == null)
		{
			AbstractDatabaseProviderModule.log.severe(
					"Unable to register persistence unit with name " + getPersistenceUnitName() + " - No persistence unit containing this name was found.");
			return;
		}
		for (PropertiesEntityManagerReader entityManagerReader : AbstractDatabaseProviderModule.propsLoader)
		{
			Map<String, String> output = entityManagerReader.processProperties(pu, jdbcProperties);
			if (output != null && !output.isEmpty())
			{
				jdbcProperties.putAll(output);
			}
		}

		ConnectionBaseInfo connectionBaseInfo = getConnectionBaseInfo(pu, jdbcProperties);
		connectionBaseInfo.populateFromProperties(pu, jdbcProperties);
		connectionBaseInfo.setJndiName(getJndiMapping());
		AbstractDatabaseProviderModule.log.fine("Connection Base Info Final - " + connectionBaseInfo);

		install(new JpaPersistPrivateModule(getPersistenceUnitName(), jdbcProperties, getBindingAnnotation()));
		DataSource ds = provideDataSource(connectionBaseInfo);
		if (ds != null)
		{
			AbstractDatabaseProviderModule.log.log(Level.FINE, "Bound DataSource.class with @" + getBindingAnnotation().getSimpleName());
			bind(getDataSourceKey()).toInstance(ds);
		}

		AbstractDatabaseProviderModule.log.log(Level.FINE, "Bound PersistenceUnit.class with @" + getBindingAnnotation().getSimpleName());
		bind(Key.get(PersistenceUnit.class, getBindingAnnotation())).toInstance(pu);
	}

	/**
	 * The name found in persistence.xml
	 *
	 * @return The persistence unit name to sear h
	 */
	@NotNull
	protected abstract String getPersistenceUnitName();

	/**
	 * Returns the persistence unit associated with the supplied name
	 *
	 * @return The given persistence unit
	 */
	@SuppressWarnings("WeakerAccess")
	protected PersistenceUnit getPersistenceUnit()
	{
		try
		{
			for (PersistenceUnit pu : PersistenceFileHandler.getPersistenceUnits())
			{
				if (pu.getName()
				      .equals(getPersistenceUnitName()))
				{
					return pu;
				}
			}
		}
		catch (Throwable T)
		{
			AbstractDatabaseProviderModule.log.log(Level.SEVERE, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]");
		}
		AbstractDatabaseProviderModule.log.log(Level.WARNING, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]. Returning a Null Instance");
		return null;
	}

	/**
	 * Builds up connection base data info from a persistence unit.
	 * <p>
	 * Use with the utility methods e.g.
	 *
	 * @param unit
	 * 		The physical persistence unit, changes have no effect the persistence ready
	 *
	 * @return The new connetion base info
	 */
	@NotNull
	protected abstract ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties);

	/**
	 * A properties map of the properties from the file
	 *
	 * @return A properties map of the given persistence units properties
	 */
	@NotNull
	private Properties getJDBCPropertiesMap()
	{
		Properties jdbcProperties = new Properties();
		PersistenceUnit pu = getPersistenceUnit();
		configurePersistenceUnitProperties(pu, jdbcProperties);
		return jdbcProperties;
	}

	/**
	 * The name found in jta-data-source from the persistence.xml
	 *
	 * @return The JNDI mapping name to use
	 */
	@NotNull
	protected abstract String getJndiMapping();

	/**
	 * Returns the generated key for the data source
	 *
	 * @return The key of the annotation and data source
	 */
	@SuppressWarnings("WeakerAccess")
	@NotNull
	protected Key<DataSource> getDataSourceKey()
	{
		return Key.get(DataSource.class, getBindingAnnotation());
	}

	/**
	 * Returns the key used for the entity manager
	 *
	 * @return The key for the entity manager and the annotation
	 */
	@SuppressWarnings("unused")
	@NotNull
	protected Key<EntityManager> getEntityManagerKey()
	{
		return Key.get(EntityManager.class, getBindingAnnotation());
	}

	/**
	 * The annotation which will identify this guy
	 *
	 * @return The annotation that will identify the given databsae
	 */
	@NotNull
	protected abstract Class<? extends Annotation> getBindingAnnotation();

	/**
	 * Builds a property map from a persistence unit properties file
	 * <p>
	 * Overwrites ${} items with system properties
	 *
	 * @param pu
	 * 		The persistence unit
	 * @param jdbcProperties
	 * 		The final properties map
	 */
	@SuppressWarnings("WeakerAccess")
	protected void configurePersistenceUnitProperties(PersistenceUnit pu, Properties jdbcProperties)
	{
		Properties sysProps = System.getProperties();
		if (pu != null)
		{
			for (Property props : pu.getProperties()
			                        .getProperty())
			{
				String checkProperty = props.getValue()
				                            .replace("\\$", "");
				checkProperty = checkProperty.replaceAll("\\{", "");
				checkProperty = checkProperty.replaceAll("}", "");
				if (sysProps.containsKey(checkProperty))
				{
					jdbcProperties.put(props.getName(), sysProps.get(checkProperty));
				}
				else
				{
					jdbcProperties.put(props.getName(), props.getValue());
				}
			}
		}
	}

	/**
	 * Provides the given data source
	 *
	 * @param cbi
	 * 		The connection base info required to generate a datasource
	 *
	 * @return The given data source or a NoConnectionInfo Exceptions
	 */
	private DataSource provideDataSource(ConnectionBaseInfo cbi)
	{
		if (cbi == null)
		{
			throw new NoConnectionInfoException("Not point in trying to create a connection with no info.....");
		}
		return cbi.toPooledDatasource();
	}
}
