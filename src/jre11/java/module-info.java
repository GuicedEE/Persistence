import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder;
import com.guicedee.guicedpersistence.db.services.PersistenceGuiceConfigurator;
import com.guicedee.guicedpersistence.implementations.GuicedPersistenceDestroyer;
import com.guicedee.guicedpersistence.implementations.GuicedPersistenceJarModuleExclusions;
import com.guicedee.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
import com.guicedee.guicedpersistence.scanners.GuiceInjectionMetaInfScannerExclusions;
import com.guicedee.guicedpersistence.scanners.PersistenceFileHandler;
import com.guicedee.guicedpersistence.scanners.PersistenceServiceLoadersBinder;
import com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
import com.guicedee.guicedpersistence.services.ITransactionHandler;

module com.guicedee.guicedpersistence {
	exports com.guicedee.guicedpersistence.db;
	exports com.guicedee.guicedpersistence.db.annotations;
	exports com.guicedee.guicedpersistence.db.exceptions;
	exports com.guicedee.guicedpersistence.db.intercepters;
	exports com.guicedee.guicedpersistence.services;

	exports com.guicedee.guicedpersistence.scanners;

	exports com.oracle.jaxb21;

	requires com.google.guice.extensions.persist;
	requires com.guicedee.guicedinjection;
	requires com.guicedee.logmaster;

	requires io.github.classgraph;

	requires java.logging;
	requires com.google.guice;

	requires java.xml.bind;

	requires java.naming;
	requires aopalliance;

	requires java.validation;

	requires com.google.common;
	requires javax.inject;

	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	requires java.persistence;

	requires org.json;
	requires java.sql;
	requires java.transaction;
	requires net.bytebuddy;

	uses IPropertiesConnectionInfoReader;
	uses IPropertiesEntityManagerReader;
	uses ITransactionHandler;

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
	opens com.guicedee.guicedpersistence.db to com.fasterxml.jackson.databind;
	opens com.guicedee.guicedpersistence.injectors to com.google.guice;
	opens com.guicedee.guicedpersistence.implementations  to com.google.guice;
}
