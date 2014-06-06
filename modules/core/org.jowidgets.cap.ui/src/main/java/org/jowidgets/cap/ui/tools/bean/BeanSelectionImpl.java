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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.util.Assert;

public class BeanSelectionImpl<BEAN_TYPE> implements IBeanSelection<BEAN_TYPE> {

	private final Object beanTypeId;
	private final Class<? extends BEAN_TYPE> beanType;
	private final Object entityId;
	private final List<IBeanProxy<BEAN_TYPE>> selection;
	private final IBeanProxy<BEAN_TYPE> firstSelected;

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public BeanSelectionImpl() {
		this(null, null, null, Collections.EMPTY_LIST);
	}

	/**
	 * Constructor
	 * 
	 * @param beanTypeId The beanTypeId
	 * @param beanType The beanType
	 * @param entityId The selection
	 * @param selection The selection
	 */
	public BeanSelectionImpl(
		final Object beanTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final Object entityId,
		final Collection<? extends IBeanProxy<BEAN_TYPE>> selection) {

		Assert.paramNotNull(selection, "selection");
		if (!selection.isEmpty()) {
			Assert.paramNotNull(beanTypeId, "beanTypeId");
			Assert.paramNotNull(beanType, "beanType");
		}

		this.beanTypeId = beanTypeId;
		this.beanType = beanType;
		this.entityId = entityId;
		this.selection = new LinkedList<IBeanProxy<BEAN_TYPE>>(selection);

		if (selection.size() > 0) {
			firstSelected = selection.iterator().next();
		}
		else {
			firstSelected = null;
		}
	}

	@Override
	public Object getBeanTypeId() {
		return beanTypeId;
	}

	@Override
	public final Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	@Override
	public final Object getEntityId() {
		return entityId;
	}

	@Override
	public final List<IBeanProxy<BEAN_TYPE>> getSelection() {
		return selection;
	}

	@Override
	public final IBeanProxy<BEAN_TYPE> getFirstSelected() {
		return firstSelected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanType == null) ? 0 : beanType.hashCode());
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + ((selection == null) ? 0 : selection.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IBeanSelection<?>)) {
			return false;
		}
		final IBeanSelection<?> other = (IBeanSelection<?>) obj;
		if (beanType == null) {
			if (other.getBeanType() != null) {
				return false;
			}
		}
		else if (!beanType.equals(other.getBeanType())) {
			return false;
		}
		if (entityId == null) {
			if (other.getEntityId() != null) {
				return false;
			}
		}
		else if (!entityId.equals(other.getEntityId())) {
			return false;
		}
		if (selection == null) {
			if (other.getSelection() != null) {
				return false;
			}
		}
		else if (!selection.equals(other.getSelection())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BeanSelectionImpl [beanType="
			+ beanType
			+ ", entityId="
			+ entityId
			+ ", selection="
			+ selection
			+ ", firstSelected="
			+ firstSelected
			+ "]";
	}
}
