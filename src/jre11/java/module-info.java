import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;

module com.guicedee.guicedpersistence {
	exports com.guicedee.guicedpersistence.db;
	exports com.guicedee.guicedpersistence.db.annotations;
	exports com.guicedee.guicedpersistence.db.exceptions;
	exports com.guicedee.guicedpersistence.db.intercepters;
	exports com.guicedee.guicedpersistence.services;

	exports com.guicedee.guicedpersistence.scanners;

	exports com.oracle.jaxb21;

	requires transitive com.google.guice.extensions.persist;
	requires transitive com.guicedee.guicedinjection;
	requires transitive java.xml.bind;
	requires transitive java.persistence;
	requires transitive java.transaction;

	requires org.json;
	requires net.bytebuddy;

	uses com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader;
	uses com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
	uses com.guicedee.guicedpersistence.services.ITransactionHandler;

	provides com.guicedee.guicedinjection.interfaces.IPathContentsScanner with com.guicedee.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
	provides com.guicedee.guicedinjection.interfaces.IPathContentsBlacklistScanner with com.guicedee.guicedpersistence.scanners.GuiceInjectionMetaInfScannerExclusions;
	provides com.guicedee.guicedinjection.interfaces.IFileContentsScanner with com.guicedee.guicedpersistence.scanners.PersistenceFileHandler;
	provides com.guicedee.guicedinjection.interfaces.IGuiceConfigurator with com.guicedee.guicedpersistence.db.services.PersistenceGuiceConfigurator;
	provides com.guicedee.guicedinjection.interfaces.IGuiceDefaultBinder with com.guicedee.guicedpersistence.scanners.PersistenceServiceLoadersBinder;
	provides IGuiceModule with PersistenceServicesModule;

	provides com.guicedee.guicedinjection.interfaces.IGuicePreDestroy with com.guicedee.guicedpersistence.implementations.GuicedPersistenceDestroyer;

	provides com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader with com.guicedee.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder;

	opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;
	opens com.guicedee.guicedpersistence.db to com.fasterxml.jackson.databind;
	opens com.guicedee.guicedpersistence.injectors to com.google.guice;
	opens com.guicedee.guicedpersistence.implementations  to com.google.guice;
}
