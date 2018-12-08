package com.jwebmp.guicedpersistence.db;

import com.google.inject.Provider;

import javax.sql.DataSource;

class DataSourceProvider
		implements Provider<DataSource>
{
	private ConnectionBaseInfo info;

	public DataSourceProvider(ConnectionBaseInfo info)
	{
		this.info = info;
	}

	@Override
	public DataSource get()
	{
		return info.toPooledDatasource();
	}
}
