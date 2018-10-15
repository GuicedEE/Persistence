package com.jwebmp.guicedpersistence.db.services;

import com.jwebmp.guicedinjection.GuiceConfig;
import com.jwebmp.guicedinjection.interfaces.IGuiceConfigurator;

public class PersistenceGuiceConfigurator
		implements IGuiceConfigurator
{
	@Override
	public GuiceConfig configure(GuiceConfig config)
	{
		return config.setClasspathScanning(false)
		             .setExcludePaths(true)
		             .setWhiteList(true);
	}
}
