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
import java.util.List;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.util.Assert;

public final class SingleBeanSelectionProvider<BEAN_TYPE> implements IBeanSelectionProvider<BEAN_TYPE> {

	private final IBeanSelection<BEAN_TYPE> beanSelection;

	public SingleBeanSelectionProvider(
		final IBeanProxy<BEAN_TYPE> bean,
		final Object entityId,
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType) {

		final List<IBeanProxy<BEAN_TYPE>> selection;
		if (bean != null) {
			Assert.paramNotNull(entityId, "entityId");
			Assert.paramNotNull(beanTypeId, "beanTypeId");
			Assert.paramNotNull(beanType, "beanType");
			selection = Collections.singletonList(bean);
		}
		else {
			selection = Collections.emptyList();
		}

		this.beanSelection = new IBeanSelection<BEAN_TYPE>() {

			@Override
			public Object getBeanTypeId() {
				return beanTypeId;
			}

			@Override
			public Class<? extends BEAN_TYPE> getBeanType() {
				return beanType;
			}

			@Override
			public Object getEntityId() {
				return entityId;
			}

			@Override
			public List<IBeanProxy<BEAN_TYPE>> getSelection() {
				return selection;
			}

			@Override
			public IBeanProxy<BEAN_TYPE> getFirstSelected() {
				return bean;
			}
		};
	}

	@Override
	public void addBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {}

	@Override
	public void removeBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {}

	@Override
	public IBeanSelection<BEAN_TYPE> getBeanSelection() {
		return beanSelection;
	}

}
