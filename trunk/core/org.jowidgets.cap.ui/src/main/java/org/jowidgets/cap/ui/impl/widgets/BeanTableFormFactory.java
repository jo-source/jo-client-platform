/*
 * Copyright (c) 2010, grossmann
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

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableView;
import org.jowidgets.cap.ui.api.widgets.IBeanTableViewListener;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesForm;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.widgets.factory.IWidgetFactory;

public final class BeanTableFormFactory implements IWidgetFactory<IControl, IBeanTableFormBluePrint<?>> {

	@SuppressWarnings("unchecked")
	@Override
	public IControl create(final Object parentUiReference, final IBeanTableFormBluePrint<?> bluePrint) {
		final ICapApiBluePrintFactory bpf = CapUiToolkit.bluePrintFactory();
		final IBeanTablesForm tablesForm = Toolkit.getWidgetFactory().create(parentUiReference, bpf.beanTablesForm());
		final IBeanFormBluePrint<Object> beanFormBluePrint = (IBeanFormBluePrint<Object>) bluePrint.getBeanFormBluePrint();
		if (beanFormBluePrint != null) {
			tablesForm.registerView(new DummyBeanTableView(bluePrint.getModel()), beanFormBluePrint);
		}
		else {
			tablesForm.registerView(new DummyBeanTableView(bluePrint.getModel()));
		}
		return tablesForm;
	}

	private final class DummyBeanTableView implements IBeanTableView<Object> {

		private final IBeanTableModel<Object> model;

		@SuppressWarnings("unchecked")
		private DummyBeanTableView(final IBeanTableModel<?> model) {
			this.model = (IBeanTableModel<Object>) model;
		}

		@Override
		public void addDisposeListener(final IDisposeListener listener) {}

		@Override
		public void removeDisposeListener(final IDisposeListener listener) {}

		@Override
		public void addViewListener(final IBeanTableViewListener listener) {}

		@Override
		public void removeViewListener(final IBeanTableViewListener listener) {}

		@Override
		public IBeanTableModel<Object> getModel() {
			return model;
		}

	}
}
