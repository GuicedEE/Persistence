package com.jwebmp.guicedpersistence.db.exceptions;

/**
 * Injection error when a collection instantiation isn't valid
 */
public class JPAInjectionException
		extends RuntimeException
{
	/**
	 * Injection error when a collection instantiation isn't valid
	 */
	public JPAInjectionException()
	{
	}

	/**
	 * Injection error when a collection instantiation isn't valid
	 *
	 * @param message
	 */
	public JPAInjectionException(String message)
	{
		super(message);
	}

	/**
	 * Injection error when a collection instantiation isn't valid
	 *
	 * @param message
	 * @param cause
	 */
	public JPAInjectionException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Injection error when a collection instantiation isn't valid
	 *
	 * @param cause
	 */
	public JPAInjectionException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Injection error when a collection instantiation isn't valid
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public JPAInjectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
