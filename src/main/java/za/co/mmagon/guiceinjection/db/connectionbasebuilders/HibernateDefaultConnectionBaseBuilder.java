package za.co.mmagon.guiceinjection.db.connectionbasebuilders;

import com.oracle.jaxb21.Persistence;
import za.co.mmagon.guiceinjection.db.ConnectionBaseInfo;

import java.util.Properties;

public abstract class HibernateDefaultConnectionBaseBuilder extends JPADefaultConnectionBaseBuilder
{
	@Override
	protected ConnectionBaseInfo getConnectionBaseInfo(Persistence.PersistenceUnit unit, Properties filteredProperties)
	{
		ConnectionBaseInfo cbi = super.getConnectionBaseInfo(unit, filteredProperties);
		for (String prop : filteredProperties.stringPropertyNames())
		{
			switch (prop)
			{
				case "hibernate.connection.url":
				{
					cbi.setUrl(filteredProperties.getProperty(prop));
					break;
				}
				case "hibernate.connection.user":
				{
					cbi.setUsername(filteredProperties.getProperty(prop));
					break;
				}
				case "hibernate.connection.driver_class":
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
