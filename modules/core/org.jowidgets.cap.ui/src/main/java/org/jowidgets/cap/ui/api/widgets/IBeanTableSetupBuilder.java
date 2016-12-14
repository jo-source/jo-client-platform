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

package org.jowidgets.cap.ui.api.widgets;

import org.jowidgets.api.types.AutoPackPolicy;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.api.widgets.blueprint.ITableBluePrintFactory;
import org.jowidgets.api.widgets.blueprint.builder.IComponentSetupBuilder;
import org.jowidgets.api.widgets.descriptor.setup.IComponentSetup;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.model.ILabelRenderer;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.types.AutoScrollPolicy;
import org.jowidgets.common.types.TableSelectionPolicy;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.descriptor.setup.mandatory.Mandatory;

public interface IBeanTableSetupBuilder<INSTANCE_TYPE extends IBeanTableSetupBuilder<?, BEAN_TYPE>, BEAN_TYPE>
		extends IComponentSetup, IComponentSetupBuilder<INSTANCE_TYPE>, IBeanTableSetupConvenience<INSTANCE_TYPE, BEAN_TYPE> {

	INSTANCE_TYPE setModel(IBeanTableModel<BEAN_TYPE> model);

	INSTANCE_TYPE setTableBluePrintFactory(ITableBluePrintFactory<? extends IWidgetDescriptor<ITable>> bpf);

	INSTANCE_TYPE setSelectionPolicy(TableSelectionPolicy selectionPolicy);

	INSTANCE_TYPE setColumnsMoveable(boolean columnsMoveable);

	INSTANCE_TYPE setColumnsResizeable(boolean columnsResizeable);

	INSTANCE_TYPE setDefaultMenus(boolean defaultMenus);

	INSTANCE_TYPE setDefaultCreatorAction(boolean creatorAction);

	INSTANCE_TYPE setDefaultDeleterAction(boolean deleterAction);

	INSTANCE_TYPE setDefaultEditAction(boolean editAction);

	INSTANCE_TYPE setDefaultCopyAction(boolean copyAction);

	INSTANCE_TYPE setDefaultPasteAction(boolean pasteAction);

	INSTANCE_TYPE setDefaultMoveOrderedBeanAction(boolean moveOrderedBeanAction);

	INSTANCE_TYPE setHeaderMenuInterceptor(ITableMenuCreationInterceptor<BEAN_TYPE> interceptor);

	INSTANCE_TYPE setCellMenuInterceptor(ITableMenuCreationInterceptor<BEAN_TYPE> interceptor);

	INSTANCE_TYPE setMenuInterceptor(IBeanTableMenuInterceptor<BEAN_TYPE> interceptor);

	INSTANCE_TYPE setSearchFilterToolbarVisible(boolean visible);

	INSTANCE_TYPE setValidationLabelVisible(boolean visible);

	INSTANCE_TYPE setStatusBarVisible(boolean visible);

	INSTANCE_TYPE setStatusBarRenderer(ILabelRenderer<IBeanTable<BEAN_TYPE>> renderer);

	/**
	 * Determines if the auto update settings can be configured by the user
	 * 
	 * @param configurable
	 * 
	 * @return This builder
	 */
	INSTANCE_TYPE setAutoUpdateConfigurable(boolean configurable);

	/**
	 * Sets the interval for the auto update in milliseconds
	 * 
	 * @param autoUpdateInterval The auto update interval in seconds
	 * 
	 * @return This builder
	 */
	INSTANCE_TYPE setAutoUpdateInterval(int autoUpdateInterval);

	/**
	 * Sets the auto scroll policy that will be used after auto update changed the data
	 * 
	 * @param autoScrollPolicy The auto scroll policy to set
	 * 
	 * @return This builder
	 */
	INSTANCE_TYPE setAutoScrollPolicy(AutoScrollPolicy autoScrollPolicy);

	INSTANCE_TYPE setAutoPackPolicy(AutoPackPolicy autoPackPolicy);

	INSTANCE_TYPE setAutoKeyBinding(boolean autoKeyBinding);

	INSTANCE_TYPE setEditable(boolean editable);

	INSTANCE_TYPE setRowHeight(Integer rowHeight);

	INSTANCE_TYPE setValidationLabel(IValidationLabelSetup valiadationLabel);

	@Mandatory
	ITableBluePrintFactory<? extends IWidgetDescriptor<ITable>> getTableBluePrintFactory();

	@Mandatory
	TableSelectionPolicy getSelectionPolicy();

	@Mandatory
	boolean getColumnsMoveable();

	@Mandatory
	boolean getColumnsResizeable();

	@Mandatory
	IBeanTableModel<BEAN_TYPE> getModel();

	@Mandatory
	boolean hasDefaultMenus();

	@Mandatory
	boolean hasDefaultCreatorAction();

	@Mandatory
	boolean hasDefaultDeleterAction();

	@Mandatory
	boolean hasDefaultEditAction();

	@Mandatory
	boolean hasDefaultCopyAction();

	@Mandatory
	boolean hasDefaultPasteAction();

	@Mandatory
	boolean hasDefaultMoveOrderedBeanAction();

	@Mandatory
	boolean isEditable();

	ITableMenuCreationInterceptor<BEAN_TYPE> getHeaderMenuInterceptor();

	ITableMenuCreationInterceptor<BEAN_TYPE> getCellMenuInterceptor();

	IBeanTableMenuInterceptor<BEAN_TYPE> getMenuInterceptor();

	@Mandatory
	boolean getSearchFilterToolbarVisible();

	@Mandatory
	boolean getStatusBarVisible();

	@Mandatory
	boolean getValidationLabelVisible();

	ILabelRenderer<IBeanTable<BEAN_TYPE>> getStatusBarRenderer();

	@Mandatory
	boolean getAutoUpdateConfigurable();

	@Mandatory
	int getAutoUpdateInterval();

	@Mandatory
	AutoScrollPolicy getAutoScrollPolicy();

	@Mandatory
	AutoPackPolicy getAutoPackPolicy();

	@Mandatory
	boolean getAutoKeyBinding();

	@Mandatory
	IValidationLabelSetup getValidationLabel();

	Integer getRowHeight();

}
