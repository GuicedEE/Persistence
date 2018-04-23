package za.co.mmagon.guiceinjection.db;

import com.google.inject.Key;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.Transactional;
import za.co.mmagon.guiceinjection.abstractions.GuiceInjectorModule;
import za.co.mmagon.guiceinjection.annotations.JaxbContext;
import za.co.mmagon.guiceinjection.interfaces.GuiceDefaultBinder;

import javax.xml.bind.JAXBContext;

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
		module.bind(PERSISTENCE_CONTEXT_KEY)
		      .toInstance(persistenceContext);
	}

}
