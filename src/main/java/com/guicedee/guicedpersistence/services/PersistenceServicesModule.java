package com.guicedee.guicedpersistence.services;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedpersistence.jta.JtaPersistModule;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

@Log
public class PersistenceServicesModule extends AbstractModule implements IGuiceModule<PersistenceServicesModule>
{
	@Getter
    private static final Map<ConnectionBaseInfo, JtaPersistModule> connectionModules = new HashMap<>();

    @Override
    protected void configure()
    {
        log.config("Registering JTA Database Modules");
        AtomicBoolean defaultSelected =new AtomicBoolean(false);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<ConnectionBaseInfo, JtaPersistModule> entry : connectionModules.entrySet())
        {
            ConnectionBaseInfo connectionBaseInfo = entry.getKey();
            Module module = entry.getValue();
            futures.add(CompletableFuture.runAsync(()->{
                DataSource ds;
                try
                {
                    log.config("Starting datasource - " + connectionBaseInfo.getJndiName());
                    ds = connectionBaseInfo.toPooledDatasource();
                    if (ds != null)
                    {
                        bind(Key.get(DataSource.class, Names.named(connectionBaseInfo.getJndiName()))).toInstance(ds);
                        if (!defaultSelected.get() && connectionBaseInfo.isDefaultConnection())
                        {
                            defaultSelected.set(true);
                            bind(Key.get(DataSource.class)).toInstance(ds);
                        }else if(defaultSelected.get() && connectionBaseInfo.isDefaultConnection())
                        {
                            throw new RuntimeException("Cannot have two default connections specified");
                        }
                        log.config("Bound DataSource.class @Named(\"" + connectionBaseInfo.getJndiName() + "\")");
                    }
                }
                catch (Exception t)
                {
                    log.log(Level.SEVERE, "Cannot start datasource!", t);
                }
            }));
            install(module);
        }
        try
        {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE,"Cannot complete futures for datasource start",e);
        }
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MAX_VALUE - 5;
    }
}
