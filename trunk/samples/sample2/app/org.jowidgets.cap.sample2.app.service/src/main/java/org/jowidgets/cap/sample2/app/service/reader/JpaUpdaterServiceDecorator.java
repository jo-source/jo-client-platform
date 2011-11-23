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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.impl.jpa.JpaBeanAccess;

public final class JpaUpdaterServiceDecorator implements IUpdaterService {

	private final ISyncUpdaterService original;
	private final JpaBeanAccess<?> beanAccess;

	public JpaUpdaterServiceDecorator(final ISyncUpdaterService original, final JpaBeanAccess<?> beanAccess) {
		this.original = original;
		this.beanAccess = beanAccess;
	}

	@Override
	public void update(
		final IResultCallback<List<IBeanDto>> resultCallback,
		final Collection<? extends IBeanModification> modifications,
		final IExecutionCallback executionCallback) {

		final EntityManager entityManager = EntityManagerProvider.entityManager();
		beanAccess.setEntityManager(entityManager);
		final EntityTransaction tx = entityManager.getTransaction();

		try {
			tx.begin();
			final List<IBeanDto> result = original.update(modifications, executionCallback);
			tx.commit();
			resultCallback.finished(result);
		}
		catch (final Exception e) {
			resultCallback.exception(e);
			//CHECKSTYLE:OFF
			e.printStackTrace();
			//CHECKSTYLE:ON
			return;
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
			entityManager.close();
		}

	}

}
