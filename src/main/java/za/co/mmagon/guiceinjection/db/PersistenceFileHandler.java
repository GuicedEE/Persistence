package za.co.mmagon.guiceinjection.db;

import com.oracle.jaxb21.Persistence;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessorWithContext;
import za.co.mmagon.guiceinjection.exceptions.NoConnectionInfoException;
import za.co.mmagon.guiceinjection.scanners.FileContentsScanner;
import za.co.mmagon.guiceinjection.scanners.PackageContentsScanner;
import za.co.mmagon.logger.LogFactory;

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
			persistenceUnits.addAll(getPersistenceUnitFromFile(fileContents));
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
	private Set<Persistence.PersistenceUnit> getPersistenceUnitFromFile(byte[] persistenceFile)
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
			log.log(Level.SEVERE, "Unable to get the persistence xsd object", e);
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
			log.log(Level.SEVERE, "Unable to load Persistence Context JPA 2.1", e);
			throw new NoConnectionInfoException("Persistence Unit Load Failed", e);
		}
	}

	@Override
	public Set<String> searchFor()
	{
		return new HashSet<>();
	}
}
