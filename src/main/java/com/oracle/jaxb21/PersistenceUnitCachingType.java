
package com.oracle.jaxb21;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "persistence-unit-caching-type")
@XmlEnum
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
