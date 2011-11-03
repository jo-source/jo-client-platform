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

package org.jowidgets.cap.service.impl.dummy.service;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.adapter.ISyncDeleterService;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.util.Assert;

final class SyncDeleterService implements ISyncDeleterService {

	private final IEntityData<? extends IBean> data;
	private final boolean allowDeletedData;
	private final boolean allowStaleData;

	SyncDeleterService(final IEntityData<? extends IBean> data, final boolean allowDeletedData, final boolean allowStaleData) {
		Assert.paramNotNull(data, "data");

		this.data = data;
		this.allowDeletedData = allowDeletedData;
		this.allowStaleData = allowStaleData;
	}

	@Override
	public void delete(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {

		for (final IBeanKey key : keys) {
			final IBean bean = data.getData(key.getId());
			if (!allowDeletedData && bean == null) {
				throw new DeletedBeanException(key);
			}
			if (!allowStaleData && bean != null && bean.getVersion() != key.getVersion()) {
				throw new StaleBeanException(key);
			}
			data.deleteData(key.getId());
		}
	}
}
