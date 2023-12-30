package com.guicedee.guicedpersistence.services;

import com.google.inject.*;
import com.guicedee.guicedinjection.*;
import lombok.extern.java.Log;

import javax.sql.*;
import java.lang.annotation.*;
import java.sql.*;
import java.util.logging.Level;

@Log
public class DataSourceConnectionProvider implements Provider<Connection>
{
	private static final ThreadLocal<Connection> connection = new ThreadLocal<>();
	private Class<? extends Annotation> annotationClass;
	
	public DataSourceConnectionProvider(Class<? extends Annotation> annotationClass)
	{
		this.annotationClass = annotationClass;
	}
	
	@Override
	public Connection get()
	{
		if (connection.get() == null)
		{
			getConnection();
		}
		else
		{
			try
			{
				if (connection.get()
				              .isClosed())
				{
					getConnection();
				}
			}
			catch (SQLException e)
			{
				log.log(Level.SEVERE,"Cannot determine connection ",e);
			}
		}
		return connection.get();
	}
	
	private void getConnection()
	{
		DataSource ds = GuiceContext.get(Key.get(DataSource.class, annotationClass));
		try
		{
			Connection c = ds.getConnection();
			connection.set(c);
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE,"Cannot determine connection ",e);
		}
	}
}
