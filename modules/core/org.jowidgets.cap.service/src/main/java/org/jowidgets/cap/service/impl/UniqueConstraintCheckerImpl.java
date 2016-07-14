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

package org.jowidgets.cap.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.exception.UniqueConstraintViolationException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.common.tools.execution.UpdateCallbackAdapter;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IUniqueConstraintChecker;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.util.Assert;

final class UniqueConstraintCheckerImpl implements IUniqueConstraintChecker {

	private final List<String> propertyNames;
	private final IReaderService<Void> readerService;
	@SuppressWarnings("unused")
	private final IBeanAccess<IBean> beanAccess;

	UniqueConstraintCheckerImpl(
		final IBeanServiceFactory serviceFactory,
		final Class<? extends IBean> beanType,
		final Object beanTypeId,
		final List<String> propertyNames) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		Assert.paramNotEmpty(propertyNames, "propertyNames");

		this.propertyNames = Collections.unmodifiableList(new LinkedList<String>(propertyNames));
		this.readerService = serviceFactory.readerService(beanType, beanTypeId, propertyNames);
		this.beanAccess = serviceFactory.beanAccess(beanType, beanTypeId);

	}

	@Override
	public void checkCreation(final Collection<? extends IBeanData> beansData, final IExecutionCallback executionCallback) {
		final IFilter filter = createFilter(beansData);
		final SyncResultCallback<Integer> resultCallback = new SyncResultCallback<Integer>();
		final List<IBeanKey> parentBeanKeys = Collections.emptyList();
		this.readerService.count(
				new UpdateCallbackAdapter<Integer>(resultCallback),
				parentBeanKeys,
				filter,
				null,
				executionCallback);
		final Integer result = resultCallback.getResultSynchronious();
		if (result != null && result.intValue() > 0) {
			throw new UniqueConstraintViolationException(propertyNames);
		}
	}

	private IFilter createFilter(final Collection<? extends IBeanData> beansData) {
		// TODO MG implement createFilter method
		return null;
	}

	@Override
	public void checkUpdate(
		final Collection<? extends IBeanModification> modifications,
		final IExecutionCallback executionCallback) {
		// TODO MG implement check update method

	}

}
