package com.guicedee.guicedpersistence.jta;

import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;

import javax.sql.DataSource;

public class JPAConnectionBaseInfo
		extends ConnectionBaseInfo
{
	/**
	 * You can fetch it directly from the entity manager using (DataSource)managerFactory.getConnectionFactory()
	 *
	 * @return Null
	 */
	@Override
	public DataSource toPooledDatasource()
	{
		return null;
	}
}
