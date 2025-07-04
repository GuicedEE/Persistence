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

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */

class JtaPersistService implements Provider<EntityManager>, UnitOfWork, PersistService
{
  private final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();

  private final String persistenceUnitName;
  private final Map<?, ?> persistenceProperties;
  private final JtaPersistOptions options;

  public JtaPersistService(
      JtaPersistOptions options,
      String persistenceUnitName,
      Map<?, ?> persistenceProperties) {
    this.options = options;
    this.persistenceUnitName = persistenceUnitName;
    this.persistenceProperties = persistenceProperties;
  }

  @Override
  public EntityManager get() {
    if (options.getAutoBeginWorkOnEntityManagerCreation() && !isWorking()) {
      begin();
    }

    EntityManager em = entityManager.get();
    Preconditions.checkState(
        null != em,
        "Requested EntityManager outside work unit. As of Guice 6.0, Guice Persist doesn't"
            + " automatically begin the unit of work when provisioning an EntityManager. To"
            + " preserve the legacy behavior, construct the `JpaPersistModule` with a"
            + " `JpaPersistOptions.builder().setAutoBeginWorkOnEntityManagerCreation(true).build()`."
            + " Alternately, try calling UnitOfWork.begin() first, or use a PersistFilter if you"
            + " are inside a servlet environment.");

    return em;
  }

  public boolean isWorking() {
    return entityManager.get() != null;
  }

  @Override
  public void begin() {
    /*Preconditions.checkState(
        null == entityManager.get(),
        "Work already begun on this thread. Looks like you have called UnitOfWork.begin() twice"
            + " without a balancing call to end() in between.");
*/
    entityManager.set(emFactory.createEntityManager());
  }

  @Override
  public void end() {
    EntityManager em = entityManager.get();

    // Let's not penalize users for calling end() multiple times.
    if (null == em) {
      return;
    }

    try {
      em.close();
    } finally {
      entityManager.remove();
    }
  }

  private volatile EntityManagerFactory emFactory;

  @Override
  public synchronized void start() {
    if (null != emFactory) {
      return;
    }

    if (null != persistenceProperties) {
      this.emFactory =
          Persistence.createEntityManagerFactory(persistenceUnitName, persistenceProperties);
    } else {
      this.emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    }
  }

  @Override
  public synchronized void stop() {
    if (null != emFactory && emFactory.isOpen()) {
      emFactory.close();
    }
  }

  public static class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
    private final JtaPersistService emProvider;

    public EntityManagerFactoryProvider(JtaPersistService emProvider) {
      this.emProvider = emProvider;
    }

    @Override
    public EntityManagerFactory get() {
      assert null != emProvider.emFactory;
      return emProvider.emFactory;
    }
  }
}
