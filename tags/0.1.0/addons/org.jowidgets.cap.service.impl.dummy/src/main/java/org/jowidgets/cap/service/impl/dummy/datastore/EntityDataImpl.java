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

package org.jowidgets.cap.service.impl.dummy.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.util.Assert;

@SuppressWarnings({"rawtypes", "unchecked"})
final class EntityDataImpl<BEAN_TYPE extends IBean> implements IEntityData<BEAN_TYPE> {

	private final IEntityFactory<BEAN_TYPE> entityFactory;
	private final Class beanType;
	private final Map<Object, BEAN_TYPE> dataMap;
	private final ArrayList<BEAN_TYPE> dataList;

	private Long currentId = Long.valueOf(0);

	EntityDataImpl(final IEntityFactory<BEAN_TYPE> entityFactory) {
		Assert.paramNotNull(entityFactory, "entityFactory");
		Assert.paramNotNull(entityFactory.getBeanType(), "entityFactory.getBeanType()");
		this.entityFactory = entityFactory;
		this.beanType = entityFactory.getBeanType();
		this.dataMap = new HashMap<Object, BEAN_TYPE>();
		this.dataList = new ArrayList<BEAN_TYPE>();
	}

	@Override
	public void add(final BEAN_TYPE object) {
		dataMap.put(object.getId(), object);
		dataList.add(object);
	}

	@Override
	public BEAN_TYPE getData(final Object id) {
		return dataMap.get(id);
	}

	@Override
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

	@Override
	public List<BEAN_TYPE> getAllData() {
		return new LinkedList<BEAN_TYPE>(dataList);
	}

	@Override
	public List<BEAN_TYPE> getAllData(final int fromIndex, final int rowCount) {
		return new LinkedList<BEAN_TYPE>(dataList.subList(fromIndex, Math.min(fromIndex + rowCount, dataList.size())));
	}

	@Override
	public Long nextId() {
		final Long result = currentId;
		currentId = Long.valueOf(currentId.longValue() + 1);
		return result;
	}

	@Override
	public List<BEAN_TYPE> getBeans(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {
		Assert.paramNotNull(keys, "keys");
		final List<BEAN_TYPE> result = new LinkedList<BEAN_TYPE>();
		for (final IBeanKey key : keys) {
			final BEAN_TYPE bean = getBean(key);
			if (bean != null) {
				result.add(bean);
			}
		}
		return result;
	}

	@Override
	public BEAN_TYPE getBean(final IBeanKey key) {
		Assert.paramNotNull(key, "key");
		return getData(key.getId());
	}

	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public BEAN_TYPE createData() {
		return entityFactory.createBean(nextId());
	}

	@Override
	public void flush() {}

}
