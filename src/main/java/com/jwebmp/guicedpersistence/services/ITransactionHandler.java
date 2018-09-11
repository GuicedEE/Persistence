package com.jwebmp.guicedpersistence.services;

import com.oracle.jaxb21.PersistenceUnit;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.Comparator;

/**
 * A service for managing Entity Manager Transactions automagic like
 */
public interface ITransactionHandler
		extends Comparable<ITransactionHandler>, Comparator<ITransactionHandler>
{
	/**
	 * What to do when beginning a transaction, always called
	 *
	 * @param createNew
	 * 		If create new was specified
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void beginTransacation(boolean createNew, EntityManager entityManager, PersistenceUnit persistenceUnit);

	/**
	 * What to do when committing a transaction, always called
	 *
	 * @param createNew
	 * 		If the transaction already exists
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void commitTransacation(boolean createNew, EntityManager entityManager, PersistenceUnit persistenceUnit);

	/**
	 * What to do when committing a transaction, always called
	 *
	 * @param createNew
	 * 		If the transaction already exists
	 * @param entityManager
	 * 		The entity manager associated
	 */
	void rollbackTransacation(boolean createNew, EntityManager entityManager, PersistenceUnit persistenceUnit);

	/**
	 * Returns the value denoting if the transaction exists or not
	 *
	 * @param entityManager
	 * 		The given entity manager
	 *
	 * @return if the transaction exists or not
	 */
	boolean transactionExists(EntityManager entityManager, PersistenceUnit persistenceUnit);

	/**
	 * If this handler is active or not
	 *
	 * @return If this handler should be active
	 */
	default boolean active(PersistenceUnit persistenceUnit)
	{
		return true;
	}

	/**
	 * If the handler should be available for automatic entity assist control
	 *
	 * @return default false
	 */
	default boolean enableAutomaticControl()
	{
		return false;
	}

	@Override
	default int compare(ITransactionHandler o1, ITransactionHandler o2)
	{
		if (o1 == null || o2 == null)
		{
			return -1;
		}
		return o1.sortOrder()
		         .compareTo(o2.sortOrder());
	}

	default Integer sortOrder()
	{
		return 100;
	}

	@Override
	default int compareTo(@NotNull ITransactionHandler o)
	{
		int sort = sortOrder().compareTo(o.sortOrder());
		if (sort == 0)
		{
			return -1;
		}
		return sort;
	}
}
