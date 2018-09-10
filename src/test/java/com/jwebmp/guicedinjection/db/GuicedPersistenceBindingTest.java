package com.jwebmp.guicedinjection.db;

import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.logger.LogFactory;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

class GuicedPersistenceBindingTest
{
	@Test
	public void testMe()
	{
		LogFactory.configureConsoleSingleLineOutput(Level.FINE);
		ScanResult sr = GuiceContext.instance()
		                            .getScanResult();
		GuiceContext.inject();

	}
}
