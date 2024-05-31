package com.guicedee.guicedpersistence.implementations;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Log
public class EntityManagerPostStartup
        implements IGuicePostStartup<EntityManagerPostStartup>
{
    private static boolean startPersistenceServices = true;

    @Override
    public List<CompletableFuture<Boolean>> postLoad()
    {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        if (startPersistenceServices)
        {
            PersistenceServicesModule.getConnectionModules()
                                     .forEach((connection, module) -> {
                                         CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                                             log.log(Level.INFO, "Starting up Entity Manager [" + connection.getPersistenceUnitName() + "]");
                                             Key<PersistService> persistServiceKey = Key.get(PersistService.class, Names.named(connection.getPersistenceUnitName()));
                                             PersistService persistService = IGuiceContext.get(persistServiceKey);
                                             persistService.start();
                                             log.log(Level.INFO, "Completed Entity Manager [" + connection.getPersistenceUnitName() + "]");
                                             return true;
                                         }, getExecutorService());
                                         futures.add(future);
                                     });
        }
        return futures;
    }

    /**
     * If the persistence services should start on boot, or when you want them too
     */
    public static boolean isStartPersistenceServices()
    {
        return startPersistenceServices;
    }

    /**
     * If the persistence services should start on boot, or when you want them too
     *
     * @param startPersistenceServices default true
     */
    public static void setStartPersistenceServices(boolean startPersistenceServices)
    {
        EntityManagerPostStartup.startPersistenceServices = startPersistenceServices;
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MIN_VALUE + 1;
    }
}
