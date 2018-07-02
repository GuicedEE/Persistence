package com.oracle.jaxb21;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"persistenceUnit"
})
@XmlRootElement(name = "persistence", namespace = "http://xmlns.jcp.org/xml/ns/persistence")
public class Persistence
{

	@XmlElement(name = "persistence-unit", required = true)
	protected List<Persistence.PersistenceUnit> persistenceUnit;
	@XmlAttribute(name = "version", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String version;

	public List<Persistence.PersistenceUnit> getPersistenceUnit()
	{
		if (persistenceUnit == null)
		{
			persistenceUnit = new ArrayList<Persistence.PersistenceUnit>();
		}
		return this.persistenceUnit;
	}

	public String getVersion()
	{
		if (version == null)
		{
			return "2.1";
		}
		else
		{
			return version;
		}
	}

	public void setVersion(String value)
	{
		this.version = value;
	}


	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
			"description",
			"provider",
			"jtaDataSource",
			"nonJtaDataSource",
			"mappingFile",
			"jarFile",
			"clazz",
			"excludeUnlistedClasses",
			"sharedCacheMode",
			"validationMode",
			"properties"
	})
	public static class PersistenceUnit
	{

		protected String description;
		protected String provider;
		@XmlElement(name = "jta-data-source")
		protected String jtaDataSource;
		@XmlElement(name = "non-jta-data-source")
		protected String nonJtaDataSource;
		@XmlElement(name = "mapping-file")
		protected List<String> mappingFile;
		@XmlElement(name = "jar-file")
		protected List<String> jarFile;
		@XmlElement(name = "class")
		protected List<String> clazz;
		@XmlElement(name = "exclude-unlisted-classes", defaultValue = "true")
		protected Boolean excludeUnlistedClasses;
		@XmlElement(name = "shared-cache-mode")
		@XmlSchemaType(name = "token")
		protected PersistenceUnitCachingType sharedCacheMode;
		@XmlElement(name = "validation-mode")
		@XmlSchemaType(name = "token")
		protected PersistenceUnitValidationModeType validationMode;
		protected Persistence.PersistenceUnit.Properties properties;
		@XmlAttribute(name = "name", required = true)
		protected String name;
		@XmlAttribute(name = "transaction-type")
		protected PersistenceUnitTransactionType transactionType;

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String value)
		{
			this.description = value;
		}

		public String getProvider()
		{
			return provider;
		}

		public void setProvider(String value)
		{
			this.provider = value;
		}

		public String getJtaDataSource()
		{
			return jtaDataSource;
		}

		public void setJtaDataSource(String value)
		{
			this.jtaDataSource = value;
		}

		public String getNonJtaDataSource()
		{
			return nonJtaDataSource;
		}

		public void setNonJtaDataSource(String value)
		{
			this.nonJtaDataSource = value;
		}

		public List<String> getMappingFile()
		{
			if (mappingFile == null)
			{
				mappingFile = new ArrayList<String>();
			}
			return this.mappingFile;
		}

		public List<String> getJarFile()
		{
			if (jarFile == null)
			{
				jarFile = new ArrayList<String>();
			}
			return this.jarFile;
		}

		public List<String> getClazz()
		{
			if (clazz == null)
			{
				clazz = new ArrayList<String>();
			}
			return this.clazz;
		}

		public Boolean isExcludeUnlistedClasses()
		{
			return excludeUnlistedClasses;
		}

		public void setExcludeUnlistedClasses(Boolean value)
		{
			this.excludeUnlistedClasses = value;
		}

		public PersistenceUnitCachingType getSharedCacheMode()
		{
			return sharedCacheMode;
		}

		public void setSharedCacheMode(PersistenceUnitCachingType value)
		{
			this.sharedCacheMode = value;
		}

		public PersistenceUnitValidationModeType getValidationMode()
		{
			return validationMode;
		}

		public void setValidationMode(PersistenceUnitValidationModeType value)
		{
			this.validationMode = value;
		}

		public Persistence.PersistenceUnit.Properties getProperties()
		{
			return properties;
		}

		public void setProperties(Persistence.PersistenceUnit.Properties value)
		{
			this.properties = value;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String value)
		{
			this.name = value;
		}

		public PersistenceUnitTransactionType getTransactionType()
		{
			return transactionType;
		}

		public void setTransactionType(PersistenceUnitTransactionType value)
		{
			this.transactionType = value;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "", propOrder = {
				"property"
		})
		public static class Properties
		{

			protected List<Persistence.PersistenceUnit.Properties.Property> property;

			public List<Persistence.PersistenceUnit.Properties.Property> getProperty()
			{
				if (property == null)
				{
					property = new ArrayList<Persistence.PersistenceUnit.Properties.Property>();
				}
				return this.property;
			}

			@XmlAccessorType(XmlAccessType.FIELD)
			@XmlType(name = "")
			public static class Property
			{

				@XmlAttribute(name = "name", required = true)
				protected String name;
				@XmlAttribute(name = "value", required = true)
				protected String value;

				public String getName()
				{
					return name;
				}

				public void setName(String value)
				{
					this.name = value;
				}

				public String getValue()
				{
					return value;
				}

				public void setValue(String value)
				{
					this.value = value;
				}

			}

		}

	}

}
