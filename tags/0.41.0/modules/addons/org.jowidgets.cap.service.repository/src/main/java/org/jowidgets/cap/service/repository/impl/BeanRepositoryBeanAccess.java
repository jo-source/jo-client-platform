/*
 * Copyright (c) 2014, Michael
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

package org.jowidgets.cap.service.repository.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.repository.api.IBeanRepository;
import org.jowidgets.util.Assert;

final class BeanRepositoryBeanAccess<BEAN_TYPE> implements IBeanAccess<BEAN_TYPE> {

	private final IBeanRepository<BEAN_TYPE> repository;

	BeanRepositoryBeanAccess(final IBeanRepository<BEAN_TYPE> repository) {
		Assert.paramNotNull(repository, "repository");
		this.repository = repository;
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return repository.getBeanType();
	}

	@Override
	public Object getBeanTypeId() {
		return repository.getBeanTypeId();
	}

	@Override
	public Object getId(final BEAN_TYPE bean) {
		return repository.getId(bean);
	}

	@Override
	public long getVersion(final BEAN_TYPE bean) {
		return repository.getVersion(bean);
	}

	@Override
	public List<BEAN_TYPE> getBeans(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {
		final List<BEAN_TYPE> result = new LinkedList<BEAN_TYPE>();
		for (final IBeanKey key : keys) {
			final BEAN_TYPE bean = repository.find(key.getId(), executionCallback);
			if (bean != null) {
				result.add(bean);
			}
			CapServiceToolkit.checkCanceled(executionCallback);
		}
		return result;
	}

	@Override
	public void flush() {}

}
