package com.jwebmp.guicedpersistence.db;

import java.io.Serializable;

@FunctionalInterface
public interface CustomPoolDataSource
		extends Serializable
{

	void configure(ConnectionBaseInfo cbi);

}
