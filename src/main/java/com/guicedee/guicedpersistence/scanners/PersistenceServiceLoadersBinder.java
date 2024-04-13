package com.guicedee.guicedpersistence.scanners;

import com.google.inject.*;
import com.google.inject.matcher.*;
import com.guicedee.client.*;
import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedpersistence.db.annotations.*;
import com.guicedee.guicedpersistence.services.*;

import java.util.*;

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
