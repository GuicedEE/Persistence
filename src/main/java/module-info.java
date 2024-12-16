import com.guicedee.guicedinjection.interfaces.IGuiceConfigurator;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.btm.implementation.BTMConnectionProperties;
import com.guicedee.guicedpersistence.btm.implementation.BTMDestroyer;
import com.guicedee.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder;
import com.guicedee.guicedpersistence.implementations.GuicedConfigurator;
import com.guicedee.guicedpersistence.readers.hibernateproperties.HibernateDefaultConnectionBaseBuilder;
import com.guicedee.guicedpersistence.readers.hibernateproperties.HibernateEntityManagerProperties;
import com.guicedee.guicedpersistence.readers.systemproperties.SystemEnvironmentVariablesPropertiesReader;
import com.guicedee.guicedpersistence.scanners.PersistenceServiceLoadersBinder;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;

module com.guicedee.guicedpersistence {
	exports com.guicedee.guicedpersistence.db;
    exports com.guicedee.guicedpersistence.db.exceptions;
	exports com.guicedee.guicedpersistence.db.intercepters;
	exports com.guicedee.guicedpersistence.services;
	exports com.guicedee.guicedpersistence.readers.hibernateproperties;
	exports com.guicedee.guicedpersistence.btm;
	exports com.guicedee.guicedpersistence.btm.implementation;
	exports com.guicedee.guicedpersistence.jta;
	exports com.guicedee.guicedpersistence.scanners;
	exports com.guicedee.guicedpersistence.lambda;

	exports com.guicedee.guicedpersistence.implementations;

	requires transitive com.google.guice.extensions.persist;
	requires transitive com.guicedee.client;
	requires transitive jakarta.xml.bind;
	requires transitive jakarta.persistence;
	requires transitive jakarta.transaction;
	requires transitive jakarta.validation;
	requires transitive org.hibernate.orm.core;
	requires tm.bitronix.btm;

	requires java.naming;
	requires transitive java.sql;
	requires static lombok;
    requires java.logging;


    uses com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader;
	uses com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;

	provides com.guicedee.guicedinjection.interfaces.IGuicePostStartup with com.guicedee.guicedpersistence.implementations.EntityManagerPostStartup;
	provides IGuiceModule with PersistenceServicesModule, PersistenceServiceLoadersBinder;

	provides IPropertiesEntityManagerReader with BTMConnectionProperties,
			                                        HibernateEntityManagerProperties,
			                                        SystemEnvironmentVariablesPropertiesReader;

	provides com.guicedee.guicedinjection.interfaces.IGuicePreDestroy with com.guicedee.guicedpersistence.implementations.GuicedPersistenceDestroyer, BTMDestroyer;

	provides com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader with JPADefaultConnectionBaseBuilder,
			                                                                                 HibernateDefaultConnectionBaseBuilder;
	
	provides IGuiceConfigurator with GuicedConfigurator;
	
	//provides com.guicedee.guicedinjection.interfaces.IFileContentsScanner with PersistenceFileHandler;
	//provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with PersistenceGuiceConfigurator;
//	provides com.guicedee.guicedinjection.interfaces.IPathContentsRejectListScanner with GuiceInjectionMetaInfScannerExclusions;
	//provides com.guicedee.guicedinjection.interfaces.IPathContentsScanner with GuiceInjectionMetaInfScanner;
	
	
	//opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;
	opens com.guicedee.guicedpersistence.db to com.fasterxml.jackson.databind;
	//opens com.guicedee.guicedpersistence.injectors to com.google.guice;
	opens com.guicedee.guicedpersistence.implementations to com.google.guice,com.fasterxml.jackson.databind;
	opens com.guicedee.guicedpersistence.jta to com.google.guice;
	opens com.guicedee.guicedpersistence.lambda to com.google.guice,com.fasterxml.jackson.databind,org.hibernate.orm.core;
}
