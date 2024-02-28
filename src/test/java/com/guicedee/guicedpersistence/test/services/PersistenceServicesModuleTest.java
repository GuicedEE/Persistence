package com.guicedee.guicedpersistence.test.services;

import com.guicedee.client.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

class PersistenceServicesModuleTest
{
	@Test
	public void testDBStart()
	{
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "fine");
		
		IGuiceContext.registerModule("com.guicedee.guicedpersistence.test");
		IGuiceContext.registerModule(new TestModule1());
		IGuiceContext.getContext().inject();
		EntityManager em = IGuiceContext.get(EntityManager.class, TestDB1.class);
		System.out.println(em.isOpen());
	}
}