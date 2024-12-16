package com.guicedee.guicedpersistence.lambda;

import com.guicedee.client.IGuiceContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;

@Log
public class TransactionalRunnable<T> implements Runnable {
    private Runnable consumer;
    private String name;
    private Exception stackTrace;

    public TransactionalRunnable() {
        this.stackTrace = new Exception();
    }

    public static <T> TransactionalRunnable<T> of(Runnable supplier) {
        var ts = IGuiceContext.get(TransactionalRunnable.class);
        ts.consumer = supplier;
        return ts;
    }

    @Transactional
    public void run() {
        if (this.consumer != null) {
            try {
                this.consumer.run();
                EntityManager em = IGuiceContext.get(EntityManager.class);
                em.flush();
            } catch (Throwable e) {
                if (stackTrace != null) {
                    e.addSuppressed(stackTrace);
                }
                throw e;
            }
        }
    }

    public TransactionalRunnable<T> setConsumer(Runnable consumer) {
        this.consumer = consumer;
        return this;
    }
}
