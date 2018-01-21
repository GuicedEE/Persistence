package za.co.mmagon.guiceinjection.db;

import za.co.mmagon.guiceinjection.db.ConnectionBaseInfo;

import java.io.Serializable;

@FunctionalInterface
public interface CustomPoolDataSource extends Serializable
{

	void configure(ConnectionBaseInfo cbi);

}
