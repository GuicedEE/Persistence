
package com.oracle.jaxb21;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "persistence-unit-transaction-type")
@XmlEnum
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
