package com.jwebmp.guicedinjection.db;

import com.jwebmp.guicedpersistence.db.connectionbasebuilders.HibernateDefaultConnectionBaseBuilder;

import java.lang.annotation.Annotation;

public class TestDBPrivateModule
		extends HibernateDefaultConnectionBaseBuilder
{

	@Override
	protected String getJndiMapping()
	{
		return "jdbc/jndi";
	}

	@Override
	protected String getPersistenceUnitName()
	{
		return "guiceinjectionh2test";
	}

	@Override
	protected Class<? extends Annotation> getBindingAnnotation()
	{
		return TestCustomPersistenceLoader.class;
	}
}
