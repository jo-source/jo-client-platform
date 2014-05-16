/*
 * Copyright (c) 2014, grossmann
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

package org.jowidgets.cap.service.repository.tools;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.repository.api.ICrudSupportBeanRepository;
import org.jowidgets.util.Assert;

/**
 * This class was implemented for test purpose only.
 * 
 * This class is not threadsafe !!!!
 * 
 * @author grossmann
 * 
 * @param <BEAN_TYPE> The bean type
 */
public class HashMapCrudRepository<BEAN_TYPE> implements ICrudSupportBeanRepository<BEAN_TYPE> {

	private final Class<BEAN_TYPE> beanType;
	private final HashMap<Object, BEAN_TYPE> data;

	public HashMapCrudRepository(final Class<BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");

		this.beanType = beanType;
		this.data = new LinkedHashMap<Object, BEAN_TYPE>();
	}

	public void add(final BEAN_TYPE bean) {
		Assert.paramNotNull(bean, "bean");
		data.put(getId(bean), bean);
	}

	@Override
	public final Class<BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public Object getBeanTypeId() {
		return beanType.getClass().getName();
	}

	@Override
	public Object getId(final BEAN_TYPE bean) {
		return bean;
	}

	@Override
	public long getVersion(final BEAN_TYPE bean) {
		return 0;
	}

	@Override
	public BEAN_TYPE find(final Object id, final IExecutionCallback executionCallback) {
		Assert.paramNotNull(id, "id, name");
		return data.get(id);
	}

	@Override
	public List<BEAN_TYPE> read(final List<? extends IBeanKey> parentBeanKeys, final IExecutionCallback executionCallback) {
		return new LinkedList<BEAN_TYPE>(data.values());
	}

	@Override
	public BEAN_TYPE create(final IExecutionCallback executionCallback) {
		try {
			return beanType.newInstance();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void postCreate(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {
		data.put(getId(bean), bean);
	}

	@Override
	public void preUpdate(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {}

	@Override
	public void postUpdate(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {}

	@Override
	public void delete(final BEAN_TYPE bean, final IExecutionCallback executionCallback) {
		data.remove(getId(bean));
	}

}
