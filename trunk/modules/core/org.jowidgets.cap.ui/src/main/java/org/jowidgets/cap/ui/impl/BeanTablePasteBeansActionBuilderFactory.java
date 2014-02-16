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

package org.jowidgets.cap.ui.impl;

import java.util.Collections;

import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.IPasteBeansActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.tools.model.BeanListModelWrapper;

final class BeanTablePasteBeansActionBuilderFactory {

	private BeanTablePasteBeansActionBuilderFactory() {}

	static <BEAN_TYPE> IPasteBeansActionBuilder<BEAN_TYPE> createBuilder(final IBeanTable<BEAN_TYPE> table) {
		final IBeanTableModel<BEAN_TYPE> model = table.getModel();
		final IBeanListModel<BEAN_TYPE> wrappedModel = new BeanListModelWrapper<BEAN_TYPE>(model) {
			@Override
			public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
				super.addBean(bean);
				if (model.getSize() > 0) {
					model.setSelection(Collections.singletonList(Integer.valueOf(model.getSize() - 1)));
					table.scrollToSelection();
				}
			}
		};
		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		final IPasteBeansActionBuilder<BEAN_TYPE> builder = actionFactory.pasteBeansActionBuilder(
				model.getEntityId(),
				model.getBeanType(),
				wrappedModel);

		builder.setEntityLabelPlural(model.getEntityLabelPlural());
		builder.setAttributes(model.getAttributes());

		return builder;
	}

}
