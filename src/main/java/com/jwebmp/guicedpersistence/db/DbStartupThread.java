package com.jwebmp.guicedpersistence.db;

import com.google.inject.persist.PersistService;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedpersistence.services.IAsyncStartup;
import com.jwebmp.logger.LogFactory;

import java.lang.annotation.Annotation;

public class DbStartupThread
		extends Thread
		implements IAsyncStartup
{
	private Class<? extends Annotation> annotation;

	public DbStartupThread(Class<? extends Annotation> annotation)
	{
		this.annotation = annotation;
	}

	@Override
	public void run()
	{
		super.run();
		PersistService ps = GuiceContext.get(PersistService.class, annotation);
		ps.start();
		LogFactory.getLog("DBStartupThread Started - " + annotation.getSimpleName());
	}

	@Override
	public String name()
	{
		return "Persist Service Starter - @" + annotation.getSimpleName();
	}
}
