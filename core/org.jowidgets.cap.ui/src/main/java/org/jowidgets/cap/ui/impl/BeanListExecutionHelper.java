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

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;

final class BeanListExecutionHelper<BEAN_TYPE> {

	private final String shortErrorMessage;
	private final IBeanListModel<BEAN_TYPE> listModel;
	private final Collection<? extends IBeanProxy<BEAN_TYPE>> beans;
	private final IBeanExceptionConverter exceptionConverter;
	private final BeanExecutionPolicy beanExecutionPolicy;
	private final boolean transientSourceBeans;
	private final boolean fireBeansChanged;

	BeanListExecutionHelper(
		final String shortErrorMessage,
		final IBeanListModel<BEAN_TYPE> listModel,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> beans,
		final BeanExecutionPolicy beanExecutionPolicy,
		final IBeanExceptionConverter exceptionConverter,
		final boolean transientSourceBeans,
		final boolean fireBeansChanged) {

		this.shortErrorMessage = shortErrorMessage;
		this.listModel = listModel;
		this.beans = beans;
		this.beanExecutionPolicy = beanExecutionPolicy;
		this.exceptionConverter = exceptionConverter;
		this.transientSourceBeans = transientSourceBeans;
		this.fireBeansChanged = fireBeansChanged;
	}

	List<List<IBeanProxy<BEAN_TYPE>>> prepareExecutions() {
		final List<List<IBeanProxy<BEAN_TYPE>>> result = new LinkedList<List<IBeanProxy<BEAN_TYPE>>>();

		if (BeanExecutionPolicy.BATCH == beanExecutionPolicy) {
			final IExecutionTask executionTask = createExecutionTask();
			final List<IBeanProxy<BEAN_TYPE>> subList = new LinkedList<IBeanProxy<BEAN_TYPE>>();
			result.add(subList);
			for (final IBeanProxy<BEAN_TYPE> bean : beans) {
				if (bean.getExecutionTask() != null) {
					bean.getExecutionTask().cancel();
				}
				bean.setExecutionTask(executionTask);
				subList.add(bean);

			}
		}
		else {
			for (final IBeanProxy<BEAN_TYPE> bean : beans) {
				final List<IBeanProxy<BEAN_TYPE>> subList = new LinkedList<IBeanProxy<BEAN_TYPE>>();
				result.add(subList);
				if (bean.getExecutionTask() != null) {
					bean.getExecutionTask().cancel();
				}
				final IExecutionTask executionTask = createExecutionTask();
				bean.setExecutionTask(executionTask);
				subList.add(bean);
			}
		}
		if (fireBeansChanged) {
			listModel.fireBeansChanged();
		}
		return result;
	}

	IResultCallback<List<IBeanDto>> createResultCallback(final List<IBeanProxy<BEAN_TYPE>> beansToExecute) {
		return new AbstractUiResultCallback<List<IBeanDto>>() {
			@Override
			public void finishedUi(final List<IBeanDto> result) {
				afterExecution(beansToExecute, result);
			}

			@Override
			public void exceptionUi(final Throwable exception) {
				onExecption(beansToExecute, exception);
			}

		};
	}

	void afterExecution(final List<IBeanProxy<BEAN_TYPE>> executedBeans, final List<IBeanDto> result) {
		if (result != null) {
			if (transientSourceBeans) {
				updateTransientBeans(executedBeans, result);
			}
			else {
				updateBeans(executedBeans, result);
			}
		}
		//clear the execution task
		for (final IBeanProxy<BEAN_TYPE> bean : executedBeans) {
			bean.setExecutionTask(null);
		}
		if (fireBeansChanged) {
			listModel.fireBeansChanged();
		}
	}

	private void updateTransientBeans(final List<IBeanProxy<BEAN_TYPE>> executedBeans, final List<IBeanDto> result) {
		if (executedBeans.size() != result.size()) {
			listModel.removeBeans(executedBeans);
			for (final IBeanDto beanDto : result) {
				listModel.addBeanDto(beanDto);
			}
		}
		else {
			final Iterator<IBeanProxy<BEAN_TYPE>> sourceIterator = executedBeans.iterator();
			final Iterator<IBeanDto> resultIterator = result.iterator();
			while (sourceIterator.hasNext()) {
				sourceIterator.next().updateTransient(resultIterator.next());
			}
		}
	}

	private void updateBeans(final List<IBeanProxy<BEAN_TYPE>> executedBeans, final List<IBeanDto> result) {
		//put the resulting beans into a map for faster access later
		final Map<Object, IBeanDto> resultMap = new LinkedHashMap<Object, IBeanDto>();
		for (final IBeanDto beanDto : result) {
			resultMap.put(beanDto.getId(), beanDto);
		}

		//update the executed beans with the beans get as result from the service
		final List<IBeanProxy<BEAN_TYPE>> beansToDelete = new LinkedList<IBeanProxy<BEAN_TYPE>>();
		for (final IBeanProxy<BEAN_TYPE> bean : executedBeans) {
			final IBeanDto updatedBean = resultMap.remove(bean.getId());
			if (updatedBean != null) {
				bean.update(updatedBean);
			}
			else {
				beansToDelete.add(bean);
			}
		}

		//remove the beans from the model that was not returned by the service
		if (!beansToDelete.isEmpty()) {
			listModel.removeBeans(beansToDelete);
		}

		//add the beans to the model that was created by the service
		for (final IBeanDto beanDto : resultMap.values()) {
			listModel.addBeanDto(beanDto);
		}
	}

	void onExecption(final List<IBeanProxy<BEAN_TYPE>> executedBeans, final Throwable exception) {
		for (final IBeanProxy<BEAN_TYPE> bean : executedBeans) {
			final IExecutionTask executionTask = bean.getExecutionTask();
			final boolean canceled = (exception instanceof ServiceCanceledException)
				|| (executionTask != null && executionTask.isCanceled());
			if (!canceled) {
				bean.addMessage(exceptionConverter.convert(shortErrorMessage, executedBeans, bean, exception));
			}
			bean.setExecutionTask(null);
		}
		if (fireBeansChanged) {
			listModel.fireBeansChanged();
		}
	}

	IExecutionTask createExecutionTask() {
		return CapUiToolkit.executionTaskFactory().create();
	}

}
