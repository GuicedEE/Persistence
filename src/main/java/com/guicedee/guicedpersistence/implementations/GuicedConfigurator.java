package com.guicedee.guicedpersistence.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceConfig;
import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;

public class GuicedConfigurator implements IGuiceConfigurator
{
	@Override
	public IGuiceConfig<?> configure(IGuiceConfig<?> iGuiceConfig)
	{
		iGuiceConfig.setMethodInfo(true)
						.setAllowPaths(true)
						.setClasspathScanning(true)
						.setAnnotationScanning(true)
						.setFieldScanning(true);
		return iGuiceConfig;
	}
}
