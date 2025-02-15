package com.guicedee.guicedpersistence.lambda;

import com.google.inject.Key;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A transactional callable wrapper that ensures the provided {@link Callable} runs within a transactional context.
 * This class allows for automatic transaction management and optional scope transfers while enabling the callable
 * to return results.
 *
 * @param <T> The generic type parameter for the result of the {@link Callable}.
 */
@Log
public class TransactionalCallable<T> implements Callable<T> {

    /**
     * The {@link Callable} instance to be executed.
     */
    private Callable<T> callable;

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
    public TransactionalCallable() {
        this.stackTrace = new Exception();
    }

    /**
     * Creates a new {@link TransactionalCallable} with the specified {@link Callable}.
     *
     * @param supplier The {@link Callable} to be executed within a transactional context.
     * @param <T>      The generic type parameter for the result of the callable.
     * @return An instance of {@link TransactionalCallable}.
     */
    public static <T> TransactionalCallable<T> of(Callable<T> supplier) {
        return of(supplier, false);
    }

    /**
     * Creates a new {@link TransactionalCallable} with the specified {@link Callable}, optionally transferring the scope.
     *
     * @param supplier      The {@link Callable} to be executed within a transactional context.
     * @param transferScope If {@code true}, transfers the current scope's context values to the callable.
     * @param <T>           The generic type parameter for the result of the callable.
     * @return An instance of {@link TransactionalCallable}.
     */
    public static <T> TransactionalCallable<T> of(Callable<T> supplier, boolean transferScope) {
        var tc = IGuiceContext.get(TransactionalCallable.class);
        tc.callable = supplier;
        if (transferScope) {
            var cs = IGuiceContext.get(CallScoper.class);
            tc.values = cs.getValues();
        }
        return tc;
    }

    /**
     * Executes the wrapped {@link Callable} within a transactional context.
     * Ensures transactions are managed automatically and resets state after execution.
     *
     * @return The result of the executed {@link Callable}.
     * @throws Exception If an exception occurs during execution, it is propagated with debugging information.
     */
    @Transactional
    @Override
    public T call() throws Exception {
        if (this.callable != null) {
            try {
                // Transfer the scope's context values if present
                if (values != null && !values.isEmpty()) {
                    IGuiceContext.get(CallScoper.class).setValues(values);
                }
                // Execute the callable
                return this.callable.call();
            } catch (Throwable e) {
                if (stackTrace != null) {
                    e.addSuppressed(stackTrace);
                }
                throw e;
            } finally {
                // Reset the internal state
                values = null;
                callable = null;
            }
        }
        return null; // Or throw an exception if callable is null
    }

    /**
     * Sets the {@link Callable} to be executed.
     *
     * @param callable The {@link Callable} instance to be executed.
     * @return The current {@link TransactionalCallable} instance for method chaining.
     */
    public TransactionalCallable<T> setCallable(Callable<T> callable) {
        this.callable = callable;
        return this;
    }
}