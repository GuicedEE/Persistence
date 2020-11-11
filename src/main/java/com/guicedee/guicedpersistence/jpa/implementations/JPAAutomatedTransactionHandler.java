package com.guicedee.guicedpersistence.jpa.implementations;




import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import jakarta.persistence.EntityManager;

public class JPAAutomatedTransactionHandler
		implements com.guicedee.guicedpersistence.services.ITransactionHandler<JPAAutomatedTransactionHandler>
{
	private static boolean active = false;

	private static final String RESOURCE_LOCAL_STRING = "RESOURCE_LOCAL";

	public JPAAutomatedTransactionHandler()
	{
		//No config required
	}

	/**
	 * Sets this Automated transaction handler to active
	 *
	 * @param active
	 */
	public static void setActive(boolean active)
	{
		JPAAutomatedTransactionHandler.active = active;
	}

	@Override
	public void beginTransacation(boolean createNew, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit)
	{
		entityManager.getTransaction()
		             .begin();

	}

	@Override
	public void commitTransacation(boolean createNew, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit)
	{
		entityManager.getTransaction()
		             .commit();
	}

	@Override
	public void setTransactionTimeout(int timeout, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit)
	{

	}

	@Override
	public void rollbackTransacation(boolean createNew, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit)
	{
		entityManager.getTransaction()
		             .rollback();
	}

	@Override
	public boolean transactionExists(EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit)
	{
		if(active)
		return entityManager.getTransaction()
		                    .isActive();
		else
			return false;
	}

	@Override
	public boolean active(ParsedPersistenceXmlDescriptor persistenceUnit)
	{
		return active && persistenceUnit.getTransactionType() == null || persistenceUnit.getTransactionType()
		                                                                                .toString().equals(RESOURCE_LOCAL_STRING);
	}
}
