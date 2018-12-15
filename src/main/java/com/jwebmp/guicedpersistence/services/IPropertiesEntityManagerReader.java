package com.jwebmp.guicedpersistence.services;

import com.oracle.jaxb21.PersistenceUnit;

import java.util.Map;
import java.util.Properties;

/**
 * Manages properties passed into the entity manager factory
 */
@FunctionalInterface
public interface IPropertiesEntityManagerReader
{
	/**
	 * Manages properties passed into the entity manager factory
	 * <p>
	 * return properties
	 */
	Map<String, String> processProperties(PersistenceUnit persistenceUnit, Properties incomingProperties);
}
