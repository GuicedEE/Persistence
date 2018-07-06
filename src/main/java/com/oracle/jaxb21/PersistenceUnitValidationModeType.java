
package com.oracle.jaxb21;

public enum PersistenceUnitValidationModeType
{

	AUTO,
	CALLBACK,
	NONE;

	public static PersistenceUnitValidationModeType fromValue(String v)
	{
		return valueOf(v);
	}

	public String value()
	{
		return name();
	}

}
