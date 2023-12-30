package com.guicedee.guicedpersistence.db;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.injectors.JpaPersistPrivateModule;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.extern.java.Log;
import org.hibernate.boot.archive.internal.PersistenceFileHandler;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.logging.Level;

/**
 * An abstract implementation for persistence.xml
 * <p>
 * Configuration conf = TransactionManagerServices.getConfiguration(); can be used to configure the transaction manager.
 */
@Log
public abstract class DatabaseModule<J extends DatabaseModule<J>>
		extends AbstractModule
		implements IGuiceModule<J>
{
	/**
	 * A set of all annotations that this abstraction built
	 */
	private static final Set<Class<? extends Annotation>> boundAnnotations = new HashSet<>();

	/**
	 * Constructor DatabaseModule creates a new DatabaseModule instance.
	 */
	public DatabaseModule()
	{
		//Config required
	}

	/**
	 * Returns a full list of all annotations that have bindings
	 *
	 * @return The set of all annotations that have bindings
	 */
	public static Set<Class<? extends Annotation>> getBoundAnnotations()
	{
		return DatabaseModule.boundAnnotations;
	}

	/**
	 * Configures the module with the bindings
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void configure()
	{
		DatabaseModule.log.config("Loading Database Module - " + getClass().getName() + " - " + getPersistenceUnitName());
		Properties jdbcProperties = getJDBCPropertiesMap();
		ParsedPersistenceXmlDescriptor pu = getPersistenceUnit();
		if (pu == null)
		{
			DatabaseModule.log
					.severe("Unable to register persistence unit with name " + getPersistenceUnitName() + " - No persistence unit containing this name was found.");
			return;
		}
		for (IPropertiesEntityManagerReader entityManagerReader : GuiceContext.instance()
		                                                                      .getLoader(IPropertiesEntityManagerReader.class, true,
		                                                                                 ServiceLoader.load(IPropertiesEntityManagerReader.class)))
		{
			if (!entityManagerReader.applicable(pu))
			{
				continue;
			}
			Map<String, String> output = entityManagerReader.processProperties(pu, jdbcProperties);
			if (output != null && !output.isEmpty())
			{
				jdbcProperties.putAll(output);
			}
		}
		try
		{
			ConnectionBaseInfo connectionBaseInfo = getConnectionBaseInfo(pu, jdbcProperties);
			connectionBaseInfo.populateFromProperties(pu, jdbcProperties);
			if (connectionBaseInfo.getJndiName() == null)
			{
				connectionBaseInfo.setJndiName(getJndiMapping());
			}
			log.fine(String.format("%s - Connection Base Info Final - %s",
			                       getPersistenceUnitName(), connectionBaseInfo));
			bind(Key.get(ParsedPersistenceXmlDescriptor.class, getBindingAnnotation())).toInstance(pu);
			JpaPersistPrivateModule jpaModule = new JpaPersistPrivateModule(getPersistenceUnitName(), jdbcProperties, getBindingAnnotation());
			jpaModule.setDefaultEntityManager(isDefault());
			PersistenceServicesModule.getModules()
			                         .put(getBindingAnnotation(),
					                         jpaModule);
			PersistenceServicesModule.getJtaConnectionBaseInfo()
			                         .put(getBindingAnnotation(), connectionBaseInfo);
		}
		catch (Throwable T)
		{
			log.log(Level.SEVERE, "Unable to load DB Module [" + pu.getName() + "] - " + T.getMessage(), T);
		}
	}

	/**
	 * The name found in persistence.xml
	 *
	 * @return The persistence unit name to sear h
	 */
	@NotNull
	protected abstract String getPersistenceUnitName();

	protected boolean isDefault()
	{
		return false;
	}
	
	/**
	 * Returns the persistence unit associated with the supplied name
	 *
	 * @return The given persistence unit
	 */
	protected ParsedPersistenceXmlDescriptor getPersistenceUnit()
	{
		try
		{
			for (ParsedPersistenceXmlDescriptor pu : PersistenceFileHandler.getPersistenceUnits())
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
			DatabaseModule.log.log(Level.SEVERE, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]", T);
		}
		DatabaseModule.log.log(Level.WARNING, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]. Returning a Null Instance");
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
	protected abstract ConnectionBaseInfo getConnectionBaseInfo(ParsedPersistenceXmlDescriptor unit, Properties filteredProperties);

	/**
	 * A properties map of the properties from the file
	 *
	 * @return A properties map of the given persistence units properties
	 */
	@NotNull
	private Properties getJDBCPropertiesMap()
	{
		Properties jdbcProperties = new Properties();
		ParsedPersistenceXmlDescriptor pu = getPersistenceUnit();
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
	@NotNull
	protected Key<DataSource> getDataSourceKey()
	{
		return Key.get(DataSource.class, getBindingAnnotation());
	}

	/**
	 * The annotation which will identify this guy
	 *
	 * @return The annotation that will identify the given databsae
	 */
	@NotNull
	protected abstract Class<? extends Annotation> getBindingAnnotation();

	/**
	 * Returns the key used for the entity manager
	 *
	 * @return The key for the entity manager and the annotation
	 */
	@NotNull
	protected Key<EntityManager> getEntityManagerKey()
	{
		return Key.get(EntityManager.class, getBindingAnnotation());
	}

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
	protected void configurePersistenceUnitProperties(ParsedPersistenceXmlDescriptor pu, Properties jdbcProperties)
	{
		if (pu != null)
		{
			try
			{
				for (Object o : pu.getProperties().keySet()) {
					String key = o.toString();
					String value = pu.getProperties().get(o).toString();
					jdbcProperties.put(key, value);
				}
			}
			catch (Throwable t)
			{
				log.log(Level.SEVERE, "Unable to load persistence unit properties for [" + pu.getName() + "]", t);
			}
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}

	public boolean isDataSourceAvailable()
	{
		return true;
	}
}
