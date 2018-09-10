package com.jwebmp.guicedpersistence.scanners;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwebmp.guicedinjection.interfaces.IFileContentsScanner;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.Persistence;
import com.oracle.jaxb21.PersistenceContainer;
import com.oracle.jaxb21.PersistenceUnit;
import com.oracle.jaxb21.Property;
import io.github.classgraph.ResourceList;
import org.json.JSONObject;
import org.json.XML;

import java.util.*;
import java.util.logging.Level;
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
	 * Returns a contents processer to run on match
	 *
	 * @return the maps of file identifiers and contents
	 */
	@Override
	public Map<String, ResourceList.ByteArrayConsumer> onMatch()
	{
		Map<String, ResourceList.ByteArrayConsumer> map = new HashMap<>();
		PersistenceFileHandler.log.info("Loading Persistence Units");
		ResourceList.ByteArrayConsumer processor = buildPersistenceByteArrayConsumer();
		map.put("persistence.xml", processor);
		return map;
	}

	/**
	 * Method buildPersistenceByteArrayConsumer ...
	 *
	 * @return ByteArrayConsumer
	 */
	private ResourceList.ByteArrayConsumer buildPersistenceByteArrayConsumer()
	{
		return (resource, byteArray) ->
		{
			Set<PersistenceUnit> units = getPersistenceUnitFromFile(byteArray, resource.getPathRelativeToClasspathElement());
			for (Iterator<PersistenceUnit> iterator = units.iterator(); iterator.hasNext(); )
			{
				PersistenceUnit unit = iterator.next();
				for (Property property : unit.getProperties()
				                             .getProperty())
				{
					if (property.getName()
					            .equals(PersistenceFileHandler.ignorePersistenceUnitProperty) &&
					    "true".equalsIgnoreCase(property.getValue()))
					{

						iterator.remove();
					}
					else
					{
						PersistenceFileHandler.persistenceUnits.add(unit);
					}
				}
			}
		};
	}

	/**
	 * Gets all the persistence files
	 *
	 * @param persistenceFile
	 * 		The persistence file bytes
	 * @param persistenceFileName
	 * 		The filename
	 *
	 * @return A set of persistence units
	 */
	private Set<PersistenceUnit> getPersistenceUnitFromFile(byte[] persistenceFile, String persistenceFileName)
	{
		Set<PersistenceUnit> units = new HashSet<>();
		try
		{
			String xml = new String(persistenceFile);
			JSONObject jsonObj = XML.toJSONObject(xml);

			ObjectMapper om = new ObjectMapper();
			om.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			PersistenceContainer pp = om.readValue(jsonObj.toString(), PersistenceContainer.class);

			Persistence p = pp.getPersistence();
			units.addAll(p.getPersistenceUnit());
		}
		catch (Throwable t)
		{
			PersistenceFileHandler.log.log(Level.SEVERE, "Error streaming", t);
		}
		return units;
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
