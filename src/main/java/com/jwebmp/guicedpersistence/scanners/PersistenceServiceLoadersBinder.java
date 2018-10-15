package com.jwebmp.guicedpersistence.scanners;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedinjection.abstractions.GuiceInjectorModule;
import com.jwebmp.guicedinjection.interfaces.IGuiceDefaultBinder;
import com.jwebmp.guicedpersistence.db.annotations.Transactional;
import com.jwebmp.guicedpersistence.injectors.CustomJpaLocalTxnInterceptor;
import com.jwebmp.guicedpersistence.injectors.GuicedPersistenceTxnInterceptor;
import com.jwebmp.guicedpersistence.services.ITransactionHandler;
import com.jwebmp.guicedpersistence.services.PropertiesConnectionInfoReader;
import com.jwebmp.guicedpersistence.services.PropertiesEntityManagerReader;

import java.util.ServiceLoader;
import java.util.Set;


public class PersistenceServiceLoadersBinder
		implements IGuiceDefaultBinder<PersistenceServiceLoadersBinder, GuiceInjectorModule>
{
	@SuppressWarnings("Convert2Diamond")
	public static final Key<Set<PropertiesEntityManagerReader>> PropertiesEntityManagerReader = Key.get(new TypeLiteral<Set<PropertiesEntityManagerReader>>() {});
	@SuppressWarnings("Convert2Diamond")
	public static final Key<Set<PropertiesConnectionInfoReader>> PropertiesConnectionInfoReader = Key.get(new TypeLiteral<Set<PropertiesConnectionInfoReader>>() {});
	@SuppressWarnings("Convert2Diamond")
	public static final Key<Set<ITransactionHandler>> ITransactionHandlerReader = Key.get(new TypeLiteral<Set<ITransactionHandler>>() {});

	@Override
	public void onBind(GuiceInjectorModule module)
	{
		Set<PropertiesEntityManagerReader> propertiesEntityManager = GuiceContext.instance()
		                                                                         .getLoader(PropertiesEntityManagerReader.class, true, ServiceLoader.load(
				                                                                         PropertiesEntityManagerReader.class));
		Set<PropertiesConnectionInfoReader> propertiesConnectionInfoReader = GuiceContext.instance()
		                                                                                 .getLoader(PropertiesConnectionInfoReader.class, true, ServiceLoader.load(
				                                                                                 PropertiesConnectionInfoReader.class));
		Set<ITransactionHandler> transactionHandlerReader = GuiceContext.instance()
		                                                                .getLoader(ITransactionHandler.class, true, ServiceLoader.load(
				                                                                ITransactionHandler.class));

		module.bind(PersistenceServiceLoadersBinder.PropertiesEntityManagerReader)
		      .toInstance(propertiesEntityManager);

		module.bind(PersistenceServiceLoadersBinder.PropertiesConnectionInfoReader)
		      .toInstance(propertiesConnectionInfoReader);

		module.bind(PersistenceServiceLoadersBinder.ITransactionHandlerReader)
		      .toInstance(transactionHandlerReader);

		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), new GuicedPersistenceTxnInterceptor());
		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(com.google.inject.persist.Transactional.class), new CustomJpaLocalTxnInterceptor());
		module.bindInterceptor(Matchers.any(), Matchers.annotatedWith(javax.transaction.Transactional.class), new CustomJpaLocalTxnInterceptor());
	}
}
