package com.jwebmp.guicedinjection.db;

import com.google.inject.Key;
import com.google.inject.persist.UnitOfWork;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedpersistence.db.AsyncPostStartup;
import com.jwebmp.logger.LogFactory;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class GuicedPersistenceBindingTest
{
	@Test
	public void testMe() throws InterruptedException
	{
		LogFactory.configureConsoleSingleLineOutput(Level.FINE);
		ScanResult sr = GuiceContext.instance()
		                            .getScanResult();
		GuiceContext.inject();

		AsyncPostStartup.getDbAutoStartupExecutors()
		                .awaitTermination(10, TimeUnit.MINUTES);

		EntityManager em = GuiceContext.get(Key.get(EntityManager.class, TestCustomPersistenceLoader.class));

		UnitOfWork uw = GuiceContext.get(Key.get(UnitOfWork.class, TestCustomPersistenceLoader.class));
		uw.begin();
		System.out.println("Entity Manager Ready : " + em.isOpen());
		uw.end();
		EntityManager em2 = GuiceContext.get(Key.get(EntityManager.class, TestCustomPersistenceLoader.class));
		System.out.println("Entity Manager 2 Ready : " + em2.isOpen());

		System.out.println("Are they the same entity manager? : " + em.equals(em2));

		assertFalse(em.equals(em2));

		uw.begin();
		EntityManager em3 = GuiceContext.get(Key.get(EntityManager.class, TestCustomPersistenceLoader.class));
		System.out.println("Entity Manager 3 Ready : " + em3.isOpen());

		System.out.println("Are they the same entity manager? : " + em2.equals(em3));
		assertFalse(em2.equals(em3));
		uw.end();
	}
}
