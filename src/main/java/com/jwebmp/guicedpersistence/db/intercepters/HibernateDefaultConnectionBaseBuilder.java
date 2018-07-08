package com.jwebmp.guicedpersistence.db.intercepters;

import com.jwebmp.guicedpersistence.db.ConnectionBaseInfo;
import com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
import com.oracle.jaxb21.PersistenceUnit;

import java.util.Properties;

public class HibernateDefaultConnectionBaseBuilder
		implements PropertiesConnectionInfoReader
{
	/**
	 * Specifies the maximum fetch depth
	 * <p>
	 * default min value
	 */
	private static int maxFetchDepth = Integer.MIN_VALUE;
	/**
	 * If it should be possible to lazy fetch outside of transactions,
	 * <p>
	 * default true
	 */
	private static boolean enableFetchOutsizeLadyLoad = false;

	public static int getMaxFetchDepth()
	{
		return maxFetchDepth;
	}

	public static void setMaxFetchDepth(int maxFetchDepth)
	{
		HibernateDefaultConnectionBaseBuilder.maxFetchDepth = maxFetchDepth;
	}

	public static boolean isEnableFetchOutsizeLadyLoad()
	{
		return enableFetchOutsizeLadyLoad;
	}

	public static void setEnableFetchOutsizeLadyLoad(boolean enableFetchOutsizeLadyLoad)
	{
		HibernateDefaultConnectionBaseBuilder.enableFetchOutsizeLadyLoad = enableFetchOutsizeLadyLoad;
	}

	@Override
	public ConnectionBaseInfo populateConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties, ConnectionBaseInfo cbi)
	{
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
		if (enableFetchOutsizeLadyLoad)
		{
			filteredProperties.put("ibernate.enable_lazy_load_no_trans", Boolean.toString(true));
		}
		if (maxFetchDepth != Integer.MIN_VALUE)
		{
			filteredProperties.put("ibernate.max_fetch_depth", Integer.toString(maxFetchDepth));
		}

		return cbi;
	}
}
