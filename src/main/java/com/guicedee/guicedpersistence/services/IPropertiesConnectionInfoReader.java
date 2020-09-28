package com.guicedee.guicedpersistence.services;

import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;


import java.util.Properties;

/**
 * A functional interface to populate a connection base info based on properties received
 */
@FunctionalInterface
public interface IPropertiesConnectionInfoReader
{
	/**
	 * Method populateConnectionBaseInfo ...
	 *
	 * @param unit
	 * 		of type PersistenceUnit
	 * @param filteredProperties
	 * 		of type Properties
	 * @param cbi
	 * 		of type ConnectionBaseInfo
	 *
	 * @return ConnectionBaseInfo
	 */
	ConnectionBaseInfo populateConnectionBaseInfo(ParsedPersistenceXmlDescriptor unit, Properties filteredProperties, ConnectionBaseInfo cbi);
}
