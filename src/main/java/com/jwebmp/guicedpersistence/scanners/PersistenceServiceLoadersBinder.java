package com.jwebmp.guicedpersistence.scanners;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.jwebmp.guicedinjection.abstractions.GuiceInjectorModule;
import com.jwebmp.guicedinjection.interfaces.IDefaultService;
import com.jwebmp.guicedinjection.interfaces.IGuiceDefaultBinder;
import com.jwebmp.guicedpersistence.services.IAsyncStartup;
import com.jwebmp.guicedpersistence.services.ITransactionHandler;
import com.jwebmp.guicedpersistence.services.PropertiesConnectionInfoReader;
import com.jwebmp.guicedpersistence.services.PropertiesEntityManagerReader;

import java.util.ServiceLoader;
import java.util.Set;

@SuppressWarnings("Convert2Diamond")
public class PersistenceServiceLoadersBinder
		implements IGuiceDefaultBinder<PersistenceServiceLoadersBinder, GuiceInjectorModule>
{
	public static final Key<Set<PropertiesEntityManagerReader>> PropertiesEntityManagerReader = Key.get(new TypeLiteral<Set<PropertiesEntityManagerReader>>() {});
	public static final Key<Set<PropertiesConnectionInfoReader>> PropertiesConnectionInfoReader = Key.get(new TypeLiteral<Set<PropertiesConnectionInfoReader>>() {});
	public static final Key<Set<IAsyncStartup>> IAsyncStartupReader = Key.get(new TypeLiteral<Set<IAsyncStartup>>() {});
	public static final Key<Set<ITransactionHandler>> ITransactionHandlerReader = Key.get(new TypeLiteral<Set<ITransactionHandler>>() {});

	@Override
	public void onBind(GuiceInjectorModule module)
	{

		module.bind(PersistenceServiceLoadersBinder.PropertiesEntityManagerReader)
		      .toProvider(() -> IDefaultService.loaderToSet(ServiceLoader.load(PropertiesEntityManagerReader.class)))
		      .in(Singleton.class);

		module.bind(PersistenceServiceLoadersBinder.PropertiesConnectionInfoReader)
		      .toProvider(() -> IDefaultService.loaderToSet(ServiceLoader.load(PropertiesConnectionInfoReader.class)))
		      .in(Singleton.class);

		module.bind(PersistenceServiceLoadersBinder.IAsyncStartupReader)
		      .toProvider(() -> IDefaultService.loaderToSet(ServiceLoader.load(IAsyncStartup.class)))
		      .in(Singleton.class);

		module.bind(PersistenceServiceLoadersBinder.ITransactionHandlerReader)
		      .toProvider(() -> IDefaultService.loaderToSet(ServiceLoader.load(ITransactionHandler.class)))
		      .in(Singleton.class);

	}

}
