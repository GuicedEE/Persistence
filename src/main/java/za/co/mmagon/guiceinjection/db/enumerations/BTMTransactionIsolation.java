package za.co.mmagon.guiceinjection.db.enumerations;

/**
 * Sets the transaction isolation for the pooled connections
 * <p>
 * READ_COMMITED default
 */
public enum BTMTransactionIsolation
{
	READ_COMMITTED,
	READ_UNCOMMITTED,
	REPEATABLE_READ,
	SERIALIZABLE

}
