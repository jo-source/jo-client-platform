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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.control.IDisplayFormatFactory;
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
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableSettingsBuilder;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModelBuilder;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.workbench.ICapWorkbenchToolkit;
import org.jowidgets.cap.ui.impl.DefaultCapUiToolkit;
import org.jowidgets.util.ITypedKey;

public final class CapUiToolkit {

	private static final ITypedKey<ICapUiToolkit> CAP_TOOKIT_KEY = new ITypedKey<ICapUiToolkit>() {};

	private CapUiToolkit() {}

	public static void initialize() {
		getInstance();
	}

	public static ICapUiToolkit getInstance() {
		//create one instance of the cap ui toolkit for each instance of the widgets toolkit
		ICapUiToolkit result = Toolkit.getValue(CAP_TOOKIT_KEY);
		if (result == null) {
			result = new DefaultCapUiToolkit();
			Toolkit.setValue(CAP_TOOKIT_KEY, result);
		}
		return result;
	}

	public static ICapApiBluePrintFactory bluePrintFactory() {
		return getInstance().bluePrintFactory();
	}

	public static ICapActionFactory actionFactory() {
		return getInstance().actionFactory();
	}

	public static ICapConverterFactory converterFactory() {
		return getInstance().converterFactory();
	}

	public static IUiServiceDecoratorProviderFactory serviceDecoratorFactory() {
		return getInstance().serviceDecoratorFactory();
	}

	public static <BEAN_TYPE> IBeanTableMenuFactory<BEAN_TYPE> beanTableMenuFactory(
		final Collection<IBeanTableMenuInterceptor<BEAN_TYPE>> interceptors) {
		return getInstance().beanTableMenuFactory(interceptors);
	}

	public static <BEAN_TYPE> IBeanTableMenuFactory<BEAN_TYPE> beanTableMenuFactory() {
		return getInstance().beanTableMenuFactory();
	}

	public static IDisplayFormatFactory displayFormatFactory() {
		return getInstance().displayFormatFactory();
	}

	public static IInputControlSupportRegistry inputControlRegistry() {
		return getInstance().inputControlRegistry();
	}

	public static IAttributeToolkit attributeToolkit() {
		return getInstance().attributeToolkit();
	}

	public static IFilterToolkit filterToolkit() {
		return getInstance().filterToolkit();
	}

	public static ICapWorkbenchToolkit workbenchToolkit() {
		return getInstance().workbenchToolkit();
	}

	public static IExecutionTaskFactory executionTaskFactory() {
		return getInstance().executionTaskFactory();
	}

	public static <BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> beansStateTracker() {
		return getInstance().beansStateTracker();
	}

	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> beanProxyFactory(final Class<? extends BEAN_TYPE> proxyType) {
		return getInstance().beanProxyFactory(proxyType);
	}

	public static IBeanKeyFactory beanKeyFactory() {
		return getInstance().beanKeyFactory();
	}

	public static <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTableModelBuilder(beanType);
	}

	public static <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTableModelBuilder(entityId, beanType);
	}

	public static IBeanTableModelBuilder<IBeanDto> beanTableModelBuilder(final Object entityId) {
		return getInstance().beanTableModelBuilder(entityId);
	}

	public static <CHILD_BEAN_TYPE> IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> beanRelationTreeModelBuilder(
		final Class<CHILD_BEAN_TYPE> beanType) {
		return getInstance().beanRelationTreeModelBuilder(beanType);
	}

	public static <CHILD_BEAN_TYPE> IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> beanRelationTreeModelBuilder(
		final Object entityId,
		final Class<CHILD_BEAN_TYPE> beanType) {
		return getInstance().beanRelationTreeModelBuilder(entityId, beanType);
	}

	public static <BEAN_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> beanTabFolderModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTabFolderModelBuilder(beanType);
	}

	public static <BEAN_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> beanTabFolderModelBuilder(
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTabFolderModelBuilder(entityId, beanType);
	}

	public static IBeanTabFolderModelBuilder<IBeanDto> beanTabFolderModelBuilder(final Object entityId) {
		return getInstance().beanTabFolderBuilder(entityId);
	}

	public static <BEAN_TYPE> ISingleBeanModelBuilder<BEAN_TYPE> singleBeanModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().singleBeanModelBuilder(beanType);
	}

	public static <BEAN_TYPE> ISingleBeanModelBuilder<BEAN_TYPE> singleBeanModelBuilder(
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {
		return getInstance().singleBeanModelBuilder(entityId, beanType);
	}

	public static ISingleBeanModelBuilder<IBeanDto> singleBeanModelBuilder(final Object entityId) {
		return getInstance().singleBeanModelBuilder(entityId);
	}

	public static IBeanTableModelConfigBuilder beanTableModelConfigBuilder() {
		return getInstance().beanTableModelConfigBuilder();
	}

	public static IBeanTableSettingsBuilder beanTableSettingsBuilder() {
		return getInstance().beanTableSettingsBuilder();
	}

	public static IBeanMessageBuilder beanMessageBuilder(final BeanMessageType type) {
		return getInstance().beanMessageBuilder(type);
	}

	public static IBeanMessageFixBuilder beanMessageFixBuilder() {
		return getInstance().beanMessageFixBuilder();
	}

	public static ISortModelConfigBuilder sortModelConfigBuilder() {
		return getInstance().sortModelConfigBuilder();
	}

	public static IBeanFormToolkit beanFormToolkit() {
		return getInstance().beanFormToolkit();
	}

	public static <BEAN_TYPE> IBeanProxyLabelRenderer<BEAN_TYPE> beanProxyLabelPatternRenderer(
		final String labelPattern,
		final Collection<? extends IAttribute<?>> attributes) {
		return getInstance().beanProxyLabelPatternRenderer(labelPattern, attributes);
	}

	public static ILookUpCache lookUpCache() {
		return getInstance().lookUpCache();
	}

}
