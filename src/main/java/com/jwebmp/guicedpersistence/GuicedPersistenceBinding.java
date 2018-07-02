package com.jwebmp.guicedpersistence;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.Transactional;
import com.jwebmp.guicedinjection.abstractions.GuiceInjectorModule;
import com.jwebmp.guicedinjection.interfaces.GuiceDefaultBinder;
import com.jwebmp.guicedpersistence.annotations.JaxbContext;
import com.jwebmp.guicedpersistence.db.TransactionHandler;
import com.jwebmp.guicedpersistence.exceptions.NoConnectionInfoException;
import com.oracle.jaxb21.Persistence;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class GuicedPersistenceBinding
		extends GuiceDefaultBinder
{
	public static final Key<JAXBContext> PERSISTENCE_CONTEXT_KEY = Key.get(JAXBContext.class, JaxbContext.class);
	protected static JAXBContext persistenceContext;

	/**
	 * Returns the instance of the JAXB Context
	 *
	 * @return
	 */
	public static JAXBContext getPersistenceContext()
	{
		return persistenceContext;
	}

	public static void setPersistenceContext(JAXBContext persistenceContext)
	{
		GuicedPersistenceBinding.persistenceContext = persistenceContext;
	}

	@Override
	public void onBind(GuiceInjectorModule module)
	{
		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), new TransactionHandler());
		module.bind(PERSISTENCE_CONTEXT_KEY)
		      .toProvider(() ->
		                  {
			                  if (persistenceContext == null)
			                  {
				                  try
				                  {
					                  GuicedPersistenceBinding.persistenceContext = JAXBContext.newInstance(Persistence.class);
				                  }
				                  catch (JAXBException e)
				                  {
					                  throw new NoConnectionInfoException("Persistence Unit Load Failed", e);
				                  }
			                  }
			                  return persistenceContext;
		                  })
		      .in(Singleton.class);
	}
}
