package com.guicedee.guicedpersistence.db.intercepters;

import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader;
import com.oracle.jaxb21.PersistenceUnit;

import java.util.Properties;

public class JPADefaultConnectionBaseBuilder
		implements IPropertiesConnectionInfoReader
{
	@Override
	public ConnectionBaseInfo populateConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties, ConnectionBaseInfo cbi)
	{
		for (String prop : filteredProperties.stringPropertyNames())
		{
			switch (prop)
			{
				case "javax.persistence.jdbc.url":
				{
					cbi.setUrl(filteredProperties.getProperty(prop));
					break;
				}
				case "javax.persistence.jdbc.user":
				{
					cbi.setUsername(filteredProperties.getProperty(prop));
					break;
				}
				case "javax.persistence.jdbc.password":
				{
					cbi.setPassword(filteredProperties.getProperty(prop));
					break;
				}
				case "javax.persistence.jdbc.driver":
				{
					cbi.setDriverClass(filteredProperties.getProperty(prop));
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
