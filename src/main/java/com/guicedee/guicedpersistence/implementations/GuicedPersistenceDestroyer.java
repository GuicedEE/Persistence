package com.guicedee.guicedpersistence.implementations;

import com.google.inject.persist.PersistService;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import com.guicedee.logger.LogFactory;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuicedPersistenceDestroyer
		implements IGuicePreDestroy<GuicedPersistenceDestroyer>
{
	private static final Logger log = LogFactory.getLog("GuicedPersistenceDestroyer");

	@Override
	public void onDestroy()
	{
		for (Class<? extends Annotation> boundAnnotation : DatabaseModule.getBoundAnnotations())
		{
			try
			{
				log.log(Level.INFO, "Stopping EMF and Persist Service [" + boundAnnotation.getCanonicalName() + "]");
				PersistService service = GuiceContext.get(PersistService.class, boundAnnotation);
				service.stop();
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Unable to close entity managers and factories for annotation [" + boundAnnotation.getCanonicalName() + "]", e);
			}
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 500;
	}
}
