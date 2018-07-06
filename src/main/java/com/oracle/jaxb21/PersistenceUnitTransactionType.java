
package com.oracle.jaxb21;

public enum PersistenceUnitTransactionType
{

	JTA,
	RESOURCE_LOCAL;

	public static PersistenceUnitTransactionType fromValue(String v)
	{
		return valueOf(v);
	}

	public String value()
	{
		return name();
	}

}
