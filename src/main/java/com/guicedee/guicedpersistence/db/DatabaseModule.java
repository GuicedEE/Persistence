package com.guicedee.guicedpersistence.db;

import com.google.inject.PrivateModule;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.jta.JtaPersistModule;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;
import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.extern.java.Log;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceXmlParser;

import java.util.*;
import java.util.logging.Level;

/**
 * An abstract implementation for persistence.xml
 * <p>
 * Configuration conf = TransactionManagerServices.getConfiguration(); can be used to configure the transaction manager.
 */
@Log
public abstract class DatabaseModule<J extends DatabaseModule<J>>
        extends PrivateModule
        implements IGuiceModule<J> {

    private static final List<PersistenceUnitDescriptor> parsedPersistenceXmlDescriptors = new ArrayList<>();

    /**
     * Constructor DatabaseModule creates a new DatabaseModule instance.
     */
    public DatabaseModule() {

        var parser = PersistenceXmlParser.create(Map.of(), null, null);
        var urls = parser.getClassLoaderService().locateResources("META-INF/persistence.xml");
        if (urls.isEmpty()) {
            return;
        }
        parsedPersistenceXmlDescriptors.addAll(parser.parse(urls).values());
        for (var desc : parsedPersistenceXmlDescriptors) {
            System.out.println("PU Found : " + desc.getName());
        }
    }


    /**
     * Configures the module with the bindings
     */
    @Override
    protected void configure() {
        DatabaseModule.log.config("Loading Database Module - " + getClass().getName() + " - " + getPersistenceUnitName());
        Properties jdbcProperties = getJDBCPropertiesMap();
        var pu = getPersistenceUnit();
        if (pu == null) {
            DatabaseModule.log
                    .severe("Unable to register persistence unit with name " + getPersistenceUnitName() + " - No persistence unit containing this name was found.");
            return;
        }
        for (IPropertiesEntityManagerReader entityManagerReader : IGuiceContext
                .instance()
                .getLoader(IPropertiesEntityManagerReader.class, true,
                        ServiceLoader.load(IPropertiesEntityManagerReader.class))) {
            if (!entityManagerReader.applicable(pu)) {
                continue;
            }
            Map<String, String> output = entityManagerReader.processProperties(pu, jdbcProperties);
            if (output != null && !output.isEmpty()) {
                jdbcProperties.putAll(output);
            }
        }
        try {
            ConnectionBaseInfo connectionBaseInfo = getConnectionBaseInfo(pu, jdbcProperties);
            connectionBaseInfo.populateFromProperties(pu, jdbcProperties);

            if (connectionBaseInfo.getJndiName() == null) {
                connectionBaseInfo.setJndiName(getJndiMapping());
            }
            log.fine(String.format("%s - Connection Base Info Final - %s",
                    getPersistenceUnitName(), connectionBaseInfo));
            connectionBaseInfo.setPersistenceUnitName(getPersistenceUnitName());
            JtaPersistModule jpaModule = new JtaPersistModule(getPersistenceUnitName(), connectionBaseInfo);
            jpaModule.properties(jdbcProperties);
            PersistenceServicesModule.getConnectionModules().put(connectionBaseInfo, jpaModule);
        } catch (Throwable T) {
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
    /*

     */
/**
 * Returns the persistence unit associated with the supplied name
 *
 * @return The given persistence unit
 *//*

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
*/

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
    protected abstract ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnitDescriptor unit, Properties filteredProperties);

    private PersistenceUnitDescriptor getPersistenceUnit() {
        for (var parsedPersistenceXmlDescriptor : parsedPersistenceXmlDescriptors) {
            if (parsedPersistenceXmlDescriptor.getName()
                    .equals(getPersistenceUnitName())) {
                return parsedPersistenceXmlDescriptor;
            }
        }
        return null;
    }

    /**
     * A properties map of the properties from the file
     *
     * @return A properties map of the given persistence units properties
     */
    @NotNull
    private Properties getJDBCPropertiesMap() {
        Properties jdbcProperties = new Properties();
        configurePersistenceUnitProperties(getPersistenceUnit(), jdbcProperties);
        return jdbcProperties;
    }

    /**
     * The name found in jta-data-source from the persistence.xml
     *
     * @return The JNDI mapping name to use
     */
    protected String getJndiMapping() {
        return null;
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
    protected void configurePersistenceUnitProperties(PersistenceUnitDescriptor pu, Properties jdbcProperties) {
        if (pu != null) {
            try {
                for (Object o : pu.getProperties().keySet()) {
                    String key = o.toString();
                    String value = pu.getProperties().get(o).toString();
                    jdbcProperties.put(key, value);
                }
            } catch (Throwable t) {
                log.log(Level.SEVERE, "Unable to load persistence unit properties for [" + pu.getName() + "]", t);
            }
        }
    }

    @Override
    public Integer sortOrder() {
        return 50;
    }
}
