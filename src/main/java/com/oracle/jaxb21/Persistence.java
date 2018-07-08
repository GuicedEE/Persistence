package com.oracle.jaxb21;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
		getterVisibility = NONE,
		setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Persistence
{
	@JsonProperty(value = "persistence-unit",
			required = true)
	protected List<PersistenceUnit> persistenceUnit;
	@JsonProperty(value = "version",
			required = true)
	protected String version;
	/**
	 * The namespace
	 */
	@JsonProperty("xmlns")
	private List<String> xmlns;

	public List<PersistenceUnit> getPersistenceUnit()
	{
		if (persistenceUnit == null)
		{
			persistenceUnit = new ArrayList<>();
		}
		return persistenceUnit;
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
		version = value;
	}

	public List<String> getXmlns()
	{
		return xmlns;
	}

	public void setXmlns(List<String> xmlns)
	{
		this.xmlns = xmlns;
	}
}
