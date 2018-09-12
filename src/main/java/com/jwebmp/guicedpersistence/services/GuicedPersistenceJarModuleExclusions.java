package com.jwebmp.guicedpersistence.services;

import com.jwebmp.guicedinjection.interfaces.IGuiceScanJarExclusions;
import com.jwebmp.guicedinjection.interfaces.IGuiceScanModuleExclusions;

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
		strings.add("dom4j*");
		strings.add("guice-persist*");
		strings.add("hibernate-*");
		strings.add("jandex*");
		strings.add("javax.persistence-api*");
		strings.add("javax.transaction-api*");
		strings.add("javassist*");
		strings.add("jaxb-api*");
		strings.add("jboss-logging*");
		strings.add("json-*");
		return strings;
	}

	@Override
	public @NotNull Set<String> excludeModules()
	{
		Set<String> strings = new HashSet<>();
		strings.add("com.google.guice.extensions.persist");
		strings.add("java.naming");
		strings.add("aopalliance");
		strings.add("java.validation");
		strings.add("com.google.common");
		strings.add("javax.inject");
		strings.add("com.fasterxml.jackson.core");
		strings.add("com.fasterxml.jackson.annotation");
		strings.add("com.fasterxml.jackson.databind");
		strings.add("java.persistence");
		strings.add("org.json");
		strings.add("java.sql");
		strings.add("org.hibernate.validator");
		return strings;
	}
}
