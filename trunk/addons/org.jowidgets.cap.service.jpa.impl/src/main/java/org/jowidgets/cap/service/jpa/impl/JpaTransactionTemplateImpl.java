/*
 * Copyright (c) 2012, grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL jo-widgets.org BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.jowidgets.cap.service.jpa.impl;

import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.jowidgets.cap.service.api.transaction.ITransactionTemplate;
import org.jowidgets.cap.service.jpa.api.EntityManagerHolder;
import org.jowidgets.util.Assert;

final class JpaTransactionTemplateImpl implements ITransactionTemplate {

	private final EntityManagerFactory entityManagerFactory;

	JpaTransactionTemplateImpl(final EntityManagerFactory entityManagerFactory) {
		Assert.paramNotNull(entityManagerFactory, "entityManagerFactory");
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public <RESULT_TYPE> RESULT_TYPE callInTransaction(final Callable<RESULT_TYPE> callable) {
		Assert.paramNotNull(callable, "callable");

		boolean entityManagerCreated = false;
		boolean transactionStarted = false;

		EntityManager entityManager = null;
		EntityTransaction tx = null;

		try {
			entityManager = EntityManagerHolder.get();
			if (entityManager == null) {
				entityManager = entityManagerFactory.createEntityManager();
				entityManagerCreated = true;
				EntityManagerHolder.set(entityManager);
			}
			tx = entityManager.getTransaction();
			if (tx != null && !tx.isActive()) {
				tx.begin();
				transactionStarted = true;
			}
			final RESULT_TYPE result = callable.call();
			if (transactionStarted) {
				tx.commit();
			}
			return result;
		}
		catch (final Throwable throwable) {
			if (tx != null && tx.isActive() && transactionStarted) {
				tx.rollback();
			}
			if (throwable instanceof Error) {
				throw (Error) throwable;
			}
			else if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			else {
				throw new RuntimeException(throwable);
			}
		}
		finally {
			if (entityManager != null && entityManager.isOpen() && entityManagerCreated) {
				EntityManagerHolder.set(null);
				entityManager.close();
			}
		}
	}

	@Override
	public void doInTransaction(final Runnable runnable) {
		Assert.paramNotNull(runnable, "runnable");
		callInTransaction(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				runnable.run();
				return null;
			}
		});

	}

}
