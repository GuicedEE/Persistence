package com.jwebmp.guicedpersistence.scanners;

import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.jwebmp.guicedinjection.abstractions.GuiceInjectorModule;
import com.jwebmp.guicedinjection.interfaces.IGuiceDefaultBinder;
import com.jwebmp.guicedpersistence.db.PropertiesConnectionInfoReader;
import com.jwebmp.guicedpersistence.db.PropertiesEntityManagerReader;
import com.jwebmp.guicedpersistence.services.IAsyncStartup;

import java.util.ServiceLoader;
import java.util.Set;

@SuppressWarnings("Convert2Diamond")
public class PersistenceServiceLoadersBinder
		implements IGuiceDefaultBinder<GuiceInjectorModule>
{
	public static final Key<Set<PropertiesEntityManagerReader>> PropertiesEntityManagerReader = Key.get(new TypeLiteral<Set<PropertiesEntityManagerReader>>() {});
	public static final Key<Set<PropertiesConnectionInfoReader>> PropertiesConnectionInfoReader = Key.get(new TypeLiteral<Set<PropertiesConnectionInfoReader>>() {});
	public static final Key<Set<IAsyncStartup>> IAsyncStartupReader = Key.get(new TypeLiteral<Set<IAsyncStartup>>() {});

	@Override
	public void onBind(GuiceInjectorModule module)
	{
		module.bind(PersistenceServiceLoadersBinder.PropertiesEntityManagerReader)
		      .toProvider(() -> loaderToSet(ServiceLoader.load(PropertiesEntityManagerReader.class)))
		      .in(Singleton.class);
		module.bind(PersistenceServiceLoadersBinder.PropertiesConnectionInfoReader)
		      .toProvider(() -> loaderToSet(ServiceLoader.load(PropertiesConnectionInfoReader.class)))
		      .in(Singleton.class);
		module.bind(PersistenceServiceLoadersBinder.IAsyncStartupReader)
		      .toProvider(() ->
		                  {
			                  return loaderToSet(ServiceLoader.load(IAsyncStartup.class));
		                  })
		      .in(Singleton.class);
	}

}
