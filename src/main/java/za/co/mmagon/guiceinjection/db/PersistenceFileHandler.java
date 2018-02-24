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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class PersistenceFileHandler implements FileContentsScanner, PackageContentsScanner
{
	private static final Logger log = LogFactory.getLog("PersistenceFileHandler");
	private static final Set<Persistence.PersistenceUnit> persistenceUnits = new HashSet<>();
	private static ExecutorService persistenceContextExecutorService = Executors.newSingleThreadExecutor();

	public PersistenceFileHandler()
	{
		log.config("Loading JAXB JPA 2.1 Persistence Context");
		Runnable loadAsync = () ->
		{
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
		};
		persistenceContextExecutorService.execute(loadAsync);
		persistenceContextExecutorService.shutdown();
	}

	@Override
	public Map<String, FileMatchContentsProcessorWithContext> onMatch()
	{
		Map<String, FileMatchContentsProcessorWithContext> map = new HashMap<>();

		log.config("Persistence Units Loading... ");
		FileMatchContentsProcessorWithContext processor = (classpathElt, relativePath, fileContents) ->
		{
			log.config("Found " + relativePath + " - " + classpathElt.getCanonicalPath());
			if (!getPersistenceContextExecutorService().isShutdown())
			{
				getPersistenceContextExecutorService().shutdown();
				try
				{
					getPersistenceContextExecutorService().awaitTermination(5, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					log.log(Level.SEVERE, "Unable to wait for persistence jaxb context to load..", e);
					throw new NoConnectionInfoException("JAXB Not able to load persistence file, Thread interrupted", e);
				}
			}
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
			try
			{
				persistenceContextExecutorService.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "Unable to get persistence context from the service. Timed Out while building", e);
				throw new NoConnectionInfoException("Unable to get persistence context from service, Thread interrupted", e);
			}
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
	public Set<String> searchFor()
	{
		return new HashSet<>();
	}

	public static ExecutorService getPersistenceContextExecutorService()
	{
		return persistenceContextExecutorService;
	}
}
