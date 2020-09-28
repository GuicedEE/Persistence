package com.guicedee.guicedpersistence.btm.implementation;

import com.google.common.base.Strings;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class BTMConnectionProperties
		implements IPropertiesEntityManagerReader
{
	/**
	 * If the factory class property must be added to the persistence unit
	 */
	private static boolean factoryClass = true;
	/**
	 * If the session context must be added to the persistence unit
	 */
	private static boolean sessionContext = true;
	/**
	 * If the manager lookup must be added to the persistence unit
	 */
	private static boolean managerLookup = true;
	/**
	 * If the manager lookup property must be added to the persistence unit
	 */
	private static boolean jndiClass = true;
	/**
	 * If the transaction platform must be force set to Bitronix. Overcomes issues with JBoss Glassfish etc and transaction management
	 */
	private static boolean transactionPlatform = true;

	/**
	 * Method isFactoryClass returns the factoryClass of this BTMConnectionProperties object.
	 * <p>
	 * If the factory class property must be added to the persistence unit
	 *
	 * @return the factoryClass (type boolean) of this BTMConnectionProperties object.
	 */
	public static boolean isFactoryClass()
	{
		return factoryClass;
	}

	/**
	 * Method setFactoryClass sets the factoryClass of this BTMConnectionProperties object.
	 * <p>
	 * If the factory class property must be added to the persistence unit
	 *
	 * @param factoryClass
	 * 		the factoryClass of this BTMConnectionProperties object.
	 */
	public static void setFactoryClass(boolean factoryClass)
	{
		BTMConnectionProperties.factoryClass = factoryClass;
	}

	/**
	 * Method isSessionContext returns the sessionContext of this BTMConnectionProperties object.
	 * <p>
	 * If the session context must be added to the persistence unit
	 *
	 * @return the sessionContext (type boolean) of this BTMConnectionProperties object.
	 */
	public static boolean isSessionContext()
	{
		return sessionContext;
	}

	/**
	 * Method setSessionContext sets the sessionContext of this BTMConnectionProperties object.
	 * <p>
	 * If the session context must be added to the persistence unit
	 *
	 * @param sessionContext
	 * 		the sessionContext of this BTMConnectionProperties object.
	 */
	public static void setSessionContext(boolean sessionContext)
	{
		BTMConnectionProperties.sessionContext = sessionContext;
	}

	/**
	 * Method isManagerLookup returns the managerLookup of this BTMConnectionProperties object.
	 * <p>
	 * If the manager lookup must be added to the persistence unit
	 *
	 * @return the managerLookup (type boolean) of this BTMConnectionProperties object.
	 */
	public static boolean isManagerLookup()
	{
		return managerLookup;
	}

	/**
	 * Method setManagerLookup sets the managerLookup of this BTMConnectionProperties object.
	 * <p>
	 * If the manager lookup must be added to the persistence unit
	 *
	 * @param managerLookup
	 * 		the managerLookup of this BTMConnectionProperties object.
	 */
	public static void setManagerLookup(boolean managerLookup)
	{
		BTMConnectionProperties.managerLookup = managerLookup;
	}

	/**
	 * Method isJndiClass returns the jndiClass of this BTMConnectionProperties object.
	 * <p>
	 * If the manager lookup property must be added to the persistence unit
	 *
	 * @return the jndiClass (type boolean) of this BTMConnectionProperties object.
	 */
	public static boolean isJndiClass()
	{
		return jndiClass;
	}

	/**
	 * Method setJndiClass sets the jndiClass of this BTMConnectionProperties object.
	 * <p>
	 * If the manager lookup property must be added to the persistence unit
	 *
	 * @param jndiClass
	 * 		the jndiClass of this BTMConnectionProperties object.
	 */
	public static void setJndiClass(boolean jndiClass)
	{
		BTMConnectionProperties.jndiClass = jndiClass;
	}

	/**
	 * Method isTransactionPlatform returns the transactionPlatform of this BTMConnectionProperties object.
	 * <p>
	 * If the transaction platform must be force set to Bitronix. Overcomes issues with JBoss Glassfish etc and transaction management
	 *
	 * @return the transactionPlatform (type boolean) of this BTMConnectionProperties object.
	 */
	public static boolean isTransactionPlatform()
	{
		return transactionPlatform;
	}

	/**
	 * Method setTransactionPlatform sets the transactionPlatform of this BTMConnectionProperties object.
	 * <p>
	 * If the transaction platform must be force set to Bitronix. Overcomes issues with JBoss Glassfish etc and transaction management
	 *
	 * @param transactionPlatform
	 * 		the transactionPlatform of this BTMConnectionProperties object.
	 */
	public static void setTransactionPlatform(boolean transactionPlatform)
	{
		BTMConnectionProperties.transactionPlatform = transactionPlatform;
	}

	@Override
	public Map<String, String> processProperties(ParsedPersistenceXmlDescriptor persistenceUnit, Properties properties)
	{
		if ((persistenceUnit.getTransactionType() == null || "RESOURCE_LOCAL".equals(persistenceUnit.getTransactionType()
		                                                                                            .toString()))
		    && Strings.isNullOrEmpty(persistenceUnit.getJtaDataSource().toString()))
		{
			Logger.getLogger("BTMConnectionProperties")
			      .warning("Persistence Unit : " + persistenceUnit.getName() +
			               " is not a JTA resource and may skip BTM Configuration. Consider including C3P0 for these connections.");
		}
		Map<String, String> props = new HashMap<>();

		if (!Strings.isNullOrEmpty(persistenceUnit.getJtaDataSource().toString()))
		{
			props.put("hibernate.connection.datasource", persistenceUnit.getJtaDataSource().toString());
		}
		if (sessionContext)
		{
			props.put("hibernate.current_session_context_class", "jta");
		}
		if (factoryClass)
		{
			props.put("hibernate.transaction.factory_class", "org.hibernate.transaction.JTATransactionFactory");
		}
		if (managerLookup)
		{
			props.put("hibernate.transaction.manager_lookup_class", "org.hibernate.transaction.BTMTransactionManagerLookup");
		}
		if (jndiClass)
		{
			props.put("hibernate.jndi.class", "bitronix.tm.jndi.BitronixInitialContextFactory");
		}
		if (transactionPlatform)
		{
			props.put("hibernate.transaction.jta.platform", "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform");
		}

		return props;
	}
}
