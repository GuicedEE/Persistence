package za.co.mmagon.guiceinjection.db;

import za.co.mmagon.guiceinjection.GuiceContext;
import za.co.mmagon.guiceinjection.annotations.DBStartup;
import za.co.mmagon.guiceinjection.annotations.GuicePostStartup;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Initializes all classes annotated with @DBStartup
 */
@SuppressWarnings("unused")
public class GuicedPersistenceDBStartup implements GuicePostStartup
{
	private static final Logger log = Logger.getLogger("DB Initialization");

	@Override
	public void postLoad()
	{
		Set<Class<?>> startupClasses = GuiceContext.reflect().getTypesAnnotatedWith(DBStartup.class);
		startupClasses.removeIf(a -> Modifier.isAbstract(a.getModifiers()));
		if (startupClasses.isEmpty())
		{
			log.config("No classes extends DBStartup class. No Databases will startup automatically.");
		}
		else
		{
			log.config("Starting up marked @DBStartup Classes] initializers");
			for (Class<?> clazz : startupClasses)
			{
				GuiceContext.getInstance(clazz);
			}
			log.info("All DB Startups have been called");
		}
	}

	@Override
	public Integer sortOrder()
	{
		return 50;
	}
}
