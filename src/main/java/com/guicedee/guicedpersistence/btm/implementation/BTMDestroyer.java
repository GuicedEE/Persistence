package com.guicedee.guicedpersistence.btm.implementation;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.ResourceRegistrar;
import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class BTMDestroyer
		implements IGuicePreDestroy<BTMDestroyer>
{
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

