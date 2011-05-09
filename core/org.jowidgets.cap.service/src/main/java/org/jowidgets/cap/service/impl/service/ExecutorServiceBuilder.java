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

package org.jowidgets.cap.service.impl.service;

import java.util.Collections;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.executor.IBeanCollectionExecutor;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IExecutorServiceBuilder;
import org.jowidgets.util.Assert;

public final class ExecutorServiceBuilder<BEAN_TYPE extends IBean, PARAM_TYPE> implements
		IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanAccess<? extends BEAN_TYPE> beanAccess;

	private Object executor;
	private IExecutableChecker<? extends BEAN_TYPE> executableChecker;
	private List<String> propertyNames;
	private boolean allowDeletedBeans;
	private boolean allowStaleBeans;
	private Long executionCallbackDelay;
	private boolean delayedExecutionCallback;

	public ExecutorServiceBuilder(final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		Assert.paramNotNull(beanAccess, "beanAccess");
		Assert.paramNotNull(beanAccess.getBeanType(), "beanAccess.getBeanType()");

		this.beanType = beanAccess.getBeanType();
		this.beanAccess = beanAccess;

		this.allowDeletedBeans = false;
		this.allowStaleBeans = false;
		this.delayedExecutionCallback = true;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		final IBeanExecutor<? extends BEAN_TYPE, ? extends PARAM_TYPE> beanExecutor) {
		this.executor = beanExecutor;
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		final IBeanCollectionExecutor<? extends BEAN_TYPE, ? extends PARAM_TYPE> beanCollectionExecutor) {
		this.executor = beanCollectionExecutor;
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutableChecker(
		final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		this.executableChecker = executableChecker;
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setPropertieNames(final List<String> propertyNames) {
		this.propertyNames = propertyNames;
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setAllowDeletedBeans(final boolean allowDeletedBeans) {
		this.allowDeletedBeans = allowDeletedBeans;
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setAllowStaleBeans(final boolean allowStaleBeans) {
		this.allowStaleBeans = allowStaleBeans;
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutionCallbackDelay(final long delay) {
		this.delayedExecutionCallback = true;
		this.executionCallbackDelay = Long.valueOf(delay);
		return this;
	}

	@Override
	public IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setDelayedExecutionCallback(final boolean delayedCallback) {
		this.delayedExecutionCallback = delayedCallback;
		return this;
	}

	public Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	protected List<String> getPropertyNames() {
		if (propertyNames == null) {
			return Collections.emptyList();
		}
		else {
			return propertyNames;
		}
	}

	@Override
	public IExecutorService<PARAM_TYPE> build() {
		if (executor == null) {
			executor = new IBeanExecutor<BEAN_TYPE, Object>() {
				@Override
				public BEAN_TYPE execute(final BEAN_TYPE data, final Object parameter, final IExecutionCallback executionCallback) {
					return data;
				}
			};
		}

		return new ExecutorServiceImpl<BEAN_TYPE, PARAM_TYPE>(
			beanType,
			beanAccess,
			executor,
			executableChecker,
			getPropertyNames(),
			allowDeletedBeans,
			allowStaleBeans,
			delayedExecutionCallback,
			executionCallbackDelay);
	}

}
