package com.guicedee.guicedpersistence.lambda;

import com.google.inject.Key;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A specialized {@link Consumer} that wraps another consumer to perform transactional operations.
 * It ensures transactional scoping and error handling, with the ability to transfer context values
 * for execution within the current transaction.
 *
 * @param <T> The type of input accepted by the consumer.
 */
public class TransactionalConsumer<T> implements Consumer<T> {
    private Consumer<T> consumer;
    private Exception stackTrace;
    private Map<Key<?>, Object> values;

    /**
     * Default constructor that initializes the stack trace for error tracing.
     */
    public TransactionalConsumer() {
        stackTrace = new Exception();
    }

    /**
     * Factory method to create a {@code TransactionalConsumer} instance.
     *
     * @param supplier The {@link Consumer} to be wrapped for transactional execution.
     * @param <T>      The type of input accepted by the consumer.
     * @return A new {@code TransactionalConsumer} instance wrapping the supplied consumer.
     */
    public static <T> TransactionalConsumer<T> of(Consumer<T> supplier) {
        return of(supplier, false);
    }

    /**
     * Factory method to create a {@code TransactionalConsumer} instance with optional scope transfer.
     *
     * @param supplier      The {@link Consumer} to be wrapped.
     * @param transferScope A boolean indicating whether to transfer scope context values.
     * @param <T>           The type of input accepted by the consumer.
     * @return A configured {@code TransactionalConsumer} instance.
     */
    public static <T> TransactionalConsumer<T> of(Consumer<T> supplier, boolean transferScope) {
        var ts = IGuiceContext.get(TransactionalConsumer.class);
        ts.consumer = supplier;
        if (transferScope) {
            var cs = IGuiceContext.get(CallScoper.class);
            ts.values = cs.getValues();
        }
        return ts;
    }

    /**
     * Executes the provided input within a transaction, while handling scope and exceptions.
     *
     * @param t The input object to be processed by the wrapped {@link Consumer}.
     * @throws Throwable If any error occurs during consumer execution, it gets enhanced
     *                   with the previously captured stack trace.
     */
    @Transactional
    public void perform(T t) {
        try {
            if (values != null && !values.isEmpty()) {
                IGuiceContext.get(CallScoper.class).setValues(values);
            }
            consumer.accept(t);
        } catch (Throwable throwable) {
            if (stackTrace != null) {
                throwable.addSuppressed(stackTrace);
            }
            throw throwable;
        } finally {
            values = null;
            consumer = null;
        }
    }

    /**
     * Sets the underlying consumer to be executed within the transaction.
     *
     * @param consumer The {@link Consumer} to be set.
     * @return The current {@code TransactionalConsumer} instance for chaining.
     */
    public TransactionalConsumer<T> setConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
        return (TransactionalConsumer<T>) this;
    }

    /**
     * Accepts an input value, delegating the processing to the {@link #perform(Object)} method.
     * <p>
     * Implements the {@link Consumer#accept(Object)} method.
     *
     * @param t The input object to be consumed.
     */
    @Override
    public void accept(T t) {
        perform(t);
    }
}
