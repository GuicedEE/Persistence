package com.guicedee.guicedpersistence.services;

import com.google.inject.Module;
import com.google.inject.*;
import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedpersistence.db.*;
import com.guicedee.logger.*;

import javax.sql.*;
import java.lang.annotation.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

@SuppressWarnings("unused")
public class PersistenceServicesModule
		extends AbstractModule
		implements IGuiceModule<PersistenceServicesModule>
{
	private static final Logger log = LogFactory.getLog(PersistenceServicesModule.class);
	
	private static final Map<Class<? extends Annotation>, Module> modules = new LinkedHashMap<>();
	private static final Map<Class<? extends Annotation>, ConnectionBaseInfo> jtaConnectionBaseInfo = new LinkedHashMap<>();
	
	private static final Map<String, DataSource> jtaDataSources = new LinkedHashMap<>();
	private static final Map<String, Set<String>> jtaPersistenceUnits = new LinkedHashMap<>();
	
	@Override
	protected void configure()
	{
		log.config("Building Persistence Services Module");
		modules.forEach((key, value) -> install(value));
		
		for (Map.Entry<Class<? extends Annotation>, ConnectionBaseInfo> entry : jtaConnectionBaseInfo.entrySet())
		{
			Class<? extends Annotation> k = entry.getKey();
			ConnectionBaseInfo v = entry.getValue();
			DataSource ds;
			try
			{
				if (!jtaDataSources.containsKey(v.getJndiName()))
				{
					log.config("Starting datasource - " + v.getJndiName());
					ds = v.toPooledDatasource();
					if (ds != null)
					{
						jtaDataSources.put(v.getJndiName(), ds);
						bind(Key.get(DataSource.class, k)).toInstance(ds);
						
						log.config("Bound DataSource.class with Key " + k.getSimpleName());
						bind(Key.get(Connection.class, k)).toProvider(new DataSourceConnectionProvider(k));
						log.config("Bound Thread Local Connection.class with Key " + k.getSimpleName());
					}
					if (!jtaPersistenceUnits.containsKey(v.getJndiName()))
					{
						jtaPersistenceUnits.put(v.getJndiName(), new LinkedHashSet<>());
					}
					jtaPersistenceUnits.get(v.getJndiName())
					                   .add(jtaConnectionBaseInfo.get(k)
					                                             .getPersistenceUnitName());
				}
				else
				{
					ds = jtaDataSources.get(v.getJndiName());
					if (ds != null)
					{
						bind(Key.get(DataSource.class, k)).toInstance(ds);
					}
				}
			}
			catch (Exception t)
			{
				log.log(Level.SEVERE, "Cannot start datasource!", t);
			}
		}
	}
	
	public static Map<Class<? extends Annotation>, Module> getModules()
	{
		return modules;
	}
	
	public static Map<Class<? extends Annotation>, ConnectionBaseInfo> getJtaConnectionBaseInfo()
	{
		return jtaConnectionBaseInfo;
	}
	
	public static Map<String, DataSource> getJtaDataSources()
	{
		return jtaDataSources;
	}
	
	public static void addJtaPersistenceUnits(String jndi, String persistenceUnitName)
	{
		if (!jtaPersistenceUnits.containsKey(jndi))
		{
			jtaPersistenceUnits.put(jndi, new LinkedHashSet<>());
		}
		jtaPersistenceUnits.get(jndi)
		                   .add(persistenceUnitName);
	}
	
	public static Map<String, Set<String>> getJtaPersistenceUnits()
	{
		return jtaPersistenceUnits;
	}
	
	@Override
	public Integer sortOrder()
	{
		return Integer.MAX_VALUE - 5;
	}
}
