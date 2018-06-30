package com.jwebmp.guicedinjection.db;

import com.jwebmp.guicedinjection.exceptions.NoConnectionInfoException;
import com.jwebmp.guicedinjection.scanners.FileContentsScanner;
import com.jwebmp.guicedinjection.scanners.PackageContentsScanner;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.Persistence;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessorWithContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
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
	private static final Set<Persistence.PersistenceUnit> persistenceUnits = new HashSet<>();

	public PersistenceFileHandler()
	{
		//No Config Required
	}

	/**
	 * Returns all the persistence units that were found or loaded
	 *
	 * @return
	 */
	public static Set<Persistence.PersistenceUnit> getPersistenceUnits()
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
	private Set<Persistence.PersistenceUnit> getPersistenceUnitFromFile(byte[] persistenceFile, String persistenceFileName)
	{
		Set<Persistence.PersistenceUnit> units = new HashSet<>();
		if (GuicedPersistenceBinding.getPersistenceContext() == null)
		{
			loadJPA22();
		}
		JAXBContext pContext = GuicedPersistenceBinding.getPersistenceContext();
		String content = new String(persistenceFile);
		try
		{
			Persistence p = (Persistence) pContext.createUnmarshaller()
			                                      .unmarshal(new StringReader(content));
			for (Persistence.PersistenceUnit persistenceUnit : p.getPersistenceUnit())
			{
				units.add(persistenceUnit);
			}
		}
		catch (Exception e)
		{
			log.log(Level.WARNING, "Persistence File does not look like a JPA2.1 or higher - " + persistenceFileName);
			log.log(Level.FINER, "Unable to get the persistence xsd object", e);
		}
		return units;
	}

	private static void loadJPA22()
	{
		log.config("Loading JAXB JPA 2.1 Persistence Context");
		try
		{
			GuicedPersistenceBinding.persistenceContext = JAXBContext.newInstance(Persistence.class);
			log.config("Loaded Persistence JAXB Context");
		}
		catch (JAXBException e)
		{
			log.log(Level.WARNING, "JPA2.1 context unable to load. Check the header of the persistence.xml file", e);
			log.log(Level.FINER, "Unable to load Persistence Context JPA 2.1", e);
			throw new NoConnectionInfoException("Persistence Unit Load Failed", e);
		}
	}

	@Override
	public Set<String> searchFor()
	{
		return new HashSet<>();
	}
}
