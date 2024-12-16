package com.guicedee.guicedpersistence.lambda;

import jakarta.transaction.Transactional;

import java.util.function.Consumer;

public class TransactionalConsumer<T> implements Consumer<T>
{
	private Consumer<T> consumer;
	private Exception stackTrace;

	public TransactionalConsumer() {
		stackTrace = new Exception();
	}

	@Transactional
	public void perform(T t)
	{
		try {
			consumer.accept(t);
		}catch (Throwable throwable)
		{
			if (stackTrace != null) {
				throwable.addSuppressed(stackTrace);
			}
			throw throwable;
		}
	}
	
	public TransactionalConsumer<T> setConsumer(Consumer<T> consumer)
	{
		this.consumer = consumer;
		return (TransactionalConsumer<T>) this;
	}
	
	@Override
	public void accept(T t)
	{
		perform(t);
	}
}
