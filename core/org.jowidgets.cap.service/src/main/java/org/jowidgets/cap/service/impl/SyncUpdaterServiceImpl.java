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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.ISyncExecutorService;
import org.jowidgets.cap.service.api.updater.ISyncUpdaterService;

public final class SyncUpdaterServiceImpl<BEAN_TYPE extends IBean> implements ISyncUpdaterService {

	private final ISyncExecutorService<Collection<? extends IBeanModification>> executorService;

	SyncUpdaterServiceImpl(final ExecutorServiceBuilderImpl<BEAN_TYPE, Collection<? extends IBeanModification>> executorServiceBuilder) {

		final Collection<String> propertyNames = executorServiceBuilder.getPropertyNames();
		final Map<String, Method> methods = new HashMap<String, Method>();

		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(executorServiceBuilder.getBeanType());
			for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
				final String propertyName = propertyDescriptor.getName();
				if (propertyNames.contains(propertyName)) {
					methods.put(propertyName, propertyDescriptor.getWriteMethod());
				}
			}
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}

		executorServiceBuilder.setExecutor(new IBeanExecutor<BEAN_TYPE, Collection<? extends IBeanModification>>() {

			@Override
			public BEAN_TYPE execute(
				final BEAN_TYPE data,
				final Collection<? extends IBeanModification> modifications,
				final IExecutionCallback executionCallback) {

				for (final IBeanModification modification : modifications) {
					final Method method = methods.get(modification.getPropertyName());
					if (method != null) {
						try {
							method.invoke(data, modification.getNewValue());
						}
						catch (final Exception e) {
							throw new RuntimeException(e);
						}
					}
					else {
						throw new ServiceException(modification, "Tryed to set the property '"
							+ modification.getPropertyName()
							+ "', but the property is not defined / allowed for this service.");
					}
				}
				return data;
			}
		});

		this.executorService = executorServiceBuilder.buildSyncService();
	}

	@Override
	public List<IBeanDto> update(
		final Collection<? extends IBeanModification> modifications,
		final IExecutionCallback executionCallback) {

		final Map<Object, List<IBeanModification>> keyMap = createKeyMap(modifications);

		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final List<IBeanModification> modificationList : keyMap.values()) {
			final List<? extends IBeanKey> keysList = Collections.singletonList(modificationList.iterator().next());
			result.addAll(executorService.execute(keysList, modificationList, executionCallback));
		}
		return result;
	}

	private Map<Object, List<IBeanModification>> createKeyMap(final Collection<? extends IBeanModification> modifications) {
		final Map<Object, List<IBeanModification>> result = new HashMap<Object, List<IBeanModification>>();
		for (final IBeanModification beanModification : modifications) {
			List<IBeanModification> modificationList = result.get(beanModification.getId());
			if (modificationList == null) {
				modificationList = new LinkedList<IBeanModification>();
				result.put(beanModification.getId(), modificationList);
			}
			modificationList.add(beanModification);
		}
		return result;
	}

}
