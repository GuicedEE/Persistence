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
	requires jakarta.activation;

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

	uses com.jwebmp.guicedpersistence.services.IPropertiesConnectionInfoReader;
	uses com.jwebmp.guicedpersistence.services.IPropertiesEntityManagerReader;
	uses com.jwebmp.guicedpersistence.services.ITransactionHandler;

	provides com.jwebmp.guicedinjection.interfaces.IPathContentsScanner with com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
	provides com.jwebmp.guicedinjection.interfaces.IPathContentsBlacklistScanner with com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScannerExclusions;
	provides com.jwebmp.guicedinjection.interfaces.IFileContentsScanner with com.jwebmp.guicedpersistence.scanners.PersistenceFileHandler;
	provides com.jwebmp.guicedinjection.interfaces.IGuiceConfigurator with com.jwebmp.guicedpersistence.db.services.PersistenceGuiceConfigurator;
	//provides IGuicePostStartup with AsyncPostStartup;
	provides com.jwebmp.guicedinjection.interfaces.IGuiceDefaultBinder with com.jwebmp.guicedpersistence.scanners.PersistenceServiceLoadersBinder;

	provides com.jwebmp.guicedinjection.interfaces.IGuiceScanModuleExclusions with com.jwebmp.guicedpersistence.implementations.GuicedPersistenceJarModuleExclusions;
	provides com.jwebmp.guicedinjection.interfaces.IGuiceScanJarExclusions with com.jwebmp.guicedpersistence.implementations.GuicedPersistenceJarModuleExclusions;
	provides com.jwebmp.guicedinjection.interfaces.IGuicePreDestroy with com.jwebmp.guicedpersistence.implementations.GuicedPersistenceDestroyer;

	provides com.jwebmp.guicedpersistence.services.IPropertiesConnectionInfoReader with com.jwebmp.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder;

	opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.db to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.injectors to com.google.guice, cglib;
	opens com.jwebmp.guicedpersistence.implementations  to com.google.guice,cglib;
}
