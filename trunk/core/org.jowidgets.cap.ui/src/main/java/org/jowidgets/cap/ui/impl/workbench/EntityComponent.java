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

package org.jowidgets.cap.ui.impl.workbench;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.entity.IEntityClass;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.workbench.api.IComponent;
import org.jowidgets.workbench.api.IComponentContext;
import org.jowidgets.workbench.api.IView;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.tools.AbstractComponent;

public class EntityComponent extends AbstractComponent implements IComponent {

	private final IBeanTableModel<Object> tableModel;
	private final List<IAttribute<Object>> tableAttributes;
	private final List<IAttribute<Object>> detailAttributes;
	private final IEntityClass entityClass;

	public EntityComponent(
		final IComponentNodeModel componentNodeModel,
		final IComponentContext componentContext,
		final IEntityClass entityClass) {

		componentContext.setLayout(new EntityComponentDefaultLayout(entityClass).getLayout());

		//get the entity service
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);

		//create the table attributes
		final List<IProperty> properties = entityService.getDescriptor(entityClass.getId()).getProperties();
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		this.tableAttributes = attributeToolkit.createAttributes(properties);

		//create the detail attributes
		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addAcceptEditableAttributesFilter();
		this.detailAttributes = attributeToolkit.createAttributes(properties, modifierBuilder.build());

		//get the services provider
		final IBeanServicesProvider servicesProvider = entityService.getBeanServices(entityClass.getId());

		this.tableModel = createTableModel(tableAttributes, servicesProvider);
		this.entityClass = entityClass;
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (EntityTableView.ID.equals(viewId)) {
			return new EntityTableView(context, tableModel, entityClass);
		}
		else if (EntityDetailView.ID.equals(viewId)) {
			return new EntityDetailView(context, tableModel, detailAttributes, entityClass);
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public void onDispose() {}

	@Override
	public void onActivation() {
		//		WorkbenchActions.loadAction().addDataModel(userTableModel);
		//		WorkbenchActions.saveAction().addDataModel(userTableModel);
		//		WorkbenchActions.undoAction().addDataModel(userTableModel);
		//		WorkbenchActions.cancelAction().addDataModel(userTableModel);
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		//		WorkbenchActions.loadAction().removeDataModel(userTableModel);
		//		WorkbenchActions.saveAction().removeDataModel(userTableModel);
		//		WorkbenchActions.undoAction().removeDataModel(userTableModel);
		//		WorkbenchActions.cancelAction().removeDataModel(userTableModel);
	}

	private IBeanTableModel<Object> createTableModel(
		final List<IAttribute<Object>> tableAttributes,
		final IBeanServicesProvider beanServicesProvider) {
		final IBeanTableModelBuilder<Object> builder = CapUiToolkit.beanTableModelBuilder(Object.class);
		builder.setAttributes(tableAttributes);
		builder.setEntityServices(beanServicesProvider);
		return builder.build();
	}

}
