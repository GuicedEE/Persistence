package com.oracle.jaxb21;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
		getterVisibility = NONE,
		setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistenceContainer
{
	@JsonProperty("persistence")
	private Persistence persistence;

	public PersistenceContainer()
	{
		//No Config Required
	}

	/**
	 * Returns the wrapped persistence object
	 *
	 * @return The persistence object
	 */
	public Persistence getPersistence()
	{
		return persistence;
	}

	/**
	 * Sets the persistence object of this container
	 *
	 * @param persistence
	 * 		The persistence
	 *
	 * @return The persistence container
	 */
	@NotNull
	public PersistenceContainer setPersistence(Persistence persistence)
	{
		this.persistence = persistence;
		return this;
	}
}
