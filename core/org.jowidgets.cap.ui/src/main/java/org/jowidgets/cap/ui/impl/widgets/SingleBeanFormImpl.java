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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanForm;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;

final class SingleBeanFormImpl<BEAN_TYPE> extends ControlWrapper implements ISingleBeanForm<BEAN_TYPE> {

	private final ISingleBeanModel<BEAN_TYPE> model;
	private final boolean hideReadonlyAttributes;
	private final boolean hideMetaAttributes;

	SingleBeanFormImpl(final IComposite composite, final ISingleBeanFormBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);
		this.model = bluePrint.getModel();
		this.hideReadonlyAttributes = bluePrint.getHideReadonlyAttributes();
		this.hideMetaAttributes = bluePrint.getHideMetaAttributes();
		composite.add(BPF.textLabel().setText("TODO"));
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@SuppressWarnings("unused")
	private List<IAttribute<?>> getFilteredAttributes(final List<IAttribute<?>> attributes) {
		final List<IAttribute<?>> result = new LinkedList<IAttribute<?>>();
		for (final IAttribute<?> attribute : attributes) {
			if ((!hideReadonlyAttributes || attribute.isEditable())
				&& (!hideMetaAttributes || !IBeanProxy.ALL_META_ATTRIBUTES.contains(attribute.getPropertyName()))) {
				result.add(attribute);
			}
		}
		return result;
	}

	@Override
	public ISingleBeanModel<BEAN_TYPE> getModel() {
		return model;
	}

}
