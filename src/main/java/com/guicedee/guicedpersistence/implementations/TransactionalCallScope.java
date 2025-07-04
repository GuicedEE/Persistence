package com.guicedee.guicedpersistence.implementations;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedservlets.servlets.services.IOnCallScopeEnter;
import com.guicedee.guicedservlets.servlets.services.IOnCallScopeExit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TransactionalCallScope implements IOnCallScopeEnter<TransactionalCallScope>, IOnCallScopeExit<TransactionalCallScope> {

    @Override
    public void onScopeEnter(Scope scope) {
        var ps = IGuiceContext.get(PersistService.class);
        ps.start();
        UnitOfWork unitOfWork = IGuiceContext.get(Key.get(UnitOfWork.class));
        unitOfWork.begin();
        EntityManager em = IGuiceContext.get(Key.get(EntityManager.class));
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        final EntityTransaction txn = em.getTransaction();
        txn.begin();
    }

    @Override
    public void onScopeExit() {
        UnitOfWork unitOfWork = IGuiceContext.get(Key.get(UnitOfWork.class));
        EntityManager em = IGuiceContext.get(Key.get(EntityManager.class));
        final EntityTransaction txn = em.getTransaction();
        if (txn.isActive()) {
            if (txn.getRollbackOnly()) {
                txn.rollback();
            } else {
                txn.commit();
            }
        }

        unitOfWork.end();
    }
}
