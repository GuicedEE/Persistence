package com.guicedee.guicedpersistence.lambda;

import com.guicedee.client.IGuiceContext;
import jakarta.transaction.Transactional;

import java.util.function.BiConsumer;

public class TransactionalBiConsumer<T, U> implements BiConsumer<T, U> {
    private BiConsumer<T, U> consumer;
    private Exception stackTrace;

    public TransactionalBiConsumer() {
        stackTrace = new Exception();
    }

    public static <T, U> TransactionalBiConsumer<T, U> of(BiConsumer<T, U> supplier) {
        var ts = IGuiceContext.get(TransactionalBiConsumer.class);
        ts.consumer = supplier;
        return ts;
    }

    @Transactional
    public void perform(T t, U u) {
        try {
            consumer.accept(t, u);
        }catch (Throwable throwable)
        {
            if (stackTrace != null) {
                throwable.addSuppressed(stackTrace);
            }
            throw throwable;
        }
    }

    public TransactionalBiConsumer<T, U> setConsumer(BiConsumer<T, U> consumer) {
        this.consumer = consumer;
        return (TransactionalBiConsumer<T, U>) this;
    }

    @Override
    public void accept(T t, U u) {
        perform(t, u);
    }
}
