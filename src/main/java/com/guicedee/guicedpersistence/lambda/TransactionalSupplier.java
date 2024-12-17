package com.guicedee.guicedpersistence.lambda;

import com.google.inject.Key;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

import java.util.Map;
import java.util.function.Supplier;


@Log
/**
 * Represents a transactional wrapper around a {@link Supplier}.
 * This class ensures that the supplied logic is executed within a transactional
 * context, leveraging Jakarta EE's {@link Transactional} annotation and Guice
 * for dependency injection.
 * <p>
 * It supports optional scope transfer using the Guice {@link CallScoper} to manage
 * scoped values in a thread-safe manner.
 *
 * @param <T> The type of the result supplied by the {@code consumer}.
 */
public class TransactionalSupplier<T> implements Supplier<T> {
    private Supplier<T> consumer;
    private Exception stackTrace;
    private Map<Key<?>, Object> values;

    /**
     * Default constructor. Captures the creation stack trace for debugging purposes.
     */
    public TransactionalSupplier() {
        this.stackTrace = new Exception();
    }

    /**
     * Creates a {@code TransactionalSupplier} instance with the given supplier.
     * The supplier is executed within a transactional context.
     *
     * @param supplier The {@link Supplier} whose logic will be executed transactionally.
     * @param <T>      The type of the result supplied by the given {@code supplier}.
     * @return A configured {@code TransactionalSupplier} instance.
     */
    public static <T> TransactionalSupplier<T> of(Supplier<T> supplier) {
        return of(supplier, false);
    }

    /**
     * Creates a {@code TransactionalSupplier} instance with the given supplier.
     * The supplier is executed within a transactional context. Optionally transfers
     * scoped values to the transactional context.
     *
     * @param supplier      The {@link Supplier} whose logic will be executed transactionally.
     * @param transferScope If {@code true}, scoped values are transferred to the transactional context.
     * @param <T>           The type of the result supplied by the given {@code supplier}.
     * @return A configured {@code TransactionalSupplier} instance.
     */
    public static <T> TransactionalSupplier<T> of(Supplier<T> supplier, boolean transferScope) {
        var ts = IGuiceContext.get(TransactionalSupplier.class);
        ts.consumer = supplier;
        if (transferScope) {
            var cs = IGuiceContext.get(CallScoper.class);
            ts.values = cs.getValues();
        }
        return ts;
    }

    /**
     * Executes the supplier logic within a transactional context.
     * If scoped values are provided, they are applied using {@link CallScoper}.
     * <p>
     * Handles any exceptions and attaches the creation stack trace for debugging.
     *
     * @return The result of the {@code consumer}'s logic, or {@code null} if no consumer is set.
     */
    @Transactional
    public T accept() {
        if (this.consumer != null) {
            try {
                if (values != null && !values.isEmpty()) {
                    IGuiceContext.get(CallScoper.class).setValues(values);
                }
                var o = (T) this.consumer.get();
                return o;
            } catch (Throwable e) {
                if (stackTrace != null) {
                    e.addSuppressed(stackTrace);
                }
                throw e;
            } finally {
                values = null;
                consumer = null;
            }
        }
        return null;
    }

    /**
     * Retrieves the result by executing the supplier logic transactionally.
     * This is effectively a call to {@link #accept()}.
     *
     * @return The result of the {@code consumer}'s logic, or {@code null} if no consumer is set.
     */
    @Override
    public T get() {
        return accept();
    }

    /**
     * Sets the {@code consumer} supplier logic that will be executed transactionally.
     *
     * @param consumer The {@link Supplier} to be executed.
     * @return This {@code TransactionalSupplier} instance for method chaining.
     */
    public TransactionalSupplier<T> setConsumer(Supplier<T> consumer) {
        this.consumer = consumer;
        return this;
    }
}
