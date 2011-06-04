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

package org.jowidgets.cap.sample.app.server.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.util.Assert;

public abstract class AbstractData<DATA_TYPE extends IBean> implements IBeanAccess<DATA_TYPE> {

	private static Long currentId = Long.valueOf(0);

	private final Map<Object, DATA_TYPE> dataMap;
	private final ArrayList<DATA_TYPE> dataList;

	public AbstractData() {
		super();
		this.dataMap = new HashMap<Object, DATA_TYPE>();
		this.dataList = new ArrayList<DATA_TYPE>();
	}

	public abstract DATA_TYPE createBean();

	public void add(final DATA_TYPE object) {
		dataMap.put(object.getId(), object);
		dataList.add(object);
	}

	public DATA_TYPE getData(final Object id) {
		return dataMap.get(id);
	}

	public void deleteData(final Object id) {
		dataMap.remove(id);
		int beanIndex = -1;
		for (int i = 0; i < dataList.size(); i++) {
			if (dataList.get(i).getId().equals(id)) {
				beanIndex = i;
				break;
			}
		}
		if (beanIndex != -1) {
			dataList.remove(beanIndex);
		}
	}

	public List<? extends DATA_TYPE> getAllData() {
		return new LinkedList<DATA_TYPE>(dataList);
	}

	public List<? extends DATA_TYPE> getAllData(final int fromIndex, final int rowCount) {
		return new LinkedList<DATA_TYPE>(dataList.subList(fromIndex, Math.min(fromIndex + rowCount, dataList.size() - 1)));
	}

	public static Long nextId() {
		final Long result = currentId;
		currentId = Long.valueOf(currentId.longValue() + 1);
		return result;
	}

	@Override
	public List<DATA_TYPE> getBeans(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {
		Assert.paramNotNull(keys, "keys");
		final List<DATA_TYPE> result = new LinkedList<DATA_TYPE>();
		for (final IBeanKey key : keys) {
			final DATA_TYPE bean = getBean(key);
			if (bean != null) {
				result.add(bean);
			}
		}
		return result;
	}

	public DATA_TYPE getBean(final IBeanKey key) {
		Assert.paramNotNull(key, "key");
		return getData(key.getId());
	}

}
