package com.jwebmp.guicedpersistence.db.exceptions;

/**
 * Generic exception to mark that no connection information was supplied
 */
public class InvalidConnectionInfoException
		extends RuntimeException
{
	/**
	 * Generic exception to mark that no connection information was supplied
	 */
	public InvalidConnectionInfoException()
	{
		//Nothing needed
	}

	/**
	 * Generic exception to mark that no connection information was supplied
	 *
	 * @param message
	 */
	public InvalidConnectionInfoException(String message)
	{
		super(message);
	}

	/**
	 * Generic exception to mark that no connection information was supplied
	 *
	 * @param message
	 * @param cause
	 */
	public InvalidConnectionInfoException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Generic exception to mark that no connection information was supplied
	 *
	 * @param cause
	 */
	public InvalidConnectionInfoException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Generic exception to mark that no connection information was supplied
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public InvalidConnectionInfoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
