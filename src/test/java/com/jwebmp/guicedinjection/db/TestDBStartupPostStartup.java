package com.jwebmp.guicedinjection.db;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import com.jwebmp.guicedpersistence.services.IAsyncStartup;

public class TestDBStartupPostStartup
		implements IAsyncStartup
{
	public TestDBStartupPostStartup()
	{
	}

	@Inject
	public TestDBStartupPostStartup(@TestCustomPersistenceLoader PersistService ps)
	{
		ps.start();
	}
}
