package com.guicedee.guicedpersistence.test.services;

import com.guicedee.guicedinjection.GuiceContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

class PersistenceServicesModuleTest
{
	@Test
	public void testDBStart()
	{
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "fine");
		GuiceContext.registerModule("com.guicedee.guicedpersistence.test");
		GuiceContext.registerModule(new TestModule1());
		GuiceContext.inject();
		EntityManager em = GuiceContext.get(EntityManager.class, TestDB1.class);
		System.out.println(em.isOpen());
	}
}