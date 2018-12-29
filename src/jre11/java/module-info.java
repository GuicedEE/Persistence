import com.jwebmp.guicedinjection.interfaces.*;
import com.jwebmp.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder;
import com.jwebmp.guicedpersistence.db.services.PersistenceGuiceConfigurator;
import com.jwebmp.guicedpersistence.implementations.GuicedPersistenceDestroyer;
import com.jwebmp.guicedpersistence.implementations.GuicedPersistenceJarModuleExclusions;
import com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
import com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScannerExclusions;
import com.jwebmp.guicedpersistence.scanners.PersistenceFileHandler;
import com.jwebmp.guicedpersistence.scanners.PersistenceServiceLoadersBinder;
import com.jwebmp.guicedpersistence.services.IPropertiesConnectionInfoReader;

module com.jwebmp.guicedpersistence {
	exports com.jwebmp.guicedpersistence.db;
	exports com.jwebmp.guicedpersistence.db.annotations;
	exports com.jwebmp.guicedpersistence.db.exceptions;
	exports com.jwebmp.guicedpersistence.db.intercepters;
	exports com.jwebmp.guicedpersistence.services;

	exports com.jwebmp.guicedpersistence.scanners to com.jwebmp.guicedinjection, io.github.classgraph, com.jwebmp.entityassist;
	exports com.oracle.jaxb21;

	requires com.google.guice.extensions.persist;
	requires com.jwebmp.guicedinjection;
	requires com.jwebmp.logmaster;

	requires io.github.classgraph;

	requires java.logging;
	requires com.google.guice;

	requires java.xml.bind;

	requires java.naming;
	requires aopalliance;

	requires java.validation;
	requires java.activation;

	requires com.google.common;
	requires javax.inject;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	requires java.persistence;

	requires org.json;
	requires java.sql;
	requires java.transaction;

	uses IPropertiesConnectionInfoReader;
	uses com.jwebmp.guicedpersistence.services.IPropertiesEntityManagerReader;
	uses com.jwebmp.guicedpersistence.services.ITransactionHandler;

	provides IPathContentsScanner with GuiceInjectionMetaInfScanner;
	provides IPathContentsBlacklistScanner with GuiceInjectionMetaInfScannerExclusions;
	provides IFileContentsScanner with PersistenceFileHandler;
	provides IGuiceConfigurator with PersistenceGuiceConfigurator;
	//provides IGuicePostStartup with AsyncPostStartup;
	provides IGuiceDefaultBinder with PersistenceServiceLoadersBinder;

	provides IGuiceScanModuleExclusions with GuicedPersistenceJarModuleExclusions;
	provides IGuiceScanJarExclusions with GuicedPersistenceJarModuleExclusions;
	provides IGuicePreDestroy with GuicedPersistenceDestroyer;

	provides IPropertiesConnectionInfoReader with JPADefaultConnectionBaseBuilder;

	opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.db to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.injectors to com.google.guice, cglib;
}
