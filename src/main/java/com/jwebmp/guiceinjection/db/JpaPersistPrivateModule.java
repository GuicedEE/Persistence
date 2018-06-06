package com.jwebmp.guiceinjection.db;

import com.google.inject.PrivateModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.google.inject.persist.jpa.JpaPersistModule;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * Provides jar scoped persistence private modules, and exposes them to the ear level
 *
 * @author GedMarc
 */
public class JpaPersistPrivateModule
		extends PrivateModule
{

	protected final String persistenceUnitName;
	protected final Properties props;
	protected final Class<? extends Annotation> qualifier;

	public JpaPersistPrivateModule(String persistenceUnitName, Class<? extends Annotation> qualifier)
	{
		this(persistenceUnitName, new Properties(), qualifier);
	}

	public JpaPersistPrivateModule(String persistenceUnitName, Properties props, Class<? extends Annotation> qualifier)
	{
		this.persistenceUnitName = persistenceUnitName;
		this.props = props;
		this.qualifier = qualifier;
	}

	/**
	 * Configures the JPA
	 */
	@Override
	protected void configure()
	{
		install(new JpaPersistModule(persistenceUnitName).properties(props));
		rebind(qualifier, EntityManagerFactory.class, EntityManager.class, PersistService.class, UnitOfWork.class);
		doConfigure();
	}

	/**
	 * Rebinds and exposes the default connection handlers with the qualifier
	 *
	 * @param qualifier
	 * @param classes
	 */
	public void rebind(Class<? extends Annotation> qualifier, Class<?>... classes)
	{
		for (Class<?> clazz : classes)
		{
			rebind(qualifier, clazz);
		}
	}

	/**
	 * bind your interfaces and classes as well as concrete ones that use JPA
	 * classes explicitly
	 */
	protected void doConfigure()
	{
		//Nothing needed
	}

	/**
	 * Rebinds and exposes the default connection handlers with the qualifier
	 *
	 * @param qualifier
	 * @param clazz
	 * @param <T>
	 */
	public <T> void rebind(Class<? extends Annotation> qualifier, Class<T> clazz)
	{
		bind(clazz).annotatedWith(qualifier)
		           .toProvider(binder().getProvider(clazz));
		expose(clazz).annotatedWith(qualifier);
	}

	/**
	 * binds and exposes a concrete class with an annotation
	 *
	 * @param <T>
	 * @param clazz
	 */
	protected <T> void bindConcreteClassWithQualifier(Class<T> clazz)
	{
		bind(clazz).annotatedWith(qualifier)
		           .to(clazz);
		expose(clazz).annotatedWith(qualifier);
	}

	/**
	 * binds and exposes a concrete class without any annotation
	 *
	 * @param clazz
	 */
	protected void bindConcreteClass(Class<?> clazz)
	{
		bind(clazz);
		expose(clazz);
	}
}
