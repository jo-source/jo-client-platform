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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;

public final class JpaCreatorService implements ICreatorService {

	private final IBeanInitializer<IBean> beanInitializer;
	private final IExecutorService<Object> executorService;

	@PersistenceContext
	private EntityManager entityManager;

	public JpaCreatorService(final IBeanAccess<? extends IBean> beanProvider, final List<String> propertyNames) {
		beanInitializer = CapServiceToolkit.beanInitializer(beanProvider.getBeanType(), propertyNames);
		executorService = CapServiceToolkit.executorServiceBuilder(beanProvider).setBeanDtoFactory(propertyNames).setExecutor(
				new IBeanListExecutor<IBean, Collection<? extends IBeanData>>() {
					@Override
					public List<IBean> execute(
						final List<? extends IBean> data,
						final Collection<? extends IBeanData> beansData,
						final IExecutionCallback executionCallback) {
						final List<IBean> result = new LinkedList<IBean>();
						for (final IBeanData beanData : beansData) {
							final IBean bean;
							try {
								bean = beanProvider.getBeanType().newInstance();
							}
							catch (final InstantiationException e) {
								throw new RuntimeException(e);
							}
							catch (final IllegalAccessException e) {
								throw new RuntimeException(e);
							}
							beanInitializer.initialize(bean, beanData);
							entityManager.persist(bean);
							result.add(bean);
						}
						return result;
					}
				}).build();
	}

	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void create(
		final IResultCallback<List<IBeanDto>> result,
		final Collection<? extends IBeanData> beansData,
		final IExecutionCallback executionCallback) {
		executorService.execute(result, new ArrayList<IBeanKey>(), beansData, executionCallback);
	}

}
