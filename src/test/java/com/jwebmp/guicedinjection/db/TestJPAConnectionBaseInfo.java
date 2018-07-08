package com.jwebmp.guicedinjection.db;

import com.jwebmp.guicedpersistence.db.ConnectionBaseInfo;
import com.jwebmp.logger.LogFactory;

import javax.sql.DataSource;
import java.util.logging.Logger;

public class TestJPAConnectionBaseInfo
		extends ConnectionBaseInfo
{
	private static final Logger log = LogFactory.getLog("JPAConnectionBaseInfo");
	private boolean driverRegistered;

	/**
	 * You can fetch it directly from the entity manager using (DataSource)managerFactory.getConnectionFactory()
	 *
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public DataSource toPooledDatasource()
	{
		return null;
	}
}
