package com.jwebmp.guicedpersistence.db.services;

import com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;
import com.oracle.jaxb21.PersistenceUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HibernateEntityManagerProperties
		implements PropertiesEntityManagerReader
{
	private static boolean showSql;
	private static boolean formatSql;
	private static boolean useSqlComments;

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
		HibernateEntityManagerProperties.maxFetchDepth = maxFetchDepth;
	}

	public static boolean isEnableFetchOutsizeLadyLoad()
	{
		return enableFetchOutsizeLadyLoad;
	}

	public static void setEnableFetchOutsizeLadyLoad(boolean enableFetchOutsizeLadyLoad)
	{
		HibernateEntityManagerProperties.enableFetchOutsizeLadyLoad = enableFetchOutsizeLadyLoad;
	}

	public static boolean isShowSql()
	{
		return showSql;
	}

	public static void setShowSql(boolean showSql)
	{
		HibernateEntityManagerProperties.showSql = showSql;
	}

	public static boolean isFormatSql()
	{
		return formatSql;
	}

	public static void setFormatSql(boolean formatSql)
	{
		HibernateEntityManagerProperties.formatSql = formatSql;
	}

	public static boolean isUseSqlComments()
	{
		return useSqlComments;
	}

	public static void setUseSqlComments(boolean useSqlComments)
	{
		HibernateEntityManagerProperties.useSqlComments = useSqlComments;
	}

	@Override
	public Map<String, String> processProperties(PersistenceUnit persistenceUnit, Properties incomingProperties)
	{
		Map<String, String> props = new HashMap<>();
		if (enableFetchOutsizeLadyLoad)
		{
			incomingProperties.put("hibernate.enable_lazy_load_no_trans", Boolean.toString(true));
		}
		if (maxFetchDepth != Integer.MIN_VALUE)
		{
			incomingProperties.put("hibernate.max_fetch_depth", Integer.toString(maxFetchDepth));
		}
		if (showSql)
		{
			incomingProperties.put("hibernate.show_sql", "true");
		}
		if (formatSql)
		{
			incomingProperties.put("hibernate.format_sql", "true");
		}
		if (useSqlComments)
		{
			incomingProperties.put("hibernate.use_sql_comments", "true");
		}
		return props;
	}
}
