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

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanForm;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.event.IChangeListener;

final class SingleBeanFormImpl<BEAN_TYPE> extends ControlWrapper implements ISingleBeanForm<BEAN_TYPE> {

	private final ISingleBeanModel<BEAN_TYPE> model;

	SingleBeanFormImpl(final IComposite composite, final ISingleBeanFormBluePrint<BEAN_TYPE> bluePrint) {
		super(composite);
		this.model = bluePrint.getModel();

		final IBeanFormBluePrint<BEAN_TYPE> beanFormBp = bluePrint.getBeanForm();

		composite.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final IBeanForm<BEAN_TYPE> beanForm = composite.add(beanFormBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final IBeanProxy<BEAN_TYPE> bean = model.getBean();
		if (bean != null) {
			beanForm.setValue(model.getBean());
		}

		model.addChangeListener(new IChangeListener() {
			@Override
			public void changed() {
				beanForm.setValue(model.getBean());
			}
		});

	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public ISingleBeanModel<BEAN_TYPE> getModel() {
		return model;
	}

}
