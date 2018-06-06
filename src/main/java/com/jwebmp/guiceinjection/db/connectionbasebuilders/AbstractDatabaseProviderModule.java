package com.jwebmp.guiceinjection.db.connectionbasebuilders;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.jwebmp.guiceinjection.annotations.GuiceInjectorModuleMarker;
import com.jwebmp.guiceinjection.db.ConnectionBaseInfo;
import com.jwebmp.guiceinjection.db.JpaPersistPrivateModule;
import com.jwebmp.guiceinjection.db.JtaPoolDataSource;
import com.jwebmp.guiceinjection.db.PersistenceFileHandler;
import com.jwebmp.guiceinjection.exceptions.NoConnectionInfoException;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.Persistence;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Properties;
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
	protected abstract ConnectionBaseInfo getConnectionBaseInfo(Persistence.PersistenceUnit unit, Properties filteredProperties);

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
		Properties jdbcProperties = null;
		jdbcProperties = getJDBCPropertiesMap();
		Persistence.PersistenceUnit pu = getPersistenceUnit();
		if (pu == null)
		{
			log.severe("Unable to register persistence unit with name " + getPersistenceUnitName() + " - No persistence unit containing this " + "" + "" + "" + "name was found.");
			return;
		}
		install(new JpaPersistPrivateModule(getPersistenceUnitName(), jdbcProperties, getBindingAnnotation()));

		ConnectionBaseInfo connectionBaseInfo = getConnectionBaseInfo(pu, jdbcProperties);
		connectionBaseInfo.setJndiName(getJndiMapping());

		bind(getDataSourceKey()).toProvider(() -> provideDataSource(connectionBaseInfo))
		                        .in(Singleton.class);
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
		Persistence.PersistenceUnit pu = getPersistenceUnit();
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

	@NotNull
	private DataSource provideDataSource(ConnectionBaseInfo cbi)
	{
		if (cbi == null)
		{
			throw new NoConnectionInfoException("Not point in trying to create a connection with no info.....");
		}
		JtaPoolDataSource dataSource = new JtaPoolDataSource();
		dataSource.configure(cbi);
		return dataSource.get();
	}

	/**
	 * Returns the persistence unit associated with the supplied name
	 *
	 * @return
	 */
	protected Persistence.PersistenceUnit getPersistenceUnit()
	{
		for (Persistence.PersistenceUnit pu : PersistenceFileHandler.getPersistenceUnits())
		{
			if (pu.getName()
			      .equals(getPersistenceUnitName()))
			{
				return pu;
			}
		}
		log.log(Level.SEVERE, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]");
		return null;
	}

	/**
	 * Builds a property map from a persistence unit properties file
	 *
	 * @param pu
	 * @param jdbcProperties
	 */
	private void configurePersistenceUnitProperties(Persistence.PersistenceUnit pu, Properties jdbcProperties)
	{
		Properties sysProps = System.getProperties();
		if (pu != null)
		{
			for (Persistence.PersistenceUnit.Properties.Property props : pu.getProperties()
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
