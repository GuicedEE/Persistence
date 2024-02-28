package com.guicedee.guicedpersistence.implementations;

import com.google.inject.persist.PersistService;
import com.guicedee.client.*;
import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import lombok.extern.java.Log;

import java.lang.annotation.Annotation;
import java.util.logging.Level;

@Log
public class GuicedPersistenceDestroyer
		implements IGuicePreDestroy<GuicedPersistenceDestroyer>
{
	@Override
	public void onDestroy()
	{
		for (Class<? extends Annotation> boundAnnotation : DatabaseModule.getBoundAnnotations())
		{
			try
			{
				log.log(Level.INFO, "Stopping EMF and Persist Service [" + boundAnnotation.getCanonicalName() + "]");
				PersistService service = IGuiceContext.get(PersistService.class, boundAnnotation);
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
