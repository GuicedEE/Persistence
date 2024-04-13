package com.guicedee.guicedpersistence.implementations;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import com.google.inject.persist.PersistService;
import com.guicedee.client.*;
import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import com.guicedee.guicedpersistence.btm.BTMTransactionIsolation;
import com.guicedee.guicedpersistence.db.DatabaseModule;
import lombok.extern.java.Log;

import java.lang.annotation.Annotation;
import java.util.logging.Level;

@Log
public class GuicedPersistenceDestroyer
		implements IGuicePreDestroy<GuicedPersistenceDestroyer>
{
	@Override
	public void onDestroy()
	{
		TransactionManagerServices.getTransactionManager()
								  .shutdown();
	}

	@Override
	public Integer sortOrder()
	{
		return 500;
	}
}
