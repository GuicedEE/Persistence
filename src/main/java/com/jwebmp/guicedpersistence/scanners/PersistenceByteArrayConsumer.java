package com.guicedee.guicedpersistence.scanners;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.logger.LogFactory;
import com.oracle.jaxb21.Persistence;
import com.oracle.jaxb21.PersistenceContainer;
import com.oracle.jaxb21.PersistenceUnit;
import com.oracle.jaxb21.Property;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import org.json.JSONObject;
import org.json.XML;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A consumer that reads persistence.xml files into PersistenceUnit objects
 */
public class PersistenceByteArrayConsumer
		implements ResourceList.ByteArrayConsumer
{
	/**
	 * The logger
	 */
	private static final Logger log = LogFactory.getLog("PersistenceByteArrayConsumer");

	/**
	 * Object mapper reader for Persistence XML Files
	 */
	private static final ObjectMapper om = new ObjectMapper();

	static
	{
		om.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	/**
	 * Method accept ...
	 *
	 * @param resource
	 * 		of type Resource
	 * @param byteArray
	 * 		of type byte[]
	 */
	@Override
	public void accept(Resource resource, byte[] byteArray)
	{
		Set<PersistenceUnit> units = getPersistenceUnitsFromFile(byteArray);
		for (Iterator<PersistenceUnit> iterator = units.iterator(); iterator.hasNext(); )
		{
			PersistenceUnit unit = iterator.next();
			for (Property property : unit.getProperties()
			                             .getProperty())
			{
				if (property.getName()
				            .equals(PersistenceFileHandler.getIgnorePersistenceUnitProperty()) &&
				    "true".equalsIgnoreCase(property.getValue()))
				{

					iterator.remove();
				}
			}
			PersistenceFileHandler.getPersistenceUnits()
			                      .add(unit);
		}
		resource.close();
	}

	/**
	 * Gets all the persistence files
	 *
	 * @param persistenceFile
	 * 		The persistence file bytes
	 *
	 * @return A set of persistence units
	 */
	private Set<PersistenceUnit> getPersistenceUnitsFromFile(byte[] persistenceFile)
	{
		Set<PersistenceUnit> units = new TreeSet<>();
		try
		{
			String xml = new String(persistenceFile);
			JSONObject jsonObj = XML.toJSONObject(xml);
			PersistenceContainer pp = om.readValue(jsonObj.toString(), PersistenceContainer.class);
			Persistence p = pp.getPersistence();
			units.addAll(p.getPersistenceUnit());
		}
		catch (Throwable t)
		{
			log.log(Level.SEVERE, "Error streaming", t);
		}
		return units;
	}
}
