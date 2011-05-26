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

package org.jowidgets.cap.ui.impl.bean;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.util.Assert;
import org.jowidgets.util.NullCompatibleEquivalence;

final class BeanProxyImpl<BEAN_TYPE> extends PropertyChangeObservable implements IBeanProxy<BEAN_TYPE> {

	private final IBeanDto beanDto;
	private final Class<? extends BEAN_TYPE> beanType;
	private final Map<String, IBeanModification> modifications;

	private BEAN_TYPE proxy;

	public BeanProxyImpl(final IBeanDto beanDto, final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanDto, "beanDto");
		Assert.paramNotNull(beanType, "beanType");

		this.beanDto = beanDto;
		this.beanType = beanType;
		this.modifications = new HashMap<String, IBeanModification>();
	}

	@Override
	public Object getId() {
		return beanDto.getId();
	}

	@Override
	public long getVersion() {
		return beanDto.getVersion();
	}

	@Override
	public Object getValue(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		if (modifications.containsKey(propertyName)) {
			return modifications.get(propertyName).getNewValue();
		}
		else {
			return beanDto.getValue(propertyName);
		}
	}

	@Override
	public void setValue(final String propertyName, final Object newValue) {
		Assert.paramNotNull(propertyName, "propertyName");

		final Object originalValue = beanDto.getValue(propertyName);
		final Object currentValue = getValue(propertyName);

		//set to the original value
		if (NullCompatibleEquivalence.equals(originalValue, newValue)) {
			modifications.remove(propertyName);
			firePropertyChange(this, propertyName, currentValue, newValue);
		}
		else if (!NullCompatibleEquivalence.equals(currentValue, newValue)) {
			final IBeanModificationBuilder modBuilder = CapCommonToolkit.beanModificationBuilder();
			modBuilder.setBeanDto(beanDto).setPropertyName(propertyName).setNewValue(newValue);
			modifications.put(propertyName, modBuilder.build());
			firePropertyChange(this, propertyName, currentValue, newValue);
		}
	}

	@Override
	public Collection<IBeanModification> getModifications() {
		return modifications.values();
	}

	@Override
	public boolean hasModifications() {
		return modifications.size() > 0;
	}

	@Override
	public void undoModifications() {
		modifications.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BEAN_TYPE getBean() {
		if (proxy == null) {
			proxy = (BEAN_TYPE) Proxy.newProxyInstance(
					beanType.getClassLoader(),
					new Class[] {beanType},
					new BeanProxyInvocationHandler(this, beanType));
		}
		return proxy;
	}

	@Override
	public String toString() {
		return "DataBeanImpl [beanDto=" + beanDto + ", beanType=" + beanType + ", modifications=" + modifications + "]";
	}

	@Override
	public int hashCode() {
		return beanDto.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		else {
			return beanDto.equals(((BeanProxyImpl<?>) obj).beanDto);
		}
	}

}
