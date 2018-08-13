import com.jwebmp.guicedinjection.interfaces.*;
import com.jwebmp.guicedpersistence.db.AsyncPostStartup;
import com.jwebmp.guicedpersistence.db.services.HibernateEntityManagerProperties;
import com.jwebmp.guicedpersistence.db.services.PersistenceGuiceConfigurator;
import com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
import com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScannerExclusions;
import com.jwebmp.guicedpersistence.scanners.PersistenceFileHandler;
import com.jwebmp.guicedpersistence.scanners.PersistenceServiceLoadersBinder;

module com.jwebmp.guicedpersistence {
	exports com.jwebmp.guicedpersistence.db;
	exports com.jwebmp.guicedpersistence.db.annotations;
	exports com.jwebmp.guicedpersistence.db.exceptions;
	exports com.jwebmp.guicedpersistence.db.intercepters;
	exports com.jwebmp.guicedpersistence.services;

	exports com.jwebmp.guicedpersistence.scanners to com.jwebmp.guicedinjection, io.github.classgraph;
	exports com.oracle.jaxb21;

	requires com.google.guice.extensions.persist;
	requires com.jwebmp.guicedinjection;
	requires com.jwebmp.logmaster;

	requires io.github.classgraph;
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
	requires org.hibernate.validator;

	uses com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
	uses com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;
	uses com.jwebmp.guicedpersistence.services.ITransactionHandler;
	uses com.jwebmp.guicedpersistence.services.IAsyncStartup;

	provides IPathContentsScanner with GuiceInjectionMetaInfScanner;
	provides IPathContentsBlacklistScanner with GuiceInjectionMetaInfScannerExclusions;
	provides IFileContentsScanner with PersistenceFileHandler;
	provides IGuiceConfigurator with PersistenceGuiceConfigurator;
	provides IGuicePostStartup with AsyncPostStartup;
	provides IGuiceDefaultBinder with PersistenceServiceLoadersBinder;

	provides com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader with HibernateEntityManagerProperties;

	provides com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader with com.jwebmp.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder,
			                                                                        com.jwebmp.guicedpersistence.db.intercepters.HibernateDefaultConnectionBaseBuilder,
			                                                                        com.jwebmp.guicedpersistence.db.intercepters.EclipseLinkDefaultConnectionBaseBuilder;

	opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.db to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.injectors to com.google.guice;

	exports com.jwebmp.guicedpersistence.db.services;
}
