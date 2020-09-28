package com.guicedee.guicedpersistence.readers.systemproperties;

import com.google.common.base.Strings;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A default connection string builder for H2 Databases
 */
public class SystemEnvironmentVariablesPropertiesReader
		implements IPropertiesEntityManagerReader
{

	@Override
	public Map<String, String> processProperties(ParsedPersistenceXmlDescriptor persistenceUnit, Properties incomingProperties)
	{
		for (String prop : incomingProperties.stringPropertyNames())
		{
			String value = incomingProperties.getProperty(prop);
			if (value.startsWith("${"))
			{
				String searchProperty = value.substring(2, value.length() - 1);
				String systemProperty = System.getProperty(searchProperty);
				if (!Strings.isNullOrEmpty(systemProperty))
				{
					incomingProperties.put(prop, systemProperty);
				}
			}
		}
		return new HashMap<>();
	}
}
