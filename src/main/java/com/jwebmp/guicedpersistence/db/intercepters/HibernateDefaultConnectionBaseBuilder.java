package com.jwebmp.guicedpersistence.db.intercepters;

import com.jwebmp.guicedpersistence.db.ConnectionBaseInfo;
import com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
import com.oracle.jaxb21.PersistenceUnit;

import java.util.Properties;

public class HibernateDefaultConnectionBaseBuilder
		implements PropertiesConnectionInfoReader
{

	@Override
	public ConnectionBaseInfo populateConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties, ConnectionBaseInfo cbi)
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
