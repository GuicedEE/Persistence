package com.guicedee.guicedpersistence.test.services;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.client.IGuiceContext;
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

		UnitOfWork work = IGuiceContext.get(UnitOfWork.class);

		work.begin();
		EntityManager em = IGuiceContext.get(EntityManager.class);
		work.end();

		UnitOfWork uow2 = IGuiceContext.get(Key.get(UnitOfWork.class, Names.named("guiceinjectionh2testJTA")));
		uow2.begin();
		EntityManager em2 = IGuiceContext.get(Key.get(EntityManager.class, Names.named("guiceinjectionh2testJTA")));
		uow2.end();
		System.out.println(em.isOpen());
	}
}