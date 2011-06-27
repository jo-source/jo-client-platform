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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public final class JpaReaderService<BEAN_TYPE extends IBean, PARAMETER_TYPE> implements IReaderService<PARAMETER_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final IQueryCreator<PARAMETER_TYPE> queryCreator;
	private final IBeanDtoFactory<BEAN_TYPE> dtoFactory;
	private final Executor executor = Executors.newCachedThreadPool(new DaemonThreadFactory());

	@PersistenceContext
	private EntityManager entityManager;

	public JpaReaderService(
		final Class<? extends BEAN_TYPE> beanType,
		final IQueryCreator<PARAMETER_TYPE> queryCreator,
		final List<String> propertyNames) {
		this.beanType = beanType;
		this.queryCreator = queryCreator;
		this.dtoFactory = CapServiceToolkit.dtoFactory(beanType, propertyNames);
	}

	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public java.util.List<IBeanDto> read(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final PARAMETER_TYPE parameter,
		final IExecutionCallback executionCallback) {

		final Query query = queryCreator.createReadQuery(entityManager, beanType, parentBeanKeys, filter, sorting, parameter);
		query.setFirstResult(firstRow);
		query.setMaxResults(maxRows);

		return executeCancelableCallable(new Callable<List<IBeanDto>>() {
			@Override
			public List<IBeanDto> call() throws Exception {
				@SuppressWarnings("unchecked")
				final List<BEAN_TYPE> result = query.getResultList();
				if (result != null) {
					return dtoFactory.createDtos(result);
				}
				return null;
			}
		}, executionCallback);
	}

	@Override
	public int count(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAMETER_TYPE parameter,
		final IExecutionCallback executionCallback) {

		final Query query = queryCreator.createCountQuery(entityManager, beanType, parentBeanKeys, filter, parameter);

		return executeCancelableCallable(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return ((Number) query.getSingleResult()).intValue();
			}
		}, executionCallback);
	}

	private <T> T executeCancelableCallable(final Callable<T> callable, final IExecutionCallback executionHandle) {
		final FutureTask<T> task = new FutureTask<T>(callable);
		executor.execute(task);
		try {
			for (;;) {
				try {
					return task.get(100, TimeUnit.MILLISECONDS);
				}
				catch (final TimeoutException e) {
					if (executionHandle.isCanceled()) {
						// TODO HW cancel query (there is no JPA call for this!)
						return null;
					}
				}
			}
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		catch (final ExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(cause);
		}
	}

}
