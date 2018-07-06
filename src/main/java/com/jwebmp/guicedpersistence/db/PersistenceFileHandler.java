package com.jwebmp.guicedpersistence.db;

import com.jwebmp.guicedinjection.scanners.FileContentsScanner;
import com.jwebmp.guicedinjection.scanners.PackageContentsScanner;
import com.jwebmp.logger.LogFactory;
import com.oracle.jaxb21.*;
import com.thoughtworks.xstream.XStream;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessorWithContext;

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
	private Set<PersistenceUnit> getPersistenceUnitFromFile(byte[] persistenceFile, String persistenceFileName)
	{
		Set<PersistenceUnit> units = new HashSet<>();

		XStream xml = new XStream();
		xml.alias("persistence", Persistence.class);

		xml.aliasField("persistence-unit", Persistence.class, "persistenceUnit");
		xml.addImplicitCollection(Persistence.class, "persistenceUnit", PersistenceUnit.class);

		xml.useAttributeFor(PersistenceUnit.class, "name");
		xml.useAttributeFor(PersistenceUnit.class, "transactionType");
		xml.aliasField("transaction-type", PersistenceUnitTransactionType.class, "transactionType");

		xml.aliasField("jta-data-source", PersistenceUnit.class, "jtaDataSource");
		xml.aliasField("nonjta-data-source", PersistenceUnit.class, "nonJtaDataSource");
		xml.aliasField("exclude-unlisted-classes", PersistenceUnit.class, "excludeUnlistedClasses");
		xml.aliasField("jar-file", PersistenceUnit.class, "jarFile");
		xml.addImplicitCollection(PersistenceUnit.class, "jarFile", String.class);
		xml.aliasField("mapping-file", PersistenceUnit.class, "mappingFile");
		xml.addImplicitCollection(PersistenceUnit.class, "mappingFile", String.class);

		xml.aliasField("class", PersistenceUnit.class, "clazz");
		xml.addImplicitCollection(PersistenceUnit.class, "clazz", String.class);

		xml.useAttributeFor(Property.class, "name");
		xml.useAttributeFor(Property.class, "value");

		xml.alias("properties", Properties.class);
		xml.addImplicitCollection(Properties.class, "property", Property.class);
		try
		{
			Persistence p = (Persistence) xml.fromXML(new String(persistenceFile));
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

	@Override
	public Set<String> searchFor()
	{
		return new HashSet<>();
	}
}
