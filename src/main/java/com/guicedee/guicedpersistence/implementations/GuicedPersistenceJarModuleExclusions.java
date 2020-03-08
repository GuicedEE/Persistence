package com.guicedee.guicedpersistence.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleExclusions;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class GuicedPersistenceJarModuleExclusions
		implements IGuiceScanModuleExclusions<GuicedPersistenceJarModuleExclusions>
{
	public GuicedPersistenceJarModuleExclusions()
	{
		//No config needed
	}

	@Override
	public @NotNull Set<String> excludeModules()
	{
		Set<String> strings = new HashSet<>();
		strings.add("com.guicedee.guicedpersistence");

		strings.add("com.google.guice.extensions.persist");
		strings.add("com.guicedee.guicedinjection");
		strings.add("com.guicedee.logmaster");

		strings.add("io.github.classgraph");
		strings.add("java.logging");
		strings.add("com.google.guice");

		strings.add("java.naming");
		strings.add("aopalliance");

		strings.add("java.validation");

		strings.add("com.google.common");
		strings.add("javax.inject");

		strings.add("com.fasterxml.jackson.core");
		strings.add("com.fasterxml.jackson.annotation");
		strings.add("com.fasterxml.jackson.databind");
		strings.add("jboss.logging");

		strings.add("java.persistence");
		strings.add("org.json");
		strings.add("java.sql");
		strings.add("java.transaction");
		return strings;
	}
}
