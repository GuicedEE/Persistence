
package com.oracle.jaxb21;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "persistence-unit-validation-mode-type")
@XmlEnum
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
