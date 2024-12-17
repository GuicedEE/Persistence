package com.guicedee.guicedpersistence.lambda;

import com.google.inject.Key;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

import java.util.Map;

@Log
/**
 * A transactional runnable wrapper that ensures the provided {@link Runnable} runs within a transactional context.
 * This class allows for automatic transaction management and optional scope transfers.
 *
 * @param <T> The generic type parameter (not utilized in this implementation).
 */
public class TransactionalRunnable<T> implements Runnable {

    /**
     * The {@link Runnable} instance to be executed.
     */
    private Runnable consumer;
    /**
     * The stack trace captured during instantiation to assist in debugging if errors occur.
     */
    private Exception stackTrace;
    /**
     * Contextual values to be transferred during execution.
     */
    private Map<Key<?>, Object> values;

    /**
     * Default constructor that initializes the stack trace for debugging purposes.
     */
    public TransactionalRunnable() {
        this.stackTrace = new Exception();
    }

    /**
     * Creates a new {@link TransactionalRunnable} with the specified {@link Runnable}.
     *
     * @param supplier The {@link Runnable} to be executed within a transactional context.
     * @param <T>      The generic type parameter (not utilized in this method).
     * @return An instance of {@link TransactionalRunnable}.
     */
    public static <T> TransactionalRunnable<T> of(Runnable supplier) {
        return of(supplier, false);
    }

    /**
     * Creates a new {@link TransactionalRunnable} with the specified {@link Runnable}, optionally transferring the scope.
     *
     * @param supplier      The {@link Runnable} to be executed within a transactional context.
     * @param transferScope If {@code true}, transfers the current scope's context values to the runnable.
     * @param <T>           The generic type parameter (not utilized in this method).
     * @return An instance of {@link TransactionalRunnable}.
     */
    public static <T> TransactionalRunnable<T> of(Runnable supplier, boolean transferScope) {
        var ts = IGuiceContext.get(TransactionalRunnable.class);
        ts.consumer = supplier;
        if (transferScope) {
            var cs = IGuiceContext.get(CallScoper.class);
            ts.values = cs.getValues();
        }
        return ts;
    }

    /**
     * Executes the wrapped {@link Runnable} within a transactional context.
     * Ensures transactions are managed automatically and resets state after execution.
     *
     * @throws Throwable If an exception occurs during execution, it is propagated with debugging information.
     */
    @Transactional
    public void run() {
        if (this.consumer != null) {
            try {
                if (values != null && !values.isEmpty()) {
                    IGuiceContext.get(CallScoper.class).setValues(values);
                }
                this.consumer.run();
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
    }

    /**
     * Sets the {@link Runnable} to be executed.
     *
     * @param consumer The {@link Runnable} instance to be executed.
     * @return The current {@link TransactionalRunnable} instance for method chaining.
     */
    public TransactionalRunnable<T> setConsumer(Runnable consumer) {
        this.consumer = consumer;
        return this;
    }
}
