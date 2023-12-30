module com.guicedee.guicedpersistence.test {
	requires com.guicedee.guicedpersistence;
	
	
	requires org.junit.jupiter.api;
	requires org.slf4j;
	requires org.slf4j.simple;
	
	
	opens com.guicedee.guicedpersistence.test.services to org.junit.platform.commons;
}
