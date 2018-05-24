package za.co.mmagon.guiceinjection.db;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.Transactional;
import com.oracle.jaxb21.Persistence;
import za.co.mmagon.guiceinjection.abstractions.GuiceInjectorModule;
import za.co.mmagon.guiceinjection.annotations.JaxbContext;
import za.co.mmagon.guiceinjection.exceptions.NoConnectionInfoException;
import za.co.mmagon.guiceinjection.interfaces.GuiceDefaultBinder;

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

	@Override
	public void onBind(GuiceInjectorModule module)
	{
		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), new TransactionHandler());
		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(javax.transaction.Transactional.class), new TransactionHandler());
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
