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
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;

final class UpdaterServiceBuilderImpl<BEAN_TYPE extends IBean> implements IUpdaterServiceBuilder<BEAN_TYPE> {

	private final ExecutorServiceBuilderImpl<BEAN_TYPE, Collection<? extends IBeanModification>> dataExecutorServiceBuilder;

	UpdaterServiceBuilderImpl(final IBeanAccess<? extends BEAN_TYPE> beanAccess) {
		this.dataExecutorServiceBuilder = new ExecutorServiceBuilderImpl<BEAN_TYPE, Collection<? extends IBeanModification>>(
			beanAccess);
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setExecutableChecker(final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		dataExecutorServiceBuilder.setExecutableChecker(executableChecker);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setPropertyNames(final List<String> propertyNames) {
		dataExecutorServiceBuilder.setPropertyNames(propertyNames);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setAllowDeletedBeans(final boolean allowDeletedBeans) {
		dataExecutorServiceBuilder.setAllowDeletedBeans(allowDeletedBeans);
		return this;
	}

	@Override
	public IUpdaterServiceBuilder<BEAN_TYPE> setAllowStaleBeans(final boolean allowStaleBeans) {
		dataExecutorServiceBuilder.setAllowStaleBeans(allowStaleBeans);
		return this;
	}

	@Override
	public IUpdaterService build() {
		return new UpdaterServiceImpl<BEAN_TYPE>(dataExecutorServiceBuilder);
	}
}
