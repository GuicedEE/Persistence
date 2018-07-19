import com.jwebmp.guicedpersistence.services.ITransactionHandler;

module com.jwebmp.guicedpersistence {
	exports com.jwebmp.guicedpersistence.db;
	exports com.jwebmp.guicedpersistence.db.annotations;
	exports com.jwebmp.guicedpersistence.db.exceptions;

	exports com.jwebmp.guicedpersistence.db.intercepters to com.jwebmp.guicedinjection;
	exports com.jwebmp.guicedpersistence.scanners to com.jwebmp.guicedinjection, io.github.lukehutch.fastclasspathscanner;
	exports com.jwebmp.guicedpersistence.db.connectionbasebuilders;
	exports com.oracle.jaxb21;
	exports com.jwebmp.guicedpersistence.services;

	requires com.google.guice.extensions.persist;
	requires com.jwebmp.guicedinjection;
	requires com.jwebmp.logmaster;

	requires io.github.lukehutch.fastclasspathscanner;
	requires java.logging;
	requires com.google.guice;

	requires java.naming;
	requires aopalliance;

	requires java.validation;

	requires com.google.common;
	requires javax.inject;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	requires java.persistence;
	requires json;
	requires java.sql;

	uses com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
	uses com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;
	uses ITransactionHandler;

	provides com.jwebmp.guicedinjection.scanners.PackageContentsScanner with com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
	provides com.jwebmp.guicedinjection.scanners.FileContentsScanner with com.jwebmp.guicedpersistence.db.PersistenceFileHandler;

	provides com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader with com.jwebmp.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder,
			                                                                        com.jwebmp.guicedpersistence.db.intercepters.HibernateDefaultConnectionBaseBuilder,
			                                                                        com.jwebmp.guicedpersistence.db.intercepters.EclipseLinkDefaultConnectionBaseBuilder;

	opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;

	opens com.jwebmp.guicedpersistence.injectors to com.google.guice;
}
