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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncExecutorService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;
import org.jowidgets.cap.service.tools.bean.BeanDtoFactoryHelper;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

public final class SyncExecutorServiceImpl<BEAN_TYPE extends IBean, PARAM_TYPE> implements ISyncExecutorService<PARAM_TYPE> {

	private final IBeanAccess<BEAN_TYPE> beanAccess;
	private final Object executor;
	private final IExecutableChecker<BEAN_TYPE> executableChecker;
	private final boolean allowDeletedBeans;
	private final boolean allowStaleBeans;

	private final IBeanDtoFactory<BEAN_TYPE> dtoFactory;

	@SuppressWarnings("unchecked")
	SyncExecutorServiceImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanAccess<? extends BEAN_TYPE> beanAccess,
		final Object executor,
		final IExecutableChecker<? extends BEAN_TYPE> executableChecker,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final boolean allowDeletedBeans,
		final boolean allowStaleBeans) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanAccess, "beanAccess");
		Assert.paramNotNull(executor, "executor");
		Assert.paramNotNull(dtoFactory, "dtoFactory");

		this.beanAccess = (IBeanAccess<BEAN_TYPE>) beanAccess;
		this.executor = executor;
		this.executableChecker = (IExecutableChecker<BEAN_TYPE>) executableChecker;
		this.allowDeletedBeans = allowDeletedBeans;
		this.allowStaleBeans = allowStaleBeans;

		this.dtoFactory = dtoFactory;
	}

	@Override
	public List<IBeanDto> execute(
		final List<? extends IBeanKey> keys,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		Assert.paramNotNull(keys, "beanInfos");

		CapServiceToolkit.checkCanceled(executionCallback);

		final List<BEAN_TYPE> beans;
		if (EmptyCheck.isEmpty(keys)) {
			beans = Collections.emptyList();
		}
		else {
			beans = beanAccess.getBeans(keys, executionCallback);
		}

		CapServiceToolkit.checkCanceled(executionCallback);

		if (!allowStaleBeans) {
			checkBeans(keys, beans, executionCallback);
		}
		else if (!allowDeletedBeans && beans.size() != keys.size()) {
			checkBeans(keys, beans, executionCallback);
		}

		if (executor instanceof IBeanListExecutor) {
			final List<IBeanDto> result = executeBeanList(beans, parameter, keys, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);
			return result;
		}
		else if (executor instanceof IBeanExecutor) {
			final List<IBeanDto> result = execute(beans, parameter, keys, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);
			return result;
		}
		else {
			throw new IllegalStateException("Data executor type '" + executor + "' is not supported");
		}
	}

	private void checkBeans(
		final List<? extends IBeanKey> keys,
		final List<BEAN_TYPE> beans,
		final IExecutionCallback executionCallback) {
		//put beans into map to access them faster at the next step
		final Map<Object, BEAN_TYPE> beanMap = new HashMap<Object, BEAN_TYPE>();
		for (final BEAN_TYPE bean : beans) {
			beanMap.put(bean.getId(), bean);
			CapServiceToolkit.checkCanceled(executionCallback);
		}

		//check if beans are deleted or stale
		for (final IBeanKey key : keys) {
			final BEAN_TYPE bean = beanMap.get(key.getId());
			if (!allowDeletedBeans && bean == null) {
				throw new DeletedBeanException(key);
			}
			else {
				if (!allowStaleBeans && key.getVersion() != bean.getVersion()) {
					throw new StaleBeanException(key);
				}
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
	}

	@SuppressWarnings("unchecked")
	private List<IBeanDto> executeBeanList(
		final List<BEAN_TYPE> beans,
		final PARAM_TYPE parameter,
		final List<? extends IBeanKey> keys,
		final IExecutionCallback executionCallback) {

		if (executableChecker != null) {
			checkExecutableStates(beans, keys, executionCallback);
		}

		final IBeanListExecutor<BEAN_TYPE, PARAM_TYPE> beanCollectionExecutor = (IBeanListExecutor<BEAN_TYPE, PARAM_TYPE>) executor;
		final List<? extends BEAN_TYPE> executionResult = beanCollectionExecutor.execute(beans, parameter, executionCallback);
		CapServiceToolkit.checkCanceled(executionCallback);
		beanAccess.flush();
		return BeanDtoFactoryHelper.createDtos(dtoFactory, executionResult);
	}

	@SuppressWarnings("unchecked")
	private List<IBeanDto> execute(
		final List<BEAN_TYPE> beans,
		final PARAM_TYPE parameter,
		final List<? extends IBeanKey> keys,
		final IExecutionCallback executionCallback) {

		final IBeanExecutor<BEAN_TYPE, PARAM_TYPE> beanExecutor = (IBeanExecutor<BEAN_TYPE, PARAM_TYPE>) executor;

		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final BEAN_TYPE bean : beans) {
			if (executableChecker != null) {
				checkExecutableState(bean, keys, executionCallback);
			}
			final BEAN_TYPE executionResult = beanExecutor.execute(bean, parameter, executionCallback);
			CapServiceToolkit.checkCanceled(executionCallback);
			beanAccess.flush();
			if (executionResult != null) {
				result.add(dtoFactory.createDto(executionResult));
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
		return result;
	}

	private void checkExecutableStates(
		final List<BEAN_TYPE> beans,
		final List<? extends IBeanKey> keys,
		final IExecutionCallback executionCallback) {
		for (final BEAN_TYPE bean : beans) {
			final IExecutableState executableState = executableChecker.getExecutableState(bean);
			if (!executableState.isExecutable()) {
				throw new ExecutableCheckException(
					findKey(keys, bean, executionCallback),
					"Executable check could not pass on service execution for bean '" + bean + "'",
					executableState.getReason());
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
	}

	private void checkExecutableState(
		final BEAN_TYPE bean,
		final List<? extends IBeanKey> keys,
		final IExecutionCallback executionCallback) {
		final IExecutableState executableState = executableChecker.getExecutableState(bean);
		if (!executableState.isExecutable()) {
			throw new ExecutableCheckException(
				findKey(keys, bean, executionCallback),
				"Executable check could not pass on service execution for bean '" + bean + "'",
				executableState.getReason());
		}
	}

	private IBeanKey findKey(final List<? extends IBeanKey> keys, final IBean bean, final IExecutionCallback executionCallback) {
		for (final IBeanKey key : keys) {
			if (key.getId().equals(bean.getId())) {
				return key;
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
		throw new IllegalStateException("Could not find key for bean '"
			+ bean
			+ "'. This seems too be a bug and may happen "
			+ "e.g, if the implemented 'bean access' creates beans with id's that are different from that of the given keys.");
	}
}
