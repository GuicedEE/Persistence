package com.guicedee.guicedpersistence.scanners;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedpersistence.services.IPropertiesConnectionInfoReader;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;

import java.util.ServiceLoader;
import java.util.Set;

public class PersistenceServiceLoadersBinder extends AbstractModule implements IGuiceModule<PersistenceServiceLoadersBinder>
{
	@SuppressWarnings("Convert2Diamond")
	public static final Key<Set<IPropertiesEntityManagerReader>> PropertiesEntityManagerReader = Key.get(new TypeLiteral<Set<IPropertiesEntityManagerReader>>()
	{});
	@SuppressWarnings("Convert2Diamond")
	public static final Key<Set<IPropertiesConnectionInfoReader>> PropertiesConnectionInfoReader = Key.get(new TypeLiteral<Set<IPropertiesConnectionInfoReader>>()
	{});

	@Override
	protected void configure()
	{
		Set<IPropertiesEntityManagerReader> propertiesEntityManager = IGuiceContext
				                                                              .getContext()
				                                                              .getLoader(IPropertiesEntityManagerReader.class, true, ServiceLoader.load(IPropertiesEntityManagerReader.class));
		Set<IPropertiesConnectionInfoReader> IPropertiesConnectionInfoReader = IGuiceContext
				                                                                       .getContext()
				                                                                       .getLoader(IPropertiesConnectionInfoReader.class, true, ServiceLoader.load(IPropertiesConnectionInfoReader.class));

		bind(PersistenceServiceLoadersBinder.PropertiesEntityManagerReader).toInstance(propertiesEntityManager);
		bind(PersistenceServiceLoadersBinder.PropertiesConnectionInfoReader).toInstance(IPropertiesConnectionInfoReader);


/*		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), new GuicedPersistenceTxnInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(com.google.inject.persist.Transactional.class), new CustomJpaLocalTxnInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(jakarta.transaction.Transactional.class), new CustomJpaLocalTxnInterceptor());*/
	}
	
}
