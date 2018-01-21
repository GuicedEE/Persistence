package za.co.mmagon.guiceinjection.db;

import org.junit.jupiter.api.Test;
import za.co.mmagon.guiceinjection.GuiceContext;

class GuicedPersistenceBindingTest
{
	@Test
	public void testMe()
	{
		GuiceContext.inject();
	}
}
