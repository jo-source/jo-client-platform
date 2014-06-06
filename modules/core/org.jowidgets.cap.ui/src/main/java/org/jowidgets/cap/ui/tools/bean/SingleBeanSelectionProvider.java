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
package org.jowidgets.cap.ui.tools.bean;

import java.util.Collections;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.util.NullCompatibleEquivalence;

public class SingleBeanSelectionProvider<BEAN_TYPE> extends BeanSelectionObservable<BEAN_TYPE> implements
		IBeanSelectionProvider<BEAN_TYPE> {

	private final Object beanTypeId;
	private final Class<? extends BEAN_TYPE> beanType;
	private final Object entityId;

	private IBeanSelection<BEAN_TYPE> selection;
	private IBeanProxy<BEAN_TYPE> selectedBean;

	public SingleBeanSelectionProvider(final Object beanTypeId, final Class<? extends BEAN_TYPE> beanType, final Object entityId) {
		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
		this.entityId = entityId;

		final Set<IBeanProxy<BEAN_TYPE>> emptySet = Collections.emptySet();
		this.selection = new BeanSelectionImpl<BEAN_TYPE>(beanTypeId, beanType, entityId, emptySet);
	}

	/**
	 * Sets the selected bean of this provider
	 * 
	 * @param bean The bean to set
	 */
	public void setSelection(final IBeanProxy<BEAN_TYPE> bean) {
		if (!NullCompatibleEquivalence.equals(selectedBean, bean)) {
			selectedBean = bean;
			if (bean == null) {
				final Set<IBeanProxy<BEAN_TYPE>> emptySet = Collections.emptySet();
				this.selection = new BeanSelectionImpl<BEAN_TYPE>(beanTypeId, beanType, entityId, emptySet);
			}
			else {
				final Set<IBeanProxy<BEAN_TYPE>> singleton = Collections.singleton(bean);
				this.selection = new BeanSelectionImpl<BEAN_TYPE>(beanTypeId, beanType, entityId, singleton);
			}
			fireBeanSelectionEvent(new BeanSelectionEventImpl<BEAN_TYPE>(
				this,
				beanTypeId,
				beanType,
				entityId,
				selection.getSelection()));
		}
	}

	@Override
	public IBeanSelection<BEAN_TYPE> getBeanSelection() {
		return selection;
	}

}
