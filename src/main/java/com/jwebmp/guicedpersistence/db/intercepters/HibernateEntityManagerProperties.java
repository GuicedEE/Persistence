package com.jwebmp.guicedpersistence.db.intercepters;

import com.jwebmp.guicedpersistence.services.PropertiesEntityManagerReader;
import com.oracle.jaxb21.PersistenceUnit;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HibernateEntityManagerProperties
		implements PropertiesEntityManagerReader
{
	/**
	 * A map of properties specific to a persistence unit
	 */
	private static final Map<Class<? extends Annotation>, HibernateEntityManagerProperties> persistenceUnitSpecificMappings = new HashMap();
	/**
	 * Whether to set show sql true or not
	 */
	private static boolean showSql;
	/**
	 * Whether to format the output sql
	 */
	private static boolean formatSql;
	/**
	 * Whether or not to use sql comments
	 */
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
		return HibernateEntityManagerProperties.maxFetchDepth;
	}

	public static void setMaxFetchDepth(int maxFetchDepth)
	{
		HibernateEntityManagerProperties.maxFetchDepth = maxFetchDepth;
	}

	public static boolean isEnableFetchOutsizeLadyLoad()
	{
		return HibernateEntityManagerProperties.enableFetchOutsizeLadyLoad;
	}

	public static void setEnableFetchOutsizeLadyLoad(boolean enableFetchOutsizeLadyLoad)
	{
		HibernateEntityManagerProperties.enableFetchOutsizeLadyLoad = enableFetchOutsizeLadyLoad;
	}

	public static boolean isShowSql()
	{
		return HibernateEntityManagerProperties.showSql;
	}

	public static void setShowSql(boolean showSql)
	{
		HibernateEntityManagerProperties.showSql = showSql;
	}

	public static boolean isFormatSql()
	{
		return HibernateEntityManagerProperties.formatSql;
	}

	public static void setFormatSql(boolean formatSql)
	{
		HibernateEntityManagerProperties.formatSql = formatSql;
	}

	public static boolean isUseSqlComments()
	{
		return HibernateEntityManagerProperties.useSqlComments;
	}

	public static void setUseSqlComments(boolean useSqlComments)
	{
		HibernateEntityManagerProperties.useSqlComments = useSqlComments;
	}

	@Override
	public Map<String, String> processProperties(PersistenceUnit persistenceUnit, Properties incomingProperties)
	{
		Map<String, String> props = new HashMap<>();
		if (HibernateEntityManagerProperties.enableFetchOutsizeLadyLoad)
		{
			incomingProperties.put("hibernate.enable_lazy_load_no_trans", Boolean.toString(true));
		}
		if (HibernateEntityManagerProperties.maxFetchDepth != Integer.MIN_VALUE)
		{
			incomingProperties.put("hibernate.max_fetch_depth", Integer.toString(HibernateEntityManagerProperties.maxFetchDepth));
		}
		if (HibernateEntityManagerProperties.showSql)
		{
			incomingProperties.put("hibernate.show_sql", "true");
		}
		if (HibernateEntityManagerProperties.formatSql)
		{
			incomingProperties.put("hibernate.format_sql", "true");
		}
		if (HibernateEntityManagerProperties.useSqlComments)
		{
			incomingProperties.put("hibernate.use_sql_comments", "true");
		}
		return props;
	}

}
