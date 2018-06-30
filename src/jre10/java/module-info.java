module com.jwebmp.guicedservlets {
	requires com.google.guice.extensions.persist;
	requires com.jwebmp.guicedinjection;

	exports com.jwebmp.guicedinjection.db;
	exports com.jwebmp.guicedinjection.annotations;
	exports com.jwebmp.guicedinjection.db.connectionbasebuilders;

	exports com.jwebmp.guicedinjection.scanners to io.github.lukehutch.fastclasspathscanner;
}
