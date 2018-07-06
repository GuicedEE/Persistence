
package com.oracle.jaxb21;

public enum PersistenceUnitCachingType
{

	ALL,
	NONE,
	ENABLE_SELECTIVE,
	DISABLE_SELECTIVE,
	UNSPECIFIED;

	public static PersistenceUnitCachingType fromValue(String v)
	{
		return valueOf(v);
	}

	public String value()
	{
		return name();
	}

}
