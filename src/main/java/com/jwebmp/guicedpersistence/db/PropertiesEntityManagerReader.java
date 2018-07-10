package com.jwebmp.guicedpersistence.db;

import java.util.Map;
import java.util.Properties;

/**
 * Manages properties passed into the entity manager factory
 */
@FunctionalInterface
public interface PropertiesEntityManagerReader
{
	/**
	 * Manages properties passed into the entity manager factory
	 * <p>
	 * return properties
	 */
	Map<String, String> processProperties(Properties incomingProperties);
}
