package com.jwebmp.guicedinjection.db;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import com.jwebmp.guicedpersistence.db.DBStartupAsync;

import javax.sql.DataSource;

public class TestDBStartup
		extends DBStartupAsync
{
	@Inject
	public TestDBStartup(@TestCustomPersistenceLoader PersistService ps)
	{
		super(ps);
		ps.start();
	}

}
