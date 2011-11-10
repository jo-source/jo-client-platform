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

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.tools.attribute.AcceptEditableAttributesFilter;
import org.jowidgets.cap.ui.tools.execution.ExecutionInterceptorAdapter;
import org.jowidgets.cap.ui.tools.model.BeanListModelWrapper;
import org.jowidgets.common.types.IVetoable;

final class BeanTableCreatorActionBuilderFactory {

	private BeanTableCreatorActionBuilderFactory() {}

	static ICreatorActionBuilder createBuilder(final IBeanTable<?> table) {
		@SuppressWarnings("unchecked")
		final IBeanTableModel<Object> model = (IBeanTableModel<Object>) table.getModel();
		final IBeanListModel<Object> wrappedModel = new BeanListModelWrapper<Object>(model) {
			@Override
			public void addBean(final IBeanProxy<Object> bean) {
				super.addBean(bean);
				if (model.getSize() > 0) {
					model.setSelection(Collections.singletonList(Integer.valueOf(model.getSize() - 1)));
					table.showSelection();
				}
			}
		};
		final ICapActionFactory actionFactory = CapUiToolkit.actionFactory();
		final ICreatorActionBuilder builder = actionFactory.creatorActionBuilder(model.getBeanType(), wrappedModel);
		builder.setEntityLabelSingular(model.getEntityLabelSingular());
		builder.setCreatorService(model.getCreatorService());
		builder.setBeanForm(model.getAttributes(AcceptEditableAttributesFilter.getInstance()));
		builder.addExecutionInterceptor(new ExecutionInterceptorAdapter() {
			@Override
			public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
				final int pageCount = model.getPageCount();
				if (pageCount > 0 && !model.isPageCreated(pageCount - 1)) {
					model.loadPage(pageCount - 1);
				}
			}
		});
		return builder;
	}

}
