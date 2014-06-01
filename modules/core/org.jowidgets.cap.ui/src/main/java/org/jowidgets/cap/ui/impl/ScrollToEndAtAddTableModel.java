/*
 * Copyright (c) 2014, Michael
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

package org.jowidgets.cap.ui.impl;

import java.util.Collections;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IDataModelContextProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.tools.model.BeanListModelWrapper;

public class ScrollToEndAtAddTableModel<BEAN_TYPE> extends BeanListModelWrapper<BEAN_TYPE> implements
		IBeanListModel<BEAN_TYPE>,
		IDataModelContextProvider {

	private final IBeanTable<BEAN_TYPE> table;
	private final IBeanTableModel<BEAN_TYPE> model;

	ScrollToEndAtAddTableModel(final IBeanTable<BEAN_TYPE> table) {
		super(table.getModel());
		this.table = table;
		this.model = table.getModel();
	}

	@Override
	public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
		super.addBean(bean);
		if (model.hasLastBean() && model.getSize() > 1) {
			model.setSelection(Collections.singletonList(Integer.valueOf(model.getSize() - 2)));
			table.scrollToSelection();
		}
		else if (model.getSize() > 0) {
			model.setSelection(Collections.singletonList(Integer.valueOf(model.getSize() - 1)));
			table.scrollToSelection();
		}
	}

	@Override
	public IDataModelContext getDataModelContext() {
		return model.getDataModelContext();
	}

}
