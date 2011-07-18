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

package org.jowidgets.cap.sample1.ui.workbench.component.dynbeans;

import org.jowidgets.cap.sample1.common.entity.IDynamicPropertiesBean;
import org.jowidgets.cap.sample1.common.service.reader.DynamicPropertiesBeanReaderServices;
import org.jowidgets.cap.sample1.ui.attribute.DynamicPropertiesBeanAttributesFactory;
import org.jowidgets.cap.sample1.ui.workbench.command.WorkbenchActions;
import org.jowidgets.cap.sample1.ui.workbench.component.dynbeans.view.DynamicPropertiesBeanTableView;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.workbench.api.IComponent;
import org.jowidgets.workbench.api.IComponentContext;
import org.jowidgets.workbench.api.IView;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.tools.AbstractComponent;

public class DynamicPropertiesBeanComponent extends AbstractComponent implements IComponent {

	private final IBeanTableModel<IDynamicPropertiesBean> tableModel;

	public DynamicPropertiesBeanComponent(final IComponentNodeModel componentNodeModel, final IComponentContext componentContext) {
		componentContext.setLayout(new DynamicPropertiesBeanComponentDefaultLayout().getLayout());
		this.tableModel = createTableModel();
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (DynamicPropertiesBeanTableView.ID.equals(viewId)) {
			return new DynamicPropertiesBeanTableView(context, tableModel);
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public void onDispose() {}

	@Override
	public void onActivation() {
		WorkbenchActions.loadAction().addDataModel(tableModel);
		WorkbenchActions.saveAction().addDataModel(tableModel);
		WorkbenchActions.undoAction().addDataModel(tableModel);
		WorkbenchActions.cancelAction().addDataModel(tableModel);
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		WorkbenchActions.loadAction().removeDataModel(tableModel);
		WorkbenchActions.saveAction().removeDataModel(tableModel);
		WorkbenchActions.undoAction().removeDataModel(tableModel);
		WorkbenchActions.cancelAction().removeDataModel(tableModel);
	}

	private IBeanTableModel<IDynamicPropertiesBean> createTableModel() {
		final IBeanTableModelBuilder<IDynamicPropertiesBean> builder = CapUiToolkit.createBeanTableModelBuilder(IDynamicPropertiesBean.class);
		builder.setAttributes(new DynamicPropertiesBeanAttributesFactory().tableAttributes());
		builder.setReaderService(DynamicPropertiesBeanReaderServices.ALL_BEANS);
		return builder.build();
	}

}
