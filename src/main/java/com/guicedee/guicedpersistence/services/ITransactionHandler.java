package com.guicedee.guicedpersistence.services;

import com.guicedee.guicedinjection.interfaces.IDefaultService;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;


import jakarta.persistence.EntityManager;

/**
 * A service for managing Entity Manager Transactions automagic like
 */
public interface ITransactionHandler<J extends ITransactionHandler<J>>
		extends IDefaultService<J>
{
	/**
	 * What to do when beginning a transaction, always called
	 *
	 * @param createNew
	 * 		If create new was specified
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void beginTransacation(boolean createNew, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit);

	/**
	 * What to do when committing a transaction, always called
	 *
	 * @param createNew
	 * 		If the transaction already exists
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void commitTransacation(boolean createNew, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit);


	/**
	 * What to do when committing a transaction, always called
	 *
	 * @param timeout
	 * 		The timeout to apply default 30
	 * @param entityManager
	 * 		The entity manager ParsedPersistenceXmlDescriptor
	 */
	void setTransactionTimeout(int timeout, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit);

	/**
	 * What to do when committing a transaction, always called
	 *
	 * @param createNew
	 * 		If the transaction already exists
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void rollbackTransacation(boolean createNew, EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit);

	/**
	 * Returns the value denoting if the transaction exists or not
	 *
	 * @param entityManager
	 * 		The given entity manager
	 *
	 * @return if the transaction exists or not
	 */
	boolean transactionExists(EntityManager entityManager, ParsedPersistenceXmlDescriptor persistenceUnit);

	/**
	 * If this handler is active or not
	 *
	 * @return If this handler should be active
	 */
	default boolean active(ParsedPersistenceXmlDescriptor persistenceUnit)
	{
		return true;
	}
}
