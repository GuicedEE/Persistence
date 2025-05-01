package com.guicedee.guicedpersistence.lambda;

import bitronix.tm.TransactionManagerServices;
import com.google.inject.Key;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
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
     * Holds the current thread's transaction when transferring the BTM context.
     */
    private Transaction currentTransaction;

    /**
     * Indicates if the BTM transaction context is being transferred.
     */
    private boolean transferTransaction;

    /**
     * Bitronix Transaction Manager.
     */
    private TransactionManager transactionManager;


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
     * Creates a new {@link TransactionalCallable} with the specified {@link Callable}, optionally transferring the scope.
     *
     * @param supplier      The {@link Callable} to be executed within a transactional context.
     * @param transferScope If {@code true}, transfers the current scope's context values to the callable.
     * @param <T>           The generic type parameter for the result of the callable.
     * @return An instance of {@link TransactionalCallable}.
     */
    public static <T> TransactionalCallable<T> of(Callable<T> supplier, boolean transferScope,boolean transferTransaction) {
        var tc = IGuiceContext.get(TransactionalCallable.class);
        tc.callable = supplier;
        if (transferScope) {
            var cs = IGuiceContext.get(CallScoper.class);
            tc.values = cs.getValues();
        }
        if (transferTransaction) {
            try {
                // Obtain the Bitronix Transaction Manager
                TransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
                tc.transactionManager = transactionManager;

                // Check if a transaction exists (i.e., is active)
                Transaction activeTransaction = transactionManager.getTransaction();
                if (activeTransaction != null) {
                    tc.currentTransaction = activeTransaction;
                    tc.transferTransaction = true;
                    tc.values.put("startedOnThisThread", false);

                } else {
                    tc.transferTransaction = false;
                    tc.values.put("startedOnThisThread", true);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to capture the current BTM transaction", e);
            }
        }

        return tc;
    }

    @Transactional
    T runOnTransaction() throws Exception
    {
        return this.callable.call();
    }

    /**
     * Executes the wrapped {@link Callable} within a transactional context.
     * Ensures transactions are managed automatically and resets state after execution.
     *
     * @return The result of the executed {@link Callable}.
     * @throws Exception If an exception occurs during execution, it is propagated with debugging information.
     */
    @Override
    public T call() throws Exception {
        if (this.callable != null) {
            Transaction previousTransaction = null;

            try {
                // Transfer the scope's context values if present
                if (values != null && !values.isEmpty()) {
                    IGuiceContext.get(CallScoper.class).setValues(values);
                    IGuiceContext.get(CallScoper.class).enter();
                }

                //check if must transfer the transaction
                if (transferTransaction && currentTransaction != null) {
                    // Suspend an active transaction, if any, in the current thread
                    previousTransaction = transactionManager.getTransaction();
                    if (previousTransaction != null) {
                        transactionManager.suspend();
                    }

                    // Resume the captured transaction
                    transactionManager.resume(currentTransaction);
                }

                return runOnTransaction();
            } catch (Throwable e) {
                if (stackTrace != null) {
                    e.addSuppressed(stackTrace);
                }
                throw e;
            } finally {
                if (transferTransaction && currentTransaction != null) {
                    try {
                        transactionManager.suspend();
                        if (previousTransaction != null) {
                            transactionManager.resume(previousTransaction);
                        }
                    } catch (Exception e) {
                        log.severe("Error restoring the previous transaction in call(): " + e.getMessage());
                    }
                }

                IGuiceContext.get(CallScoper.class).exit();
                // Reset the internal state
                values = null;
                callable = null;
            }
        }
        return null;
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