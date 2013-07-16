/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.tools.deleter;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.ExecutableCheckerComposite;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableCheckerCompositeBuilder;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceInterceptor;
import org.jowidgets.util.Assert;

public abstract class AbstractDeleterServiceBuilder<BEAN_TYPE extends IBean> implements IDeleterServiceBuilder<BEAN_TYPE> {

	private final List<IExecutableChecker<? extends BEAN_TYPE>> executableCheckers;
	private final List<IDeleterServiceInterceptor<BEAN_TYPE>> interceptors;

	private boolean allowDeletedBeans;
	private boolean allowStaleBeans;

	public AbstractDeleterServiceBuilder() {
		this.executableCheckers = new LinkedList<IExecutableChecker<? extends BEAN_TYPE>>();
		this.interceptors = new LinkedList<IDeleterServiceInterceptor<BEAN_TYPE>>();
		this.allowDeletedBeans = true;
		this.allowStaleBeans = true;
	}

	@Override
	public final IDeleterServiceBuilder<BEAN_TYPE> addExecutableChecker(
		final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		Assert.paramNotNull(executableChecker, "executableChecker");
		this.executableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public IDeleterServiceBuilder<BEAN_TYPE> setExecutableChecker(final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		this.executableCheckers.clear();
		if (executableChecker != null) {
			addExecutableChecker(executableChecker);
		}
		return this;
	}

	@Override
	public IDeleterServiceBuilder<BEAN_TYPE> setAllowDeletedBeans(final boolean allowDeletedBeans) {
		this.allowDeletedBeans = allowDeletedBeans;
		return this;
	}

	@Override
	public IDeleterServiceBuilder<BEAN_TYPE> setAllowStaleBeans(final boolean allowStaleBeans) {
		this.allowStaleBeans = allowStaleBeans;
		return this;
	}

	@Override
	public IDeleterServiceBuilder<BEAN_TYPE> addDeleterInterceptor(final IDeleterServiceInterceptor<BEAN_TYPE> interceptor) {
		Assert.paramNotNull(interceptor, "interceptor");
		interceptors.add(interceptor);
		return this;
	}

	@SuppressWarnings("unchecked")
	protected IExecutableChecker<BEAN_TYPE> getExecutableChecker() {
		if (executableCheckers.size() == 1) {
			return (IExecutableChecker<BEAN_TYPE>) executableCheckers.iterator().next();
		}
		else if (executableCheckers.size() > 1) {
			final IExecutableCheckerCompositeBuilder<BEAN_TYPE> builder = ExecutableCheckerComposite.builder();
			builder.set(executableCheckers);
			return builder.build();
		}
		else {
			return null;
		}
	}

	protected IDeleterServiceInterceptor<BEAN_TYPE> getInterceptor() {
		if (interceptors.size() == 1) {
			return interceptors.iterator().next();
		}
		else {
			return new DeleterServiceInterceptorComposite(interceptors);
		}
	}

	protected boolean isAllowDeletedBeans() {
		return allowDeletedBeans;
	}

	protected boolean isAllowStaleBeans() {
		return allowStaleBeans;
	}

	private final class DeleterServiceInterceptorComposite implements IDeleterServiceInterceptor<BEAN_TYPE> {

		private final List<IDeleterServiceInterceptor<BEAN_TYPE>> interceptors;

		private DeleterServiceInterceptorComposite(final List<IDeleterServiceInterceptor<BEAN_TYPE>> interceptors) {
			this.interceptors = new LinkedList<IDeleterServiceInterceptor<BEAN_TYPE>>(interceptors);
		}

		@Override
		public void beforeDelete(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {
			for (final IDeleterServiceInterceptor<BEAN_TYPE> interceptor : interceptors) {
				interceptor.beforeDelete(bean, executionCallback);
			}
		}

	}
}
