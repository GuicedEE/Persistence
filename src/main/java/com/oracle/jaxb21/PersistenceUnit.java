package com.oracle.jaxb21;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
		getterVisibility = NONE,
		setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class PersistenceUnit
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

	public List<String> getJarFile()
	{
		if (jarFile == null)
		{
			jarFile = new ArrayList<>();
		}
		return jarFile;
	}

	public List<String> getClazz()
	{
		if (clazz == null)
		{
			clazz = new ArrayList<>();
		}
		return clazz;
	}
}
