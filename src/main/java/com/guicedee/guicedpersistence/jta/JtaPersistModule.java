/*
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guicedee.guicedpersistence.jta;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.finder.DynamicFinder;
import com.google.inject.persist.finder.Finder;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedpersistence.db.exceptions.InvalidConnectionInfoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * JPA provider for guice persist.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class JtaPersistModule extends PersistModule
{
    private final String jpaUnit;
    private final JtaPersistOptions options;
    private final ConnectionBaseInfo connectionBaseInfo;

    private static boolean defaultSet = false;

    public JtaPersistModule(String jpaUnit, ConnectionBaseInfo connectionBaseInfo)
    {
        this(jpaUnit, JtaPersistOptions.builder()
                                       .build(), connectionBaseInfo);
    }

    public JtaPersistModule(String jpaUnit, JtaPersistOptions options, ConnectionBaseInfo connectionBaseInfo)
    {
        Preconditions.checkArgument(
                null != jpaUnit && jpaUnit.length() > 0, "JPA unit name must be a non-empty string.");
        this.jpaUnit = jpaUnit;
        this.options = options;
        this.connectionBaseInfo = connectionBaseInfo;
    }

    private Map<?, ?> properties;
    private MethodInterceptor transactionInterceptor;

    private <T> Key<T> getKey(Class<T> clazz)
    {
        return Key.get(clazz, Names.named(jpaUnit));
    }

    @Override
    protected void configurePersistence()
    {
        JtaPersistService ps = new JtaPersistService(options, jpaUnit, properties);

        bind(getKey(Map.class)).toInstance(properties);

        bind(getKey(JtaPersistOptions.class)).toInstance(options);
        bind(getKey(JtaPersistService.class)).toInstance(ps);
        bind(getKey(PersistService.class)).to(getKey(JtaPersistService.class));

        bind(getKey(UnitOfWork.class)).to(getKey(JtaPersistService.class));
        bind(getKey(EntityManager.class)).toProvider(getKey(JtaPersistService.class));
        JtaPersistService.EntityManagerFactoryProvider entityManagerFactoryProvider = new JtaPersistService.EntityManagerFactoryProvider(ps);
        bind(getKey(EntityManagerFactory.class)).toProvider(entityManagerFactoryProvider);

        transactionInterceptor = new JtaLocalTxnInterceptor(ps, connectionBaseInfo);
        requestInjection(transactionInterceptor);

        if(!defaultSet && connectionBaseInfo.isDefaultConnection())
        {
            defaultSet = true;
            bind(EntityManagerFactory.class).to(getKey(EntityManagerFactory.class));
            bind(EntityManager.class).to(getKey(EntityManager.class));
            bind(UnitOfWork.class).to(getKey(UnitOfWork.class));
            bind(PersistService.class).to(getKey(PersistService.class));
            bind(JtaPersistOptions.class).to(getKey(JtaPersistOptions.class));
        } else if(defaultSet && connectionBaseInfo.isDefaultConnection())
        {
            throw new InvalidConnectionInfoException("Cannot have two database connection information set as default - " + jpaUnit + " - " + connectionBaseInfo);
        }
        // Bind dynamic finders.
        for (Class<?> finder : dynamicFinders)
        {
            bindFinder(finder);
        }
    }

    @Override
    protected MethodInterceptor getTransactionInterceptor()
    {
        return transactionInterceptor;
    }

    /**
     * Configures the JPA persistence provider with a set of properties.
     *
     * @param properties A set of name value pairs that configure a JPA persistence provider as per
     *                   the specification.
     * @since 4.0 (since 3.0 with a parameter type of {@code java.util.Properties})
     */
    public JtaPersistModule properties(Map<?, ?> properties)
    {
        this.properties = properties;
        return this;
    }

    private final List<Class<?>> dynamicFinders = Lists.newArrayList();

    /**
     * Adds an interface to this module to use as a dynamic finder.
     *
     * @param iface Any interface type whose methods are all dynamic finders.
     */
    public <T> JtaPersistModule addFinder(Class<T> iface)
    {
        dynamicFinders.add(iface);
        return this;
    }

    private <T> void bindFinder(Class<T> iface)
    {
        if (!isDynamicFinderValid(iface))
        {
            return;
        }

        InvocationHandler finderInvoker =
                new InvocationHandler()
                {
                    @Inject
                    JtaFinderProxy finderProxy;

                    @Override
                    public Object invoke(final Object thisObject, final Method method, final Object[] args)
                            throws Throwable
                    {

                        // Don't intercept non-finder methods like equals and hashcode.
                        if (!method.isAnnotationPresent(Finder.class))
                        {
                            // NOTE(user): This is not ideal, we are using the invocation handler's equals
                            // and hashcode as a proxy (!) for the proxy's equals and hashcode.
                            return method.invoke(this, args);
                        }

                        return finderProxy.invoke(
                                new MethodInvocation()
                                {
                                    @Override
                                    public Method getMethod()
                                    {
                                        return method;
                                    }

                                    @Override
                                    public Object[] getArguments()
                                    {
                                        return null == args ? new Object[0] : args;
                                    }

                                    @Override
                                    public Object proceed() throws Throwable
                                    {
                                        return method.invoke(thisObject, args);
                                    }

                                    @Override
                                    public Object getThis()
                                    {
                                        throw new UnsupportedOperationException(
                                                "Bottomless proxies don't expose a this.");
                                    }

                                    @Override
                                    public AccessibleObject getStaticPart()
                                    {
                                        throw new UnsupportedOperationException();
                                    }
                                });
                    }
                };
        requestInjection(finderInvoker);

        @SuppressWarnings("unchecked") // Proxy must produce instance of type given.
        T proxy =
                (T)
                        Proxy.newProxyInstance(
                                Thread.currentThread()
                                      .getContextClassLoader(),
                                new Class<?>[]{iface},
                                finderInvoker);

        bind(iface).toInstance(proxy);
    }

    private boolean isDynamicFinderValid(Class<?> iface)
    {
        boolean valid = true;
        if (!iface.isInterface())
        {
            addError(iface + " is not an interface. Dynamic Finders must be interfaces.");
            valid = false;
        }

        for (Method method : iface.getMethods())
        {
            DynamicFinder finder = DynamicFinder.from(method);
            if (null == finder)
            {
                addError(
                        "Dynamic Finder methods must be annotated with @Finder, but "
                                + iface
                                + "."
                                + method.getName()
                                + " was not");
                valid = false;
            }
        }
        return valid;
    }
}
