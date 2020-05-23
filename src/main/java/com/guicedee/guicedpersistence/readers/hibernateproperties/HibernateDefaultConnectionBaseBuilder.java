package com.guicedee.guicedpersistence.readers.hibernateproperties;

import com.oracle.jaxb21.PersistenceUnit;

import java.util.Properties;

/**
 * Reads the default connection properties for hibernate and configures the connection accordingly
 */
public class HibernateDefaultConnectionBaseBuilder
		implements com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader
{

	@Override
	public com.guicedee.guicedpersistence.db.ConnectionBaseInfo populateConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties, com.guicedee.guicedpersistence.db.ConnectionBaseInfo cbi)
	{
		for (String prop : filteredProperties.stringPropertyNames())
		{
			switch (prop)
			{
				case "hibernate.connection.url":
				{
					if (cbi.getUrl() == null)
					{
						cbi.setUrl(filteredProperties.getProperty(prop));
					}
					break;
				}
				case "hibernate.connection.user":
				{
					if (cbi.getUsername() == null)
					{
						cbi.setUsername(filteredProperties.getProperty(prop));
					}
					break;
				}
				case "hibernate.connection.driver_class":
				{
					if (cbi.getDriverClass() == null)
					{
						cbi.setDriverClass(filteredProperties.getProperty(prop));
					}
					break;
				}
				default:
				{
					break;
				}
			}
		}
		return cbi;
	}
}
