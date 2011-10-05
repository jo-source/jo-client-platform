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

import javax.validation.Validation;
import javax.validation.Validator;

import org.jowidgets.cap.ui.api.ICapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.control.IDisplayFormatFactory;
import org.jowidgets.cap.ui.api.control.IInputControlSupportRegistry;
import org.jowidgets.cap.ui.api.converter.ICapConverterFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.filter.IFilterToolkit;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.lookup.ILookUpCache;
import org.jowidgets.cap.ui.api.sort.ISortModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.workbench.ICapWorkbenchToolkit;
import org.jowidgets.cap.ui.impl.workbench.CapWorkbenchToolkitImpl;

public final class DefaultCapUiToolkit implements ICapUiToolkit {

	private ICapApiBluePrintFactory bluePrintFactory;
	private ICapActionFactory actionFactory;
	private ICapConverterFactory converterFactory;
	private IBeanTableMenuFactory beanTableMenuFactory;
	private IExecutionTaskFactory executionTaskFactory;
	private IBeanKeyFactory beanKeyFactory;
	private IAttributeToolkit attributeToolkit;
	private IFilterToolkit filterToolkit;
	private ICapWorkbenchToolkit workbenchToolkit;
	private Validator beanValidator;
	private IDisplayFormatFactory displayFormatFactory;
	private IInputControlSupportRegistry inputControlRegistry;
	private ILookUpCache lookUpCache;

	@Override
	public ICapApiBluePrintFactory bluePrintFactory() {
		if (bluePrintFactory == null) {
			bluePrintFactory = new CapApiBluePrintFactory();
		}
		return bluePrintFactory;
	}

	@Override
	public ICapActionFactory actionFactory() {
		if (actionFactory == null) {
			actionFactory = new CapActionFactoryImpl();
		}
		return actionFactory;
	}

	@Override
	public ICapConverterFactory converterFactory() {
		if (converterFactory == null) {
			converterFactory = new CapConverterFactoryImpl();
		}
		return converterFactory;
	}

	@Override
	public IBeanTableMenuFactory beanTableMenuFactory() {
		if (beanTableMenuFactory == null) {
			beanTableMenuFactory = new BeanTableMenuFactoryImpl();
		}
		return beanTableMenuFactory;
	}

	@Override
	public IDisplayFormatFactory displayFormatFactory() {
		if (displayFormatFactory == null) {
			displayFormatFactory = new DisplayFormatFactoryImpl();
		}
		return displayFormatFactory;
	}

	@Override
	public IInputControlSupportRegistry inputControlRegistry() {
		if (inputControlRegistry == null) {
			inputControlRegistry = new InputControlSupportRegistryImpl();
		}
		return inputControlRegistry;
	}

	@Override
	public ICapWorkbenchToolkit workbenchToolkit() {
		if (workbenchToolkit == null) {
			workbenchToolkit = new CapWorkbenchToolkitImpl();
		}
		return workbenchToolkit;
	}

	@Override
	public IAttributeToolkit attributeToolkit() {
		if (attributeToolkit == null) {
			attributeToolkit = new AttributeToolkitImpl();
		}
		return attributeToolkit;
	}

	@Override
	public IFilterToolkit filterToolkit() {
		if (filterToolkit == null) {
			filterToolkit = new FilterToolkitImpl();
		}
		return filterToolkit;
	}

	@Override
	public <BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> beansStateTracker() {
		return new BeansStateTrackerImpl<BEAN_TYPE>();
	}

	@Override
	public <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> beanProxyFactory(final Class<? extends BEAN_TYPE> beanType) {
		return new BeanProxyFactoryImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public IBeanKeyFactory beanKeyFactory() {
		if (beanKeyFactory == null) {
			beanKeyFactory = new BeanKeyFactoryImpl();
		}
		return beanKeyFactory;
	}

	@Override
	public IExecutionTaskFactory executionTaskFactory() {
		if (executionTaskFactory == null) {
			executionTaskFactory = new ExecutionTaskFactory();
		}
		return executionTaskFactory;
	}

	@Override
	public <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return new BeanTableModelBuilderImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public IBeanTableConfigBuilder beanTableConfigBuilder() {
		return new BeanTableConfigBuilder();
	}

	@Override
	public IBeanMessageBuilder beanMessageBuilder(final BeanMessageType type) {
		return new BeanMessageBuilderImpl(type);
	}

	@Override
	public IBeanMessageFixBuilder beanMessageFixBuilder() {
		return new BeanMessageFixBuilderImpl();
	}

	@Override
	public ISortModelConfigBuilder sortModelConfigBuilder() {
		return new SortModelConfigBuilder();
	}

	@Override
	public IBeanFormToolkit beanFormToolkit() {
		return new BeanFormToolkitImpl();
	}

	@Override
	public Validator beanValidator() {
		if (beanValidator == null) {
			beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		}
		return beanValidator;
	}

	@Override
	public ILookUpCache lookUpCache() {
		if (lookUpCache == null) {
			lookUpCache = new LookUpCacheImpl();
		}
		return lookUpCache;
	}

}
