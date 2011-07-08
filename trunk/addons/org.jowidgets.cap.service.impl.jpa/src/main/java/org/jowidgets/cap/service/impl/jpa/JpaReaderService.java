/*
 * Copyright (c) 2011, H.Westphal
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
package org.jowidgets.cap.service.impl.jpa;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;

public final class JpaReaderService<PARAMETER_TYPE> implements IReaderService<PARAMETER_TYPE> {

	private final IQueryCreator<PARAMETER_TYPE> queryCreator;
	private final IBeanDtoFactory<IBean> dtoFactory;

	@PersistenceContext
	private EntityManager entityManager;

	public JpaReaderService(final IQueryCreator<PARAMETER_TYPE> queryCreator, final List<String> propertyNames) {
		this.queryCreator = queryCreator;
		this.dtoFactory = CapServiceToolkit.dtoFactory(queryCreator.getPersistenceClass(), propertyNames);
	}

	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void read(
		final IResultCallback<List<IBeanDto>> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final PARAMETER_TYPE parameter,
		final IExecutionCallback executionCallback) {

		execute(new Callable<List<IBeanDto>>() {
			@Override
			public List<IBeanDto> call() {
				final Query query = queryCreator.createReadQuery(entityManager, parentBeanKeys, filter, sorting, parameter);
				query.setFirstResult(firstRow);
				query.setMaxResults(maxRows);

				@SuppressWarnings("unchecked")
				final List<IBean> result = query.getResultList();
				if (result != null) {
					return dtoFactory.createDtos(result);
				}
				return null;
			}
		}, result, executionCallback);
	}

	@Override
	public void count(
		final IResultCallback<Integer> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAMETER_TYPE parameter,
		final IExecutionCallback executionCallback) {

		execute(new Callable<Integer>() {
			@Override
			public Integer call() {
				final Query query = queryCreator.createCountQuery(entityManager, parentBeanKeys, filter, parameter);
				return ((Number) query.getSingleResult()).intValue();
			}
		}, result, executionCallback);
	}

	private <T> void execute(
		final Callable<T> callable,
		final IResultCallback<T> resultCallback,
		final IExecutionCallback executionCallback) {
		try {
			final T result = callable.call();
			CapServiceToolkit.checkCanceled(executionCallback);
			resultCallback.finished(result);
		}
		catch (final Throwable t) {
			resultCallback.exception(t);
		}
	}

}
