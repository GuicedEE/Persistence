package com.jwebmp.guicedpersistence.services;

/**
 * DB Startup Service.
 * Just a Guiced Post Startup Item to connect to database at startup instead of first use :)
 * <p>
 * Usually inject your Constructor @Inject with your @annotation assigned to your database
 */
public interface IDBStartup
{
}
