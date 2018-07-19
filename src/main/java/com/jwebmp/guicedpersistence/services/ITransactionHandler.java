package com.jwebmp.guicedpersistence.services;

import javax.persistence.EntityManager;

/**
 * A service for managing Entity Manager Transactions automagic like
 */
public interface ITransactionHandler
{
	/**
	 * What to do when beginning a transaction, always called
	 *
	 * @param createNew
	 * 		If create new was specified
	 * @param transactionExists
	 * 		If the transaction already exists
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void beginTransacation(boolean createNew, boolean transactionExists, EntityManager entityManager);

	/**
	 * What to do when committing a transaction, always called
	 *
	 * @param createNew
	 * 		If create new was specified
	 * @param transactionExists
	 * 		If the transaction already exists
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void commitTransacation(boolean createNew, boolean transactionExists, EntityManager entityManager);

	/**
	 * If this handler is active or not
	 *
	 * @return If this handler should be active
	 */
	default boolean active()
	{
		return false;
	}
}
