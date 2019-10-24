package com.guicedee.guicedpersistence.db.services;

import com.guicedee.guicedinjection.GuiceConfig;
import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;

public class PersistenceGuiceConfigurator
		implements IGuiceConfigurator
{
	@Override
	public GuiceConfig configure(GuiceConfig config)
	{
		return config.setPathScanning(true);
	}
}
