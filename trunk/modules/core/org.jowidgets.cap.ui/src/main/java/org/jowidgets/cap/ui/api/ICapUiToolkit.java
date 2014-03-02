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

package org.jowidgets.cap.ui.api;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionClipboardBuilder;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionStringRenderer;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionTransferableFactoryBuilder;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.control.IDisplayFormatFactory;
import org.jowidgets.cap.ui.api.control.IInputControlProviderBuilder;
import org.jowidgets.cap.ui.api.control.IInputControlSupportBuilder;
import org.jowidgets.cap.ui.api.control.IInputControlSupportRegistry;
import org.jowidgets.cap.ui.api.converter.ICapConverterFactory;
import org.jowidgets.cap.ui.api.decorator.IUiServiceDecoratorProviderFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.filter.IFilterToolkit;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.lookup.ILookUpCache;
import org.jowidgets.cap.ui.api.model.ISingleBeanModelBuilder;
import org.jowidgets.cap.ui.api.sort.ISortModelConfigBuilder;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableSettingsBuilder;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModelBuilder;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.workbench.ICapWorkbenchToolkit;

public interface ICapUiToolkit {

	ICapApiBluePrintFactory bluePrintFactory();

	ICapActionFactory actionFactory();

	ICapConverterFactory converterFactory();

	IUiServiceDecoratorProviderFactory serviceDecoratorFactory();

	<BEAN_TYPE> IBeanTableMenuFactory<BEAN_TYPE> beanTableMenuFactory(
		Collection<IBeanTableMenuInterceptor<BEAN_TYPE>> interceptors);

	<BEAN_TYPE> IBeanTableMenuFactory<BEAN_TYPE> beanTableMenuFactory();

	IDisplayFormatFactory displayFormatFactory();

	IInputControlSupportRegistry inputControlRegistry();

	<ELEMENT_VALUE_TYPE> IInputControlProviderBuilder<ELEMENT_VALUE_TYPE> inputControlProviderBuilder(
		Class<ELEMENT_VALUE_TYPE> elementValueType);

	<ELEMENT_VALUE_TYPE> IInputControlSupportBuilder<ELEMENT_VALUE_TYPE> inputControlSupportBuilder();

	IAttributeToolkit attributeToolkit();

	IFilterToolkit filterToolkit();

	ICapWorkbenchToolkit workbenchToolkit();

	IExecutionTaskFactory executionTaskFactory();

	<BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> beansStateTracker();

	<BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> beansStateTracker(IBeanProxyContext context);

	<BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> beanProxyFactory(Class<? extends BEAN_TYPE> beanType);

	IBeanProxyContext beanProxyContext();

	IBeanKeyFactory beanKeyFactory();

	<BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(Class<BEAN_TYPE> beanType);

	<BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(Object entityId, Class<BEAN_TYPE> beanType);

	<CHILD_BEAN_TYPE> IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> beanRelationTreeModelBuilder(Class<CHILD_BEAN_TYPE> beanType);

	<CHILD_BEAN_TYPE> IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> beanRelationTreeModelBuilder(
		Object entityId,
		Class<CHILD_BEAN_TYPE> beanType);

	<BEAN_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> beanTabFolderModelBuilder(Class<BEAN_TYPE> beanType);

	<BEAN_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> beanTabFolderModelBuilder(Object entityId, Class<BEAN_TYPE> beanType);

	<BEAN_TYPE> ISingleBeanModelBuilder<BEAN_TYPE> singleBeanModelBuilder(Class<BEAN_TYPE> beanType);

	<BEAN_TYPE> ISingleBeanModelBuilder<BEAN_TYPE> singleBeanModelBuilder(Object entityId, Class<BEAN_TYPE> beanType);

	IBeanTableModelBuilder<IBeanDto> beanTableModelBuilder(Object entityId);

	IBeanTabFolderModelBuilder<IBeanDto> beanTabFolderBuilder(Object entityId);

	ISingleBeanModelBuilder<IBeanDto> singleBeanModelBuilder(Object entityId);

	IBeanTableModelConfigBuilder beanTableModelConfigBuilder();

	IBeanTableSettingsBuilder beanTableSettingsBuilder();

	IBeanTableConfigBuilder beanTableConfigBuilder();

	IBeanMessageBuilder beanMessageBuilder(BeanMessageType type);

	IBeanMessageFixBuilder beanMessageFixBuilder();

	ISortModelConfigBuilder sortModelConfigBuilder();

	IBeanFormToolkit beanFormToolkit();

	<BEAN_TYPE> IBeanProxyLabelRenderer<BEAN_TYPE> beanProxyLabelPatternRenderer(
		String labelPattern,
		Collection<? extends IAttribute<?>> attributes);

	<BEAN_TYPE> IEntityTypeId<BEAN_TYPE> entityTypeId(Object entityId, Class<BEAN_TYPE> beanType);

	<BEAN_TYPE> IEntityTypeId<BEAN_TYPE> entityTypeId(Class<BEAN_TYPE> beanType);

	ILookUpCache lookUpCache();

	IBeanExceptionConverter defaultExceptionConverter();

	<BEAN_TYPE> IBeanSelectionTransferableFactoryBuilder<BEAN_TYPE> beanSelectionTransferableFactoryBuilder();

	<BEAN_TYPE> IBeanSelectionStringRenderer<BEAN_TYPE> beanSelectionStringRenderer();

	IBeanSelectionClipboardBuilder beanSelectionClipboardBuilder();

}
