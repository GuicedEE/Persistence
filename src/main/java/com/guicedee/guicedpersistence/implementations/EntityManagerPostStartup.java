package com.guicedee.guicedpersistence.implementations;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.guicedpersistence.services.PersistenceServicesModule;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Log4j2
public class EntityManagerPostStartup
        implements IGuicePostStartup<EntityManagerPostStartup>
{
    private static boolean startPersistenceServices = true;

    @Inject
    private Vertx vertx;

    @Override
    public List<Future<Boolean>> postLoad()
    {
        Promise<Boolean> promise = Promise.promise();
        List<Future<Boolean>> futures = new ArrayList<>();
        PersistenceServicesModule.getConnectionModules()
                .forEach((connection, module) -> {
                    futures.add(vertx.executeBlocking(() -> {
                        log.info("Starting up Entity Manager [" + connection.getPersistenceUnitName() + "]");
                        Key<PersistService> persistServiceKey = Key.get(PersistService.class, Names.named(connection.getPersistenceUnitName()));
                        PersistService persistService = IGuiceContext.get(persistServiceKey);
                        persistService.start();
                        log.info("Completed Entity Manager [" + connection.getPersistenceUnitName() + "]");
                        return true;
                    }, false));
                });
        Future.all(futures).onComplete(ar -> {
            promise.complete();
        }).onFailure(t -> {
            promise.fail(t);
        });
        return List.of(promise.future());
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
        return Integer.MIN_VALUE + 800;
    }
}
