package com.jwebmp.guicedpersistence.db.connectionbasebuilders;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.jwebmp.guicedinjection.annotations.GuiceInjectorModuleMarker;
import com.jwebmp.guicedpersistence.db.ConnectionBaseInfo;
import com.jwebmp.guicedpersistence.db.PersistenceFileHandler;
import com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;
import com.jwebmp.guicedpersistence.db.exceptions.NoConnectionInfoException;
import com.jwebmp.guicedpersistence.injectors.JpaPersistPrivateModule;
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
@GuiceInjectorModuleMarker
public abstract class AbstractDatabaseProviderModule
		extends AbstractModule
{
	private static final Logger log = LogFactory.getLog("AbstractDatabaseProviderModule");

	@SuppressWarnings("unchecked")
	public AbstractDatabaseProviderModule()
	{
	}

	/**
	 * Builds up connection base data info from a persistence unit.
	 * <p>
	 * Use with the utility methods e.g.
	 *
	 * @param unit
	 *
	 * @return
	 */
	@NotNull
	protected abstract ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties);

	/**
	 * The name found in jta-data-source from the persistence.xml
	 *
	 * @return
	 */
	@NotNull
	protected abstract String getJndiMapping();

	/**
	 * The name found in persistence.xml
	 *
	 * @return
	 */
	@NotNull
	protected abstract String getPersistenceUnitName();

	/**
	 * Configures the module with the bindings
	 */
	@Override
	protected void configure()
	{
		log.config(getPersistenceUnitName() + " Is Binding");
		Properties jdbcProperties = getJDBCPropertiesMap();
		PersistenceUnit pu = getPersistenceUnit();
		if (pu == null)
		{
			log.severe("Unable to register persistence unit with name " + getPersistenceUnitName() + " - No persistence unit containing this name was found.");
			return;
		}
		ServiceLoader<PropertiesEntityManagerReader> entityManagerReaders = ServiceLoader.load(PropertiesEntityManagerReader.class);
		for (PropertiesEntityManagerReader entityManagerReader : entityManagerReaders)
		{
			Map<String, String> output = entityManagerReader.processProperties(jdbcProperties);
			if (output != null && !output.isEmpty())
			{
				jdbcProperties.putAll(output);
			}
		}

		ConnectionBaseInfo connectionBaseInfo = getConnectionBaseInfo(pu, jdbcProperties);
		connectionBaseInfo.populateFromProperties(pu, jdbcProperties);
		connectionBaseInfo.setJndiName(getJndiMapping());

		install(new JpaPersistPrivateModule(getPersistenceUnitName(), jdbcProperties, getBindingAnnotation()));
		DataSource ds = provideDataSource(connectionBaseInfo);
		if (ds != null)
		{
			bind(getDataSourceKey()).toInstance(ds);
		}

		log.config(getPersistenceUnitName() + " Finished Binding.");
	}

	/**
	 * A properties map of the properties from the file
	 *
	 * @return
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
	 * Returns the generated key for the data source
	 *
	 * @return
	 */
	@NotNull
	protected Key<DataSource> getDataSourceKey()
	{
		return Key.get(DataSource.class, getBindingAnnotation());
	}

	/**
	 * Returns the key used for the entity manager
	 *
	 * @return
	 */
	@NotNull
	protected Key<EntityManager> getEntityManagerKey()
	{
		return Key.get(EntityManager.class, getBindingAnnotation());
	}

	/**
	 * The annotation which will identify this guy
	 *
	 * @return
	 */
	@NotNull
	protected abstract Class<? extends Annotation> getBindingAnnotation();

	/**
	 * Provides the given data source
	 *
	 * @param cbi
	 *
	 * @return
	 */
	private DataSource provideDataSource(ConnectionBaseInfo cbi)
	{
		if (cbi == null)
		{
			throw new NoConnectionInfoException("Not point in trying to create a connection with no info.....");
		}
		return cbi.toPooledDatasource();
	}

	/**
	 * Returns the persistence unit associated with the supplied name
	 *
	 * @return
	 */
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
			log.log(Level.SEVERE, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]");
		}
		log.log(Level.WARNING, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]. Returning a Null Instance");
		return null;
	}

	/**
	 * Builds a property map from a persistence unit properties file
	 *
	 * @param pu
	 * @param jdbcProperties
	 */
	private void configurePersistenceUnitProperties(PersistenceUnit pu, Properties jdbcProperties)
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
}
