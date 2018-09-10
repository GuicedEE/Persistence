package com.oracle.jaxb21;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
		getterVisibility = NONE,
		setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistenceUnit
		implements Comparable<PersistenceUnit>
{
	@JsonProperty("description")
	protected String description;
	@JsonProperty("provider")
	protected String provider;
	@JsonProperty("jta-data-source")
	protected String jtaDataSource;
	@JsonProperty("non-jta-data-source")
	protected String nonJtaDataSource;
	@JsonProperty("mapping-file")
	protected List<String> mappingFile;
	@JsonProperty("jar-file")
	protected List<String> jarFile;
	@JsonProperty("class")
	protected List<String> clazz;
	@JsonProperty(value = "exclude-unlisted-classes",
			defaultValue = "true")
	protected Boolean excludeUnlistedClasses;
	@JsonProperty("shared-cache-mode")
	protected PersistenceUnitCachingType sharedCacheMode;
	@JsonProperty("validation-mode")
	protected PersistenceUnitValidationModeType validationMode;
	@JsonProperty("properties")
	protected Properties properties;
	@JsonProperty(value = "name",
			required = true)
	protected String name;
	@JsonProperty("transaction-type")
	protected PersistenceUnitTransactionType transactionType;

	public PersistenceUnit()
	{
	}

	public PersistenceUnit(String provider)
	{
		this.provider = provider;
	}

	public List<String> getMappingFile()
	{
		if (mappingFile == null)
		{
			mappingFile = new ArrayList<>();
		}
		return mappingFile;
	}

	public void setMappingFile(List<String> mappingFile)
	{
		this.mappingFile = mappingFile;
	}

	public List<String> getJarFile()
	{
		if (jarFile == null)
		{
			jarFile = new ArrayList<>();
		}
		return jarFile;
	}

	public void setJarFile(List<String> jarFile)
	{
		this.jarFile = jarFile;
	}

	public List<String> getClazz()
	{
		if (clazz == null)
		{
			clazz = new ArrayList<>();
		}
		return clazz;
	}

	public void setClazz(List<String> clazz)
	{
		this.clazz = clazz;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getProvider()
	{
		return provider;
	}

	public void setProvider(String provider)
	{
		this.provider = provider;
	}

	public String getJtaDataSource()
	{
		return jtaDataSource;
	}

	public void setJtaDataSource(String jtaDataSource)
	{
		this.jtaDataSource = jtaDataSource;
	}

	public String getNonJtaDataSource()
	{
		return nonJtaDataSource;
	}

	public void setNonJtaDataSource(String nonJtaDataSource)
	{
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public Boolean getExcludeUnlistedClasses()
	{
		return excludeUnlistedClasses;
	}

	public void setExcludeUnlistedClasses(Boolean excludeUnlistedClasses)
	{
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}

	public PersistenceUnitCachingType getSharedCacheMode()
	{
		return sharedCacheMode;
	}

	public void setSharedCacheMode(PersistenceUnitCachingType sharedCacheMode)
	{
		this.sharedCacheMode = sharedCacheMode;
	}

	public PersistenceUnitValidationModeType getValidationMode()
	{
		return validationMode;
	}

	public void setValidationMode(PersistenceUnitValidationModeType validationMode)
	{
		this.validationMode = validationMode;
	}

	public Properties getProperties()
	{
		return properties;
	}

	public void setProperties(Properties properties)
	{
		this.properties = properties;
	}

	public PersistenceUnitTransactionType getTransactionType()
	{
		return transactionType;
	}

	public void setTransactionType(PersistenceUnitTransactionType transactionType)
	{
		this.transactionType = transactionType;
	}

	/**
	 * Method hashCode ...
	 *
	 * @return int
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(getName());
	}

	/**
	 * Method equals ...
	 *
	 * @param o
	 * 		of type Object
	 *
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof PersistenceUnit))
		{
			return false;
		}
		PersistenceUnit that = (PersistenceUnit) o;
		return Objects.equals(getName(), that.getName());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public int compareTo(PersistenceUnit o)
	{
		return getName().compareTo(o.getName());
	}
}
