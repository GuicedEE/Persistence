/*
 * Copyright (C) 2010 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guicedee.guicedpersistence.jta;

import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.UnitOfWork;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedpersistence.db.ConnectionBaseInfo;
import com.guicedee.guicedservlets.websockets.options.CallScopeProperties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class JtaLocalTxnInterceptor implements MethodInterceptor {

  private JtaPersistService emProvider;
  private final ConnectionBaseInfo connectionBaseInfo;
  private UnitOfWork unitOfWork;

  @Transactional
  private static class Internal {}

  // Tracks if the unit of work was begun implicitly by this transaction.
  private final ThreadLocal<Boolean> didWeStartWork = new ThreadLocal<>();

  public JtaLocalTxnInterceptor(JtaPersistService emProvider, ConnectionBaseInfo connectionBaseInfo)
  {
    this.emProvider = emProvider;
    this.connectionBaseInfo = connectionBaseInfo;
  }

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {

    String name = connectionBaseInfo.getPersistenceUnitName();
    if (methodInvocation.getMethod()
                        .isAnnotationPresent(Named.class))
    {
      name = methodInvocation.getMethod().getAnnotation(Named.class).value();
    }
    if (methodInvocation.getMethod()
                        .isAnnotationPresent(jakarta.inject.Named.class))
    {
      name = methodInvocation.getMethod().getAnnotation(jakarta.inject.Named.class).value();
    }
    if (unitOfWork == null)
    {
      unitOfWork = IGuiceContext.get(Key.get(UnitOfWork.class, Names.named(name)));
    }

    CallScoper callScoper = IGuiceContext.get(CallScoper.class);
    if(callScoper.isStartedScope())
    {
      CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
      if (csp.getProperties().containsKey("startedOnThisThread"))
      {
        boolean startedOnThisThread = (boolean) csp.getProperties().get("startedOnThisThread");
        didWeStartWork.set(startedOnThisThread);
        if (startedOnThisThread && !emProvider.isWorking())
        {
          emProvider.begin();
          //this is when we transfer transactions across threads
        }
      }
    }

    {
      // Should we start a unit of work?
      if (!emProvider.isWorking())
      {
        emProvider.begin();
        didWeStartWork.set(true);
      }
    }
    Transactional transactional = readTransactionMetadata(methodInvocation);
    EntityManager em = IGuiceContext.get(Key.get(EntityManager.class, Names.named(name)));

    // Allow 'joining' of transactions if there is an enclosing @Transactional method.
    if (em.getTransaction().isActive()) {
      return methodInvocation.proceed();
    }

    final EntityTransaction txn = em.getTransaction();
    txn.begin();

    Object result;
    try {
      result = methodInvocation.proceed();

    } catch (Exception e) {
      // commit transaction only if rollback didnt occur
      if (rollbackIfNecessary(transactional, e, txn)) {
        txn.commit();
      }
      // propagate whatever exception is thrown anyway
      throw e;
    } finally {
      // Close the em if necessary (guarded so this code doesn't run unless catch fired).
      if (null != didWeStartWork.get() && !txn.isActive()) {
        didWeStartWork.remove();
        unitOfWork.end();
      }
    }

    // everything was normal so commit the txn (do not move into try block above as it
    //  interferes with the advised method's throwing semantics)
    try {
      if (txn.isActive()) {
        if (txn.getRollbackOnly()) {
          txn.rollback();
        } else {
          txn.commit();
        }
      }
    } finally {
      // close the em if necessary
      if (null != didWeStartWork.get()) {
        didWeStartWork.remove();
        unitOfWork.end();
      }
    }

    // or return result
    return result;
  }

  private Transactional readTransactionMetadata(MethodInvocation methodInvocation) {
    Transactional transactional = null;
    Method method = methodInvocation.getMethod();
    Class<?> targetClass = methodInvocation.getThis().getClass();
    if(methodInvocation.getMethod().isAnnotationPresent(jakarta.transaction.Transactional.class))
    {
      transactional = transform(methodInvocation.getMethod()
                                                .getAnnotation(jakarta.transaction.Transactional.class));
    }
    if (null == transactional)
      transactional = method.getAnnotation(Transactional.class);
    if (null == transactional) {
      // If none on method, try the class.
      transactional = targetClass.getAnnotation(Transactional.class);
    }
    if (null == transactional) {
      // If there is no transactional annotation present, use the default
      transactional = Internal.class.getAnnotation(Transactional.class);
    }

    return transactional;
  }

  private Transactional transform(jakarta.transaction.Transactional transactional)
  {
    return new Transactional()
    {
      @Override
      public Class<? extends Annotation> annotationType()
      {
        return Transactional.class;
      }

      @Override
      public Class<? extends Exception>[] rollbackOn()
      {
        return transactional.rollbackOn();
      }

      @Override
      public Class<? extends Exception>[] ignore()
      {
        return transactional.dontRollbackOn();
      }
    };
  }

  /**
   * Returns True if rollback DID NOT HAPPEN (i.e. if commit should continue).
   *
   * @param transactional The metadata annotation of the method
   * @param e The exception to test for rollback
   * @param txn A JPA Transaction to issue rollbacks on
   */
  private boolean rollbackIfNecessary(
      Transactional transactional, Exception e, EntityTransaction txn) {
    boolean commit = true;

    // check rollback clauses
    for (Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {

      // if one matched, try to perform a rollback
      if (rollBackOn.isInstance(e)) {
        commit = false;

        // check ignore clauses (supercedes rollback clause)
        for (Class<? extends Exception> exceptOn : transactional.ignore()) {
          // An exception to the rollback clause was found, DON'T rollback
          // (i.e. commit and throw anyway)
          if (exceptOn.isInstance(e)) {
            commit = true;
            break;
          }
        }

        // rollback only if nothing matched the ignore check
        if (!commit) {
          txn.rollback();
        }
        // otherwise continue to commit

        break;
      }
    }

    return commit;
  }
}
