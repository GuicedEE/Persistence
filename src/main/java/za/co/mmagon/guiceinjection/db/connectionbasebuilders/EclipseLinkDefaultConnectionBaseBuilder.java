package za.co.mmagon.guiceinjection.db.connectionbasebuilders;

import com.oracle.jaxb21.Persistence;
import za.co.mmagon.guiceinjection.db.ConnectionBaseInfo;

import java.util.Properties;

/**
 * A default connection string builder for H2 Databases
 */
public abstract class EclipseLinkDefaultConnectionBaseBuilder extends JPADefaultConnectionBaseBuilder
{
	@Override
	protected ConnectionBaseInfo getConnectionBaseInfo(Persistence.PersistenceUnit unit, Properties filteredProperties)
	{
		ConnectionBaseInfo cbi = super.getConnectionBaseInfo(unit, filteredProperties);
		for (String prop : filteredProperties.stringPropertyNames())
		{
			switch (prop)
			{
				case "eclipselink.jdbc.url":
				{
					cbi.setUrl(filteredProperties.getProperty(prop));
					break;
				}
				case "eclipselink.jdbc.user":
				{
					cbi.setUsername(filteredProperties.getProperty(prop));
					break;
				}
				case "eclipselink.jdbc.password":
				{
					cbi.setPassword(filteredProperties.getProperty(prop));
					break;
				}
				case "eclipselink.jdbc.driver":
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
