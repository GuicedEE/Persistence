package za.co.mmagon.guiceinjection.db.connectionbasebuilders;

import com.oracle.jaxb21.Persistence;
import za.co.mmagon.guiceinjection.db.ConnectionBaseInfo;

import java.util.Properties;

public abstract class JPADefaultConnectionBaseBuilder
		extends AbstractDatabaseProviderModule
{
	@Override
	protected ConnectionBaseInfo getConnectionBaseInfo(Persistence.PersistenceUnit unit, Properties filteredProperties)
	{
		ConnectionBaseInfo cbi = new ConnectionBaseInfo();
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
