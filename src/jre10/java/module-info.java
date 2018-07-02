module com.jwebmp.guicedpersistence {
	requires com.google.guice.extensions.persist;
	requires com.jwebmp.guicedinjection;
	requires com.jwebmp.logmaster;
	requires io.github.lukehutch.fastclasspathscanner;
	requires java.logging;
	requires com.google.guice;

	requires java.sql;

	requires java.naming;
	requires aopalliance;

	requires org.hibernate.validator;
	requires validation.api;
	requires hibernate.jpa;
	requires java.transaction;
	requires btm;
	requires java.xml.bind;

	provides com.jwebmp.guicedinjection.scanners.PackageContentsScanner with com.jwebmp.guicedpersistence.scanners.GuiceInjectionMetaInfScanner;
	provides com.jwebmp.guicedinjection.scanners.FileContentsScanner with com.jwebmp.guicedpersistence.db.PersistenceFileHandler;
}
