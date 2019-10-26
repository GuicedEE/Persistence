package com.guicedee.guicedpersistence.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanJarExclusions;
import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleExclusions;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class GuicedPersistenceJarModuleExclusions
		implements IGuiceScanJarExclusions<GuicedPersistenceJarModuleExclusions>,
				           IGuiceScanModuleExclusions<GuicedPersistenceJarModuleExclusions>
{
	public GuicedPersistenceJarModuleExclusions()
	{
		//No config needed
	}

	@Override
	public @NotNull Set<String> excludeJars()
	{
		Set<String> strings = new HashSet<>();
		strings.add("guiced-persistence-*");

		strings.add("byte-buddy-*");

		strings.add("dom4j-*");
		strings.add("guice-persist-*");

		strings.add("hibernate-core-*");
		strings.add("hibernate-commons-annotations-*");
		strings.add("hibernate-jcache-*");
		strings.add("hibernate-jpamodelgen-*");
		strings.add("hibernate-validator-*");

		strings.add("javax.persistence-*");

		strings.add("javax.transaction-api-*");
		strings.add("javax.persistence-api-*");

		strings.add("jaxb-api-*");
		strings.add("jboss-logging-*");

		return strings;
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
