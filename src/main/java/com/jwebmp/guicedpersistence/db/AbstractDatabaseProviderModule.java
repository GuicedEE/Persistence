package com.jwebmp.guicedpersistence.db;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.interfaces.IGuiceModule;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.guicedpersistence.injectors.JpaPersistPrivateModule;
import com.jwebmp.guicedpersistence.scanners.PersistenceFileHandler;
import com.jwebmp.guicedpersistence.services.PropertiesEntityManagerReader;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.PersistenceUnit;
import com.oracle.jaxb21.Property;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jwebmp.guicedpersistence.db.DbStartupThread.*;

/**
 * An abstract implementation for persistence.xml
 * <p>
 * Configuration conf = TransactionManagerServices.getConfiguration(); can be used to configure the transaction manager.
 */
public abstract class AbstractDatabaseProviderModule<J extends AbstractDatabaseProviderModule<J>>
		extends AbstractModule
		implements IGuiceModule<J>, IGuicePostStartup<J>
{
	/**
	 * Field log
	 */
	private static final Logger log = LogFactory.getLog("AbstractDatabaseProviderModule");

	/**
	 * A set of all annotations that this abstraction built
	 */
	private static final Set<Class<? extends Annotation>> boundAnnotations = new HashSet<>();

	/**
	 * Constructor AbstractDatabaseProviderModule creates a new AbstractDatabaseProviderModule instance.
	 */
	public AbstractDatabaseProviderModule()
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
		return AbstractDatabaseProviderModule.boundAnnotations;
	}

	/**
	 * Creates a DB Startup that will boot
	 */
	public boolean autoStart;

	/**
	 * Configures the module with the bindings
	 */
	@Override
	protected void configure()
	{
		AbstractDatabaseProviderModule.log.config("Loading Database Module - " + getClass().getName() + " - " + getPersistenceUnitName());
		Properties jdbcProperties = getJDBCPropertiesMap();
		PersistenceUnit pu = getPersistenceUnit();
		if (pu == null)
		{
			AbstractDatabaseProviderModule.log
					.severe("Unable to register persistence unit with name " + getPersistenceUnitName() + " - No persistence unit containing this name was found.");
			return;
		}
		for (PropertiesEntityManagerReader entityManagerReader : GuiceContext.instance()
		                                                                     .getLoader(PropertiesEntityManagerReader.class, true,
		                                                                                ServiceLoader.load(PropertiesEntityManagerReader.class)))
		{

			Map<String, String> output = entityManagerReader.processProperties(pu, jdbcProperties);
			if (output != null && !output.isEmpty())
			{
				jdbcProperties.putAll(output);
			}
		}
		ConnectionBaseInfo connectionBaseInfo = getConnectionBaseInfo(pu, jdbcProperties);
		connectionBaseInfo.populateFromProperties(pu, jdbcProperties);
		if (connectionBaseInfo.getJndiName() == null)
		{
			connectionBaseInfo.setJndiName(getJndiMapping());
		}
		AbstractDatabaseProviderModule.log.fine(getPersistenceUnitName() + " - Connection Base Info Final - " + connectionBaseInfo);

		install(new JpaPersistPrivateModule(getPersistenceUnitName(), jdbcProperties, getBindingAnnotation()));

		ConnectionBaseInfo ds;
		if (getLoadedDataSources().containsKey(getJndiMapping()))
		{
			ds = getLoadedDataSources().get(getJndiMapping());
			log.log(Level.CONFIG, "Re-Using Data Source for JNDI Mapping " + getJndiMapping());
		}
		else
		{
			ds = connectionBaseInfo;
			getLoadedDataSources().put(getJndiMapping(), connectionBaseInfo);
			if (isAutoStart())
			{
				DbStartupThread newPostStartup = new DbStartupThread(getBindingAnnotation());
				GuiceContext.instance()
				            .loadPostStartupServices()
				            .add(newPostStartup);
			}
		}
		if (ds != null)
		{
			AbstractDatabaseProviderModule.log.log(Level.FINE, "Bound DataSource.class with @" + getBindingAnnotation().getSimpleName());
			bind(getDataSourceKey()).toProvider(new DataSourceProvider(ds))
			                        .in(Singleton.class);
		}

		AbstractDatabaseProviderModule.log.log(Level.FINE, "Bound PersistenceUnit.class with @" + getBindingAnnotation().getSimpleName());
		bind(Key.get(PersistenceUnit.class, getBindingAnnotation())).toInstance(pu);
		AbstractDatabaseProviderModule.boundAnnotations.add(getBindingAnnotation());
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
			AbstractDatabaseProviderModule.log.log(Level.SEVERE, "Couldn't Find Persistence Unit for the given name [" + getPersistenceUnitName() + "]", T);
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
	protected void configurePersistenceUnitProperties(PersistenceUnit pu, Properties jdbcProperties)
	{
		if (pu != null)
		{
			for (Property props : pu.getProperties()
			                        .getProperty())
			{
				jdbcProperties.put(props.getName(), props.getValue());

			}
		}
	}

	/**
	 * Boots the persistence unit during post-load asynchronously
	 */
	@Override
	public void postLoad()
	{
		try
		{
			GuiceContext.get(DataSource.class, getBindingAnnotation());
		}
		catch (Throwable T)
		{
			LogFactory.getLog("DBStartup")
			          .log(Level.SEVERE, "Datasource Unable to start", T);
		}

		PersistService ps = GuiceContext.get(PersistService.class, getBindingAnnotation());
		ps.start();
		UnitOfWork ow = GuiceContext.get(UnitOfWork.class, getBindingAnnotation());
		ow.end();

		LogFactory.getLog("DBStartup")
		          .log(Level.CONFIG, "DB Post Startup Completed - " + getBindingAnnotation().getSimpleName());
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}

	/**
	 * Method isAutoStart returns the autoStart of this AbstractDatabaseProviderModule object.
	 * <p>
	 * Creates a DB Startup that will boot
	 *
	 * @return the autoStart (type boolean) of this AbstractDatabaseProviderModule object.
	 */
	public boolean isAutoStart()
	{
		return autoStart;
	}

	/**
	 * Method setAutoStart sets the autoStart of this AbstractDatabaseProviderModule object.
	 * <p>
	 * Creates a DB Startup that will boot
	 *
	 * @param autoStart
	 * 		the autoStart of this AbstractDatabaseProviderModule object.
	 *
	 * @return AbstractDatabaseProviderModule<J>
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public J setAutoStart(boolean autoStart)
	{
		this.autoStart = autoStart;
		return (J)this;
	}
}
