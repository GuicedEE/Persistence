package za.co.mmagon.guiceinjection.db;

import org.junit.jupiter.api.Test;
import za.co.mmagon.guiceinjection.GuiceContext;
import za.co.mmagon.logger.LogFactory;

import java.util.logging.Level;

class GuicedPersistenceBindingTest
{
	@Test
	public void testMe()
	{
		LogFactory.configureConsoleSingleLineOutput(Level.FINE);
		GuiceContext.inject();
	}
}
