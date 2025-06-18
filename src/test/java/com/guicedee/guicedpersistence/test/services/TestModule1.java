package com.guicedee.guicedpersistence.test.services;

import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import com.guicedee.guicedpersistence.jta.JPAConnectionBaseInfo;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

import java.util.Properties;

public class TestModule1 extends DatabaseModule<TestModule1>
{
	
	@Override
	protected String getPersistenceUnitName()
	{
		return "guiceinjectionh2test";
	}
	
	@Override
	protected ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnitDescriptor unit, Properties filteredProperties)
	{
		return new JPAConnectionBaseInfo();
	}
	
	@Override
	protected String getJndiMapping()
	{
		return "jdbc/testmodule";
	}
}
