package com.guicedee.guicedpersistence.btm.implementation;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.ResourceRegistrar;
import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import com.guicedee.logger.LogFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BTMDestroyer
		implements IGuicePreDestroy<BTMDestroyer>
{
	private static final Logger log = LogFactory.getLog("BTMDestroyer");

	@Override
	public void onDestroy()
	{
		try
		{
			ResourceRegistrar.getResourcesUniqueNames()
			                 .forEach(name ->
			                          {
				                          try
				                          {
					                          ResourceRegistrar.unregister(ResourceRegistrar.get(name));
				                          }
				                          catch (Throwable T)
				                          {
					                          log.log(Level.SEVERE, "Unable to unregister resource [" + name + "] during destroy");
				                          }
			                          });
		}catch(Throwable T)
		{
			log.log(Level.SEVERE, "Unable to unregister resource", T);
		}

		if (TransactionManagerServices.isTransactionManagerRunning())
		{
			TransactionManagerServices.getTransactionManager()
			                          .shutdown();
			log.info("BTM Successfully Shutdown");
		}
	}
}

