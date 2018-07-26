import com.jwebmp.guicedinjection.interfaces.IFileContentsScanner;
import com.jwebmp.guicedinjection.interfaces.IGuiceConfigurator;
import com.jwebmp.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.guicedinjection.interfaces.IPackageContentsScanner;
import com.jwebmp.guicedpersistence.db.DBStartupAsyncPostStartup;
import com.jwebmp.guicedpersistence.db.PersistenceFileHandler;
import com.jwebmp.guicedpersistence.db.services.HibernateEntityManagerProperties;
import com.jwebmp.guicedpersistence.db.services.PersistenceGuiceConfigurator;
import com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
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
	requires org.hibernate.orm.jcache;
	requires org.hibernate.validator;

	uses com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
	uses com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;
	uses ITransactionHandler;
	uses com.jwebmp.guicedpersistence.services.IDBStartup;

	provides IPackageContentsScanner with GuiceInjectionMetaInfScanner;
	provides IFileContentsScanner with PersistenceFileHandler;
	provides IGuiceConfigurator with PersistenceGuiceConfigurator;
	provides IGuicePostStartup with DBStartupAsyncPostStartup;

	provides com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader with HibernateEntityManagerProperties;

	provides com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader with com.jwebmp.guicedpersistence.db.intercepters.JPADefaultConnectionBaseBuilder,
			                                                                        com.jwebmp.guicedpersistence.db.intercepters.HibernateDefaultConnectionBaseBuilder,
			                                                                        com.jwebmp.guicedpersistence.db.intercepters.EclipseLinkDefaultConnectionBaseBuilder;

	opens com.oracle.jaxb21 to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.db to com.fasterxml.jackson.databind;
	opens com.jwebmp.guicedpersistence.injectors to com.google.guice;
	exports com.jwebmp.guicedpersistence.db.services;
}
