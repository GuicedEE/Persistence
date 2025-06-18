package com.guicedee.guicedpersistence.test.services;

import com.guicedee.guicedpersistence.btm.BTMConnectionBaseInfo;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

import java.util.Properties;

public class TestModule1JTA extends DatabaseModule<TestModule1JTA>
{
	
	@Override
	protected String getPersistenceUnitName()
	{
		return "guiceinjectionh2testJTA";
	}
	
	@Override
	protected ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnitDescriptor unit, Properties filteredProperties)
	{
		return new BTMConnectionBaseInfo().setDefaultConnection(false);
	}
	
	@Override
	protected String getJndiMapping()
	{
		return "jdbc/testmoduleJTA";
	}
}
