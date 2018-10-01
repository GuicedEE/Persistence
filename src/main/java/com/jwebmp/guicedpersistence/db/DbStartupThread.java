package com.jwebmp.guicedpersistence.db;

import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.logger.LogFactory;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.logging.Level;

public class DbStartupThread
{
	private Class<? extends Annotation> annotation;

	public DbStartupThread(Class<? extends Annotation> annotation)
	{
		this.annotation = annotation;
	}

	public void run()
	{
		try
		{
			GuiceContext.get(DataSource.class, annotation);
		}
		catch (Throwable T)
		{
			LogFactory.getLog("DBStartup")
			          .log(Level.SEVERE, "Datasource Unable to start", T);

		}

		PersistService ps = GuiceContext.get(PersistService.class, annotation);
		ps.start();
		UnitOfWork ow = GuiceContext.get(UnitOfWork.class, annotation);
		ow.end();

		LogFactory.getLog("DBStartup")
		          .log(Level.CONFIG, "DBStartupThread Started - " + annotation.getSimpleName());
	}

	public String name()
	{
		return "DBStartupThread - @" + annotation.getSimpleName();
	}
}
