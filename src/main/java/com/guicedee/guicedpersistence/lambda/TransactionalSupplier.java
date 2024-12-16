package com.guicedee.guicedpersistence.lambda;

import com.guicedee.client.IGuiceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

import java.util.function.Supplier;

@Log
public class TransactionalSupplier<T> implements Supplier<T> {
    private Supplier<T> consumer;
    private String name;
    private Exception stackTrace;

    public TransactionalSupplier() {
        this.stackTrace = new Exception();
    }

    public static <T> TransactionalSupplier<T> of(Supplier<T> supplier) {
        var ts = IGuiceContext.get(TransactionalSupplier.class);
        ts.consumer = supplier;
        return ts;
    }

    @Transactional
    public T accept() {
        if (this.consumer != null) {
            try {
                var o = (T) this.consumer.get();
                EntityManager em = IGuiceContext.get(EntityManager.class);
                em.flush();
                return o;
            } catch (Throwable e) {
                if (stackTrace != null) {
                    e.addSuppressed(stackTrace);
                }
                throw e;
            }
        }
        return null;
    }

    @Override
    public T get() {
        return accept();
    }

    public TransactionalSupplier<T> setConsumer(Supplier<T> consumer) {
        this.consumer = consumer;
        return this;
    }
}
