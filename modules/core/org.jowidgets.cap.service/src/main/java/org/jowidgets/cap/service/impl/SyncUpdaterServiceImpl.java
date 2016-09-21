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

package org.jowidgets.cap.service.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.adapter.ISyncExecutorService;
import org.jowidgets.cap.service.api.adapter.ISyncUpdaterService;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

public final class SyncUpdaterServiceImpl<BEAN_TYPE> implements ISyncUpdaterService {

	private final ISyncExecutorService<Map<Object, Collection<IBeanModification>>> executorService;

	@SuppressWarnings("rawtypes")
	SyncUpdaterServiceImpl(
		final IBeanIdentityResolver beanIdentityResolver,
		final IBeanModifier<BEAN_TYPE> beanModifier,
		final ExecutorServiceBuilderImpl<BEAN_TYPE, Map<Object, Collection<IBeanModification>>> executorServiceBuilder) {

		Assert.paramNotNull(beanIdentityResolver, "beanIdentityResolver");
		Assert.paramNotNull(beanModifier, "beanModifier");
		Assert.paramNotNull(executorServiceBuilder, "executorServiceBuilder");

		final boolean allowStaleBeans = executorServiceBuilder.isAllowStaleBeans();

		executorServiceBuilder.setExecutor(new IBeanListExecutor<BEAN_TYPE, Map<Object, Collection<IBeanModification>>>() {

			@Override
			@SuppressWarnings("unchecked")
			public List<BEAN_TYPE> execute(
				final List<BEAN_TYPE> beans,
				final Map<Object, Collection<IBeanModification>> modificationsMap,
				final IExecutionCallback executionCallback) {
				for (final BEAN_TYPE bean : beans) {
					final Collection<IBeanModification> modifications = modificationsMap.get(beanIdentityResolver.getId(bean));
					executeBean(bean, modifications, executionCallback);
				}
				return beans;
			}

			@SuppressWarnings("unchecked")
			private BEAN_TYPE executeBean(
				final BEAN_TYPE bean,
				final Collection<? extends IBeanModification> modifications,
				final IExecutionCallback executionCallback) {
				if (!allowStaleBeans) {
					for (final IBeanModification modification : modifications) {
						if (!allowStaleBeans && beanModifier.isPropertyStale(bean, modification)) {
							throw new StaleBeanException(
								beanIdentityResolver.getId(bean),
								"The bean property '" + modification.getPropertyName() + "' is stale");
						}
					}
				}
				for (final IBeanModification modification : modifications) {
					beanModifier.modify(bean, modification);
				}
				return bean;
			}

		});

		this.executorService = executorServiceBuilder.buildSyncService();
	}

	@Override
	public List<IBeanDto> update(
		final Collection<? extends IBeanModification> modifications,
		final IExecutionCallback executionCallback) {

		final Map<Object, Collection<IBeanModification>> modificationsMap = createModificationsMap(modifications);

		final List<IBeanDto> result = new LinkedList<IBeanDto>();

		final List<IBeanKey> keysList = new LinkedList<IBeanKey>();
		for (final Collection<IBeanModification> modificationList : modificationsMap.values()) {
			if (!EmptyCheck.isEmpty(modificationList)) {
				keysList.add(modificationList.iterator().next());
			}
		}
		result.addAll(executorService.execute(keysList, modificationsMap, executionCallback));
		return result;
	}

	private Map<Object, Collection<IBeanModification>> createModificationsMap(
		final Collection<? extends IBeanModification> modifications) {
		final Map<Object, Collection<IBeanModification>> result = new LinkedHashMap<Object, Collection<IBeanModification>>();
		for (final IBeanModification beanModification : modifications) {
			Collection<IBeanModification> modificationList = result.get(beanModification.getId());
			if (modificationList == null) {
				modificationList = new LinkedList<IBeanModification>();
				result.put(beanModification.getId(), modificationList);
			}
			modificationList.add(beanModification);
		}
		return result;
	}

}
