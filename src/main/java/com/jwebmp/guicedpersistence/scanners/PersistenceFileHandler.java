package com.jwebmp.guicedpersistence.scanners;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
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

@SuppressWarnings("unused")
public class PersistenceFileHandler
		implements IFileContentsScanner
{
	private static final Logger log = LogFactory.getLog("PersistenceFileHandler");
	private static final String ignorePersistenceUnitProperty = "guicedpersistence.ignore";
	private static final Set<PersistenceUnit> persistenceUnits = new HashSet<>();

	public PersistenceFileHandler()
	{
		//No Config Required
	}

	/**
	 * Returns all the persistence units that were found or loaded
	 *
	 * @return
	 */
	public static Set<PersistenceUnit> getPersistenceUnits()
	{
		return PersistenceFileHandler.persistenceUnits;
	}

	@Override
	public Map<String, ResourceList.ByteArrayConsumer> onMatch()
	{
		Map<String, ResourceList.ByteArrayConsumer> map = new HashMap<>();
		PersistenceFileHandler.log.info("Loading Persistence Units");
		ResourceList.ByteArrayConsumer processor = (resource, bytes) ->
		{
			Set<PersistenceUnit> units = getPersistenceUnitFromFile(bytes, resource.getPathRelativeToClasspathElement());
			for (Iterator<PersistenceUnit> iterator = units.iterator(); iterator.hasNext(); )
			{
				PersistenceUnit unit = iterator.next();
				for (Property property : unit.getProperties()
				                             .getProperty())
				{
					if (property.getName()
					            .equals(PersistenceFileHandler.ignorePersistenceUnitProperty) &&
					    property.getValue()
					            .equalsIgnoreCase("true"))
					{
						iterator.remove();
					}
				}
			}
			for (PersistenceUnit unit : units)
			{
				PersistenceFileHandler.log.config("Found Persistence Unit " + unit.getName() + " - JTA (" + Strings.isNullOrEmpty(unit.getJtaDataSource()) + ")");
				PersistenceFileHandler.persistenceUnits.add(unit);
			}
		};
		map.put("persistence.xml", processor);
		return map;
	}

	/**
	 * Gets all the persistence files
	 *
	 * @param persistenceFile
	 *
	 * @return
	 */
	private Set<PersistenceUnit> getPersistenceUnitFromFile(byte[] persistenceFile, String persistenceFileName)
	{
		Set<PersistenceUnit> units = new HashSet<>();
		try
		{
			String xml = new String(persistenceFile);
			JSONObject jsonObj = XML.toJSONObject(new String(persistenceFile));
			String json = String.valueOf(jsonObj);
			xml = replaceNameSpaceAttributes(xml);
			jsonObj = XML.toJSONObject(xml);

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

	private String replaceNameSpaceAttributes(String xml)
	{
		String replaced = xml;
		replaced = PersistenceFileHandler.removeAllXmlNamespace(replaced);
		replaced = replaced.replace(
				"xmlns=\"http://java.sun.com/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\"",
				"");
		replaced = replaced.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
		replaced = replaced.replace("xmlns=\"http://xmlns.jcp.org/xml/ns/persistence\"", "");
		replaced = replaced.replace("xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/persistence", "");
		replaced = replaced.replace("http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd\"", "");
		return replaced;
	}

	private static String removeAllXmlNamespace(String xmlData)
	{
		String xmlnsPattern = "\\s+xmlns\\s*(:\\w)?\\s*=\\s*\\\"(?<url>[^\\\"]*)\\\"";
		return xmlData.replaceAll(xmlnsPattern, "");
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj.getClass()
		       .equals(getClass()))
		{
			return true;
		}
		return false;
	}
}