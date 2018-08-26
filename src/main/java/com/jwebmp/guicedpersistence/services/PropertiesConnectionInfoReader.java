package com.jwebmp.guicedpersistence.services;

import com.jwebmp.guicedpersistence.db.ConnectionBaseInfo;
import com.oracle.jaxb21.PersistenceUnit;

import java.util.Properties;

/**
 * A functional interface to populate a connection base info based on properties received
 */
@FunctionalInterface
public interface PropertiesConnectionInfoReader
{
	ConnectionBaseInfo populateConnectionBaseInfo(PersistenceUnit unit, Properties filteredProperties, ConnectionBaseInfo cbi);
}
