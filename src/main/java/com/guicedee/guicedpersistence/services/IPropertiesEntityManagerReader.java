package com.guicedee.guicedpersistence.services;


import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

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
	Map<String, String> processProperties(ParsedPersistenceXmlDescriptor persistenceUnit, Properties incomingProperties);
}
