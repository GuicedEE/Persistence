package com.guicedee.guicedpersistence.scanners;

import com.guicedee.guicedinjection.interfaces.IFileContentsScanner;
import com.guicedee.logger.LogFactory;
import com.oracle.jaxb21.PersistenceUnit;
import io.github.classgraph.ResourceList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Loads persistence units from persistence files as found on the registered classpath
 */
@SuppressWarnings("unused")
public class PersistenceFileHandler
		implements IFileContentsScanner
{
	/**
	 * The logger
	 */
	private static final Logger log = LogFactory.getLog("PersistenceFileHandler");
	/**
	 * The property that marks a persistence unit as ignore
	 */
	private static final String ignorePersistenceUnitProperty = "guicedpersistence.ignore";
	/**
	 * A list of all registered persistence units
	 */
	private static final Set<PersistenceUnit> persistenceUnits = new HashSet<>();

	/**
	 * A new persistence file handler
	 */
	public PersistenceFileHandler()
	{
		//No Config Required
	}

	/**
	 * Returns all the persistence units that were found or loaded
	 *
	 * @return A set of persistence units
	 */
	public static Set<PersistenceUnit> getPersistenceUnits()
	{
		return PersistenceFileHandler.persistenceUnits;
	}

	/**
	 * Method getIgnorePersistenceUnitProperty returns the ignorePersistenceUnitProperty of this PersistenceFileHandler object.
	 * <p>
	 * The property that marks a persistence unit as ignore
	 *
	 * @return the ignorePersistenceUnitProperty (type String) of this PersistenceFileHandler object.
	 */
	@SuppressWarnings("WeakerAccess")
	public static String getIgnorePersistenceUnitProperty()
	{
		return ignorePersistenceUnitProperty;
	}

	/**
	 * Returns a contents processer to run on match
	 *
	 * @return the maps of file identifiers and contents
	 */
	@Override
	public Map<String, ResourceList.ByteArrayConsumer> onMatch()
	{
		Map<String, ResourceList.ByteArrayConsumer> map = new HashMap<>();
		PersistenceFileHandler.log.info("Loading Persistence Unit Byte Array Consumer");
		ResourceList.ByteArrayConsumer processor = new PersistenceByteArrayConsumer();
		map.put("persistence.xml", processor);
		return map;
	}

	/**
	 * Method hashCode ...
	 *
	 * @return int
	 */
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	/**
	 * Method equals ...
	 *
	 * @param obj
	 * 		of type Object
	 *
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		return obj.getClass()
		          .equals(getClass());
	}
}
