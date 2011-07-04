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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.IValidationLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.defaults.IDefaultInitializer;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.command.IActionFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.sort.ISortModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.impl.DefaultCapUiToolkit;
import org.jowidgets.cap.ui.impl.widgets.BeanFormFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTableFactory;
import org.jowidgets.common.types.TableSelectionPolicy;
import org.jowidgets.common.widgets.factory.IGenericWidgetFactory;

public final class CapUiToolkit {

	private static ICapUiToolkit dataUiToolkit = createDefaultInstance();

	private CapUiToolkit() {}

	public static ICapUiToolkit getInstance() {
		return dataUiToolkit;
	}

	public static ICapApiBluePrintFactory getBluePrintFactory() {
		return getInstance().getBluePrintFactory();
	}

	public static IActionFactory getActionFactory() {
		return getInstance().getActionFactory();
	}

	public static IAttributeToolkit getAttributeToolkit() {
		return getInstance().getAttributeToolkit();
	}

	public static IExecutionTaskFactory executionTaskFactory() {
		return getInstance().executionTaskFactory();
	}

	public static <BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> createBeansStateTracker() {
		return getInstance().createBeansStateTracker();
	}

	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> createBeanProxyFactory(final Class<? extends BEAN_TYPE> proxyType) {
		return getInstance().createBeanProxyFactory(proxyType);
	}

	public static IBeanKeyFactory getBeanKeyFactory() {
		return getInstance().getBeanKeyFactory();
	}

	public static <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> createBeanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().createBeanTableModelBuilder(beanType);
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

	private static ICapUiToolkit createDefaultInstance() {
		registerWidgets();
		return new DefaultCapUiToolkit();
	}

	@SuppressWarnings("unchecked")
	private static void registerWidgets() {
		final IGenericWidgetFactory genericWidgetFactory = Toolkit.getWidgetFactory();
		genericWidgetFactory.register(IBeanTableBluePrint.class, new BeanTableFactory());
		genericWidgetFactory.register(IBeanFormBluePrint.class, new BeanFormFactory());

		Toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanTableBluePrint.class,
				new IDefaultInitializer<IBeanTableBluePrint<?>>() {

					@Override
					public void initialize(final IBeanTableBluePrint<?> bluePrint) {
						bluePrint.setSelectionPolicy(TableSelectionPolicy.MULTI_ROW_SELECTION);
						bluePrint.setColumnsMoveable(true);
						bluePrint.setColumnsResizeable(true);
					}
				});

		Toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanFormBluePrint.class,
				new IDefaultInitializer<IBeanFormBluePrint<?>>() {

					@Override
					public void initialize(final IBeanFormBluePrint<?> bluePrint) {
						bluePrint.setAutoResetValidation(true);
						bluePrint.setContentScrolled(true);
						final IValidationLabelBluePrint validationLabelBp = Toolkit.getBluePrintFactory().validationLabel();
						bluePrint.setValidationLabel(validationLabelBp);
					}
				});
	}

}
