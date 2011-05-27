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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeansModificationBuffer;
import org.jowidgets.cap.ui.impl.model.ModificationStateObservable;
import org.jowidgets.util.Assert;

public class BeansModificationBuffer<BEAN_TYPE> extends ModificationStateObservable implements
		IBeansModificationBuffer<BEAN_TYPE> {

	private final Set<IBeanProxy<BEAN_TYPE>> modifiedBeans;

	public BeansModificationBuffer() {
		this.modifiedBeans = new HashSet<IBeanProxy<BEAN_TYPE>>();
	}

	@Override
	public void add(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		final boolean lastHasModifications = hasModifications();
		modifiedBeans.add(bean);
		if (lastHasModifications != hasModifications()) {
			fireModificationStateChanged();
		}
	}

	@Override
	public void remove(final IBeanProxy<BEAN_TYPE> bean) {
		Assert.paramNotNull(bean, "bean");
		final boolean lastHasModifications = hasModifications();
		modifiedBeans.remove(bean);
		if (lastHasModifications != hasModifications()) {
			fireModificationStateChanged();
		}
	}

	@Override
	public Set<IBeanProxy<BEAN_TYPE>> getModifiedBeans() {
		return Collections.unmodifiableSet(modifiedBeans);
	}

	@Override
	public boolean hasModifications() {
		return !modifiedBeans.isEmpty();
	}

	@Override
	public void clear() {
		modifiedBeans.clear();
	}

}
