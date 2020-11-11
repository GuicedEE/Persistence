package com.guicedee.guicedpersistence.db.intercepters;

import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;


import java.util.Properties;

public class JPADefaultConnectionBaseBuilder
		implements IPropertiesConnectionInfoReader
{
	@Override
	public ConnectionBaseInfo populateConnectionBaseInfo(ParsedPersistenceXmlDescriptor unit, Properties filteredProperties, ConnectionBaseInfo cbi)
	{
		for (String prop : filteredProperties.stringPropertyNames())
		{
			switch (prop)
			{
				case "jakarta.persistence.jdbc.url":
				{
					cbi.setUrl(filteredProperties.getProperty(prop));
					break;
				}
				case "jakarta.persistence.jdbc.user":
				{
					cbi.setUsername(filteredProperties.getProperty(prop));
					break;
				}
				case "jakarta.persistence.jdbc.password":
				{
					cbi.setPassword(filteredProperties.getProperty(prop));
					break;
				}
				case "jakarta.persistence.jdbc.driver":
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
