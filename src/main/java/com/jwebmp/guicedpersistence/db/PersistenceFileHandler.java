package com.jwebmp.guicedpersistence.db;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwebmp.guicedinjection.scanners.FileContentsScanner;
import com.jwebmp.guicedinjection.scanners.PackageContentsScanner;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.Persistence;
import com.oracle.jaxb21.PersistenceContainer;
import com.oracle.jaxb21.PersistenceUnit;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessorWithContext;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class PersistenceFileHandler
		implements FileContentsScanner, PackageContentsScanner
{
	private static final Logger log = LogFactory.getLog("PersistenceFileHandler");
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
		return persistenceUnits;
	}

	@Override
	public Map<String, FileMatchContentsProcessorWithContext> onMatch()
	{
		Map<String, FileMatchContentsProcessorWithContext> map = new HashMap<>();

		log.config("Persistence Units Loading... ");
		FileMatchContentsProcessorWithContext processor = (classpathElt, relativePath, fileContents) ->
		{
			log.config("Found " + relativePath + " - " + classpathElt.getCanonicalPath());
			persistenceUnits.addAll(getPersistenceUnitFromFile(fileContents, "Found " + relativePath + " - " + classpathElt.getCanonicalPath()));
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
	private Set<PersistenceUnit> getPersistenceUnitFromFile(byte[] persistenceFile, String persistenceFileName) throws IOException
	{
		Set<PersistenceUnit> units = new HashSet<>();
		String xml = new String(persistenceFile);

		JSONObject jsonObj = XML.toJSONObject(new String(persistenceFile));
		String json = String.valueOf(jsonObj);
		xml = replaceNameSpaceAttributes(xml);
		jsonObj = XML.toJSONObject(xml);

		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		PersistenceContainer pp = om.readValue(jsonObj.toString(), PersistenceContainer.class);

		try
		{
			Persistence p = pp.getPersistence();
			for (PersistenceUnit persistenceUnit : p.getPersistenceUnit())
			{
				units.add(persistenceUnit);
			}
		}
		catch (Throwable t)
		{
			log.log(Level.SEVERE, "Error streaming", t);
		}
		return units;
	}

	private String replaceNameSpaceAttributes(String xml)
	{
		String replaced = xml;
		replaced = replaced.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
		replaced = replaced.replace("xmlns=\"http://xmlns.jcp.org/xml/ns/persistence\"", "");
		replaced = replaced.replace("xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/persistence", "");
		replaced = replaced.replace("http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd\"", "");
		return replaced;
	}

	@Override
	public Set<String> searchFor()
	{
		return new HashSet<>();
	}
}
