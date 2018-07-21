package com.jwebmp.guicedinjection.db;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import com.jwebmp.guicedpersistence.services.IDBStartup;

public class TestDBStartupPostStartup
		implements IDBStartup
{
	@Inject
	public TestDBStartupPostStartup(@TestCustomPersistenceLoader PersistService ps)
	{
		ps.start();
	}
}
