module com.jwebmp.guicedpersistence {
	exports com.jwebmp.guicedpersistence.db;
	exports com.jwebmp.guicedpersistence.db.annotations;
	exports com.jwebmp.guicedpersistence.db.exceptions;

	requires com.google.guice.extensions.persist;
	requires com.jwebmp.guicedinjection;
	requires com.jwebmp.logmaster;
	requires io.github.lukehutch.fastclasspathscanner;
	requires java.logging;
	requires com.google.guice;

	requires java.sql;
	requires java.naming;
	requires aopalliance;

	requires java.validation;

	requires java.persistence;
	requires com.google.common;
	requires javax.inject;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.module.jaxb;
	requires com.fasterxml.jackson.dataformat.xml;

	uses  com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
	uses  com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;

	provides com.jwebmp.guicedinjection.scanners.PackageContentsScanner with com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
	provides com.jwebmp.guicedinjection.scanners.FileContentsScanner with com.jwebmp.guicedpersistence.db.PersistenceFileHandler;

	provides com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader with  com.jwebmp.guicedpersistence.db.intercepter.JPADefaultConnectionBaseBuilder;
	provides com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader with  com.jwebmp.guicedpersistence.db.intercepter.HibernateDefaultConnectionBaseBuilder;
	provides com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader with  com.jwebmp.guicedpersistence.db.intercepter.EclipseLinkDefaultConnectionBaseBuilder;
}
