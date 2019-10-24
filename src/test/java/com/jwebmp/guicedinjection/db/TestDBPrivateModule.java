package com.guicedee.guicedinjection.db;

import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.oracle.jaxb21.PersistenceUnit;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Properties;

public class TestDBPrivateModule
		extends DatabaseModule<TestDBPrivateModule>
		implements IGuiceModule<TestDBPrivateModule>
{

	@NotNull
	@Override
	protected String getPersistenceUnitName()
	{
		return "guiceinjectionh2test";
	}

	@Override
	protected @NotNull ConnectionBaseInfo getConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties)
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
