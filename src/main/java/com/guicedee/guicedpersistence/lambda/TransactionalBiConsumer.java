package com.guicedee.guicedpersistence.lambda;

import com.google.inject.Key;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.function.BiConsumer;


/**
 * A BiConsumer implementation that provides transactional support for consuming two arguments.
 * It uses Jakarta EE's @Transactional annotation to ensure transactional behavior.
 * The class also supports context transfer for scoped values through CallScoper.
 *
 * @param <T> the type of the first input.
 * @param <U> the type of the second input.
 */
public class TransactionalBiConsumer<T, U> implements BiConsumer<T, U> {

    /**
     * The underlying BiConsumer that performs the main operation.
     */
    private BiConsumer<T, U> consumer;

    /**
     * Stores the exception stack trace for debugging and tracking suppressed exceptions.
     */
    private Exception stackTrace;

    /**
     * Stores scoped values that may be transferred during execution.
     */
    private Map<Key<?>, Object> values;

    /**
     * Default constructor that initializes the stack trace for error tracking.
     */
    public TransactionalBiConsumer() {
        stackTrace = new Exception();
    }

    /**
     * Creates a TransactionalBiConsumer with the given BiConsumer.
     *
     * @param supplier the BiConsumer to be wrapped.
     * @param <T>      the type of the first input.
     * @param <U>      the type of the second input.
     * @return a new instance of TransactionalBiConsumer.
     */
    public static <T, U> TransactionalBiConsumer<T, U> of(BiConsumer<T, U> supplier) {
        return of(supplier, false);
    }

    /**
     * Creates a TransactionalBiConsumer with an option to transfer scoped values.
     *
     * @param supplier      the BiConsumer to be wrapped.
     * @param transferScope whether to transfer scoped values (true) or not.
     * @param <T>           the type of the first input.
     * @param <U>           the type of the second input.
     * @return a new instance of TransactionalBiConsumer.
     */
    public static <T, U> TransactionalBiConsumer<T, U> of(BiConsumer<T, U> supplier, boolean transferScope) {
        var ts = IGuiceContext.get(TransactionalBiConsumer.class);
        ts.consumer = supplier;
        if (transferScope) {
            var cs = IGuiceContext.get(CallScoper.class);
            ts.values = cs.getValues();
        }
        return ts;
    }

    /**
     * Executes the BiConsumer operation under a transactional context.
     * If the transaction fails, the original exception with the stack trace is rethrown.
     *
     * @param t the first input.
     * @param u the second input.
     */
    @Transactional
    public void perform(T t, U u) {
        try {
            if (values != null && !values.isEmpty()) {
                IGuiceContext.get(CallScoper.class).setValues(values);
            }
            consumer.accept(t, u);
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
     * Sets the underlying BiConsumer operation.
     *
     * @param consumer the BiConsumer to be set.
     * @return the updated TransactionalBiConsumer instance.
     */
    public TransactionalBiConsumer<T, U> setConsumer(BiConsumer<T, U> consumer) {
        this.consumer = consumer;
        return (TransactionalBiConsumer<T, U>) this;
    }

    /**
     * Consumes the given inputs and performs the operation under a transactional context.
     *
     * @param t the first input.
     * @param u the second input.
     */
    @Override
    public void accept(T t, U u) {
        perform(t, u);
    }
}
