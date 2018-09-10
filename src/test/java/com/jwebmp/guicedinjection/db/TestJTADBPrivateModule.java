package com.jwebmp.guicedinjection.db;

import com.jwebmp.guicedinjection.interfaces.IGuiceModule;
import com.jwebmp.guicedpersistence.db.AbstractDatabaseProviderModule;
import com.jwebmp.guicedpersistence.db.ConnectionBaseInfo;
import com.oracle.jaxb21.PersistenceUnit;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.Properties;

public class TestJTADBPrivateModule
		extends AbstractDatabaseProviderModule
		implements IGuiceModule
{

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

	@Override
	protected String getJndiMapping()
	{
		return "jdbc/jndi";
	}

	@Override
	protected Class<? extends Annotation> getBindingAnnotation()
	{
		return TestCustomPersistenceLoader.class;
	}
}
