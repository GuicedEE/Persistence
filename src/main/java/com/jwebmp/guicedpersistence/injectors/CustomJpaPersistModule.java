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

package com.jwebmp.guicedpersistence.injectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.finder.DynamicFinder;
import com.google.inject.persist.finder.Finder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
@SuppressWarnings({"MissingClassJavaDoc", "WeakerAccess"})
public final class CustomJpaPersistModule
		extends PersistModule
{
	/**
	 * Field jpaUnit
	 */
	private final String jpaUnit;
	/**
	 * Field dynamicFinders
	 */
	private final List<Class<?>> dynamicFinders = Lists.newArrayList();
	/**
	 * Field properties
	 */
	private Map<?, ?> properties;
	/**
	 * Field transactionInterceptor
	 */
	private MethodInterceptor transactionInterceptor;

	/**
	 * Constructor CustomJpaPersistModule creates a new CustomJpaPersistModule instance.
	 *
	 * @param jpaUnit
	 * 		of type String
	 */
	public CustomJpaPersistModule(String jpaUnit)
	{
		Preconditions.checkArgument(
				null != jpaUnit && jpaUnit.length() > 0, "JPA unit name must be a non-empty string.");
		this.jpaUnit = jpaUnit;
	}

	/**
	 * Method configurePersistence ...
	 */
	@Override
	protected void configurePersistence()
	{
		bindConstant().annotatedWith(CustomJpa.class)
		              .to(jpaUnit);

		bind(CustomJpaPersistService.class).in(Singleton.class);

		bind(PersistService.class).to(CustomJpaPersistService.class);
		bind(UnitOfWork.class).to(CustomJpaPersistService.class);
		bind(EntityManager.class).toProvider(CustomJpaPersistService.class);
		bind(EntityManagerFactory.class)
				.toProvider(CustomJpaPersistService.EntityManagerFactoryProvider.class);

		transactionInterceptor = new CustomJpaLocalTxnInterceptor();
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionInterceptor);

		for (Class<?> finder : dynamicFinders)
		{
			bindFinder(finder);
		}
	}

	/**
	 * Method getTransactionInterceptor returns the transactionInterceptor of this CustomJpaPersistModule object.
	 *
	 * @return the transactionInterceptor (type MethodInterceptor) of this CustomJpaPersistModule object.
	 */
	@Override
	protected MethodInterceptor getTransactionInterceptor()
	{
		return transactionInterceptor;
	}

	/**
	 * Method bindFinder ...
	 *
	 * @param iface
	 * 		of type Class<T>
	 */
	private <T> void bindFinder(Class<T> iface)
	{
		if (!isDynamicFinderValid(iface))
		{
			return;
		}

		InvocationHandler finderInvoker =
				new InvocationHandler()
				{
					/** Field finderProxy  */
					@Inject
					CustomJpaFinderProxy finderProxy;

					/**
					 * Method invoke ...
					 *
					 * @param thisObject of type Object
					 * @param method of type Method
					 * @param args of type Object[]
					 * @return Object
					 * @throws Throwable when
					 */
					@Override
					public Object invoke(Object thisObject, Method method, Object[] args)
							throws Throwable
					{

						// Don't intercept non-finder methods like equals and hashcode.
						if (!method.isAnnotationPresent(Finder.class))
						{
							// NOTE(dhanji): This is not ideal, we are using the invocation handler's equals
							// and hashcode as a proxy (!) for the proxy's equals and hashcode.
							return method.invoke(this, args);
						}

						return finderProxy.invoke(
								new MethodInvocation()
								{
									/**
									 * Method getMethod returns the method of this ${CLASS} object.
									 *
									 *
									 *
									 * @return the method (type Method) of this ${CLASS} object.
									 */
									@Override
									public Method getMethod()
									{
										return method;
									}

									/**
									 * Method getArguments returns the arguments of this ${CLASS} object.
									 *
									 *
									 *
									 * @return the arguments (type Object[]) of this ${CLASS} object.
									 */
									@Override
									public Object[] getArguments()
									{
										return null == args ? new Object[0] : args;
									}

									/**
									 * Method proceed ...
									 * @return Object
									 * @throws Throwable when
									 */
									@Override
									public Object proceed() throws Throwable
									{
										return method.invoke(thisObject, args);
									}

									/**
									 * Method getThis returns the this of this ${CLASS} object.
									 *
									 *
									 *
									 * @return the this (type Object) of this ${CLASS} object.
									 */
									@Override
									public Object getThis()
									{
										throw new UnsupportedOperationException(
												"Bottomless proxies don't expose a this.");
									}

									/**
									 * Method getStaticPart returns the staticPart of this ${CLASS} object.
									 *
									 *
									 *
									 * @return the staticPart (type AccessibleObject) of this ${CLASS} object.
									 */
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

	/**
	 * Method isDynamicFinderValid ...
	 *
	 * @param iface
	 * 		of type Class<?>
	 *
	 * @return boolean
	 */
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

	/**
	 * Method provideProperties ...
	 *
	 * @return Map<?
                                               *               	       	       ,
                                               *               	       	       ?>
	 */
	@Provides
	@CustomJpa
	Map<?, ?> provideProperties()
	{
		return properties;
	}

	/**
	 * Configures the JPA persistence provider with a set of properties.
	 *
	 * @param properties
	 * 		A set of name value pairs that configure a JPA persistence provider as per
	 * 		the specification.
	 *
	 * @since 4.0 (since 3.0 with a parameter type of {@code java.util.Properties})
	 */
	public CustomJpaPersistModule properties(Map<?, ?> properties)
	{
		this.properties = properties;
		return this;
	}

	/**
	 * Adds an interface to this module to use as a dynamic finder.
	 *
	 * @param iface
	 * 		Any interface type whose methods are all dynamic finders.
	 */
	@SuppressWarnings("unused")
	public <T> CustomJpaPersistModule addFinder(Class<T> iface)
	{
		dynamicFinders.add(iface);
		return this;
	}
}
