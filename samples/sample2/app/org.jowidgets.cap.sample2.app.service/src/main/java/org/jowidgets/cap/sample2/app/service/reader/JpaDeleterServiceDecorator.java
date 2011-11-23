/*
 * Copyright (c) 2011, grossmann
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

package org.jowidgets.cap.sample2.app.service.reader;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;
import org.jowidgets.cap.service.impl.jpa.JpaBeanAccess;
import org.jowidgets.cap.service.impl.jpa.JpaDeleterService;

public final class JpaDeleterServiceDecorator implements IDeleterService {

	private final JpaDeleterService original;
	private final JpaBeanAccess<?> beanAccess;

	public JpaDeleterServiceDecorator(final JpaDeleterService original, final JpaBeanAccess<?> beanAccess) {
		this.original = original;
		this.beanAccess = beanAccess;
	}

	@Override
	public void delete(
		final IResultCallback<Void> resultCallback,
		final Collection<? extends IBeanKey> beanKeys,
		final IExecutionCallback executionCallback) {
		final EntityManager entityManager = EntityManagerProvider.entityManager();
		beanAccess.setEntityManager(entityManager);
		original.setEntityManager(entityManager);
		final EntityTransaction tx = entityManager.getTransaction();

		final IResultCallback<Void> decoratedResultCallback = new IResultCallback<Void>() {

			@Override
			public void finished(final Void result) {
				try {
					tx.commit();
				}
				catch (final Exception e) {
					//CHECKSTYLE:OFF
					e.printStackTrace();
					//CHECKSTYLE:ON
					exception(e);
					return;
				}
				resultCallback.finished(result);
			}

			@Override
			public void exception(final Throwable exception) {
				//CHECKSTYLE:OFF
				exception.printStackTrace();
				//CHECKSTYLE:ON
				resultCallback.exception(exception);
			}

			@Override
			public void timeout() {
				resultCallback.timeout();
			}
		};

		try {
			tx.begin();
			original.delete(decoratedResultCallback, beanKeys, executionCallback);
		}
		catch (final Exception e) {
			//CHECKSTYLE:OFF
			e.printStackTrace();
			//CHECKSTYLE:ON
			resultCallback.exception(e);
		}

		finally {
			try {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
			}
			catch (final Exception e) {
			}
			beanAccess.setEntityManager(null);
			original.setEntityManager(null);
			entityManager.close();
		}

	}

}
