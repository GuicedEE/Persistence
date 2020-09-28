package com.guicedee.guicedinjection.db;

import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;

import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Properties;

public class TestJTADBPrivateModule
		extends DatabaseModule<TestJTADBPrivateModule>
		implements IGuiceModule<TestJTADBPrivateModule>
{

	@NotNull
	@Override
	protected String getPersistenceUnitName()
	{
		return "guiceinjectionh2test";
	}

	@Override
	protected @NotNull ConnectionBaseInfo getConnectionBaseInfo(ParsedPersistenceXmlDescriptor unit, Properties filteredProperties)
	{
		return new ConnectionBaseInfo()
		{
			@Override
			public DataSource toPooledDatasource()
			{
				return new TestJPAConnectionBaseInfo().toPooledDatasource();
			}
		};
	}

	@NotNull
	@Override
	protected String getJndiMapping()
	{
		return "jdbc/jndi";
	}

	@NotNull
	@Override
	protected Class<? extends Annotation> getBindingAnnotation()
	{
		return TestCustomPersistenceLoader.class;
	}
}
