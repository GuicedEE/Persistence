package za.co.mmagon.guiceinjection.db;

import com.google.inject.Key;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.Transactional;
import za.co.mmagon.guiceinjection.abstractions.GuiceInjectorModule;
import za.co.mmagon.guiceinjection.annotations.JaxbContext;
import za.co.mmagon.guiceinjection.interfaces.GuiceDefaultBinder;
import za.co.mmagon.logger.LogFactory;

import javax.xml.bind.JAXBContext;
import java.util.logging.Logger;

public class GuicedPersistenceBinding extends GuiceDefaultBinder
{
	private static final Logger log = LogFactory.getLog("DatabaseBinder");
	protected static JAXBContext persistenceContext;

	public static final Key<JAXBContext> PERSISTENCE_CONTEXT_KEY = Key.get(JAXBContext.class, JaxbContext.class);

	@Override
	public void onBind(GuiceInjectorModule module)
	{
		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), new BTMTransactionHandler());
		module.bind(PERSISTENCE_CONTEXT_KEY).toInstance(persistenceContext);
	}

	/**
	 * Returns the instance of the JAXB Context
	 *
	 * @return
	 */
	public static JAXBContext getPersistenceContext()
	{
		return persistenceContext;
	}

}
