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

import org.jowidgets.cap.ui.api.ICapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.command.IActionFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;

public final class DefaultCapUiToolkit implements ICapUiToolkit {

	private ICapApiBluePrintFactory bluePrintFactory;
	private IActionFactory commandFactory;
	private IExecutionTaskFactory executionTaskFactory;
	private IBeanKeyFactory beanKeyFactory;
	private IAttributeToolkit attributeToolkit;

	@Override
	public ICapApiBluePrintFactory getBluePrintFactory() {
		if (bluePrintFactory == null) {
			bluePrintFactory = new CapApiBluePrintFactory();
		}
		return bluePrintFactory;
	}

	@Override
	public IActionFactory getActionFactory() {
		if (commandFactory == null) {
			commandFactory = new ActionFactoryImpl();
		}
		return commandFactory;
	}

	@Override
	public IAttributeToolkit getAttributeToolkit() {
		if (attributeToolkit == null) {
			attributeToolkit = new AttributeToolkitImpl();
		}
		return attributeToolkit;
	}

	@Override
	public <BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> createBeansStateTracker() {
		return new BeansStateTrackerImpl<BEAN_TYPE>();
	}

	@Override
	public <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> createBeanProxyFactory(final Class<? extends BEAN_TYPE> beanType) {
		return new BeanProxyFactoryImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public IBeanKeyFactory getBeanKeyFactory() {
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
	public <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> createBeanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return new BeanTableModelBuilderImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public IBeanMessageBuilder beanMessageBuilder(final BeanMessageType type) {
		//TODO MG implement beanMessageBuilder
		return null;
	}

	@Override
	public IBeanMessageFixBuilder beanMessageFixBuilder() {
		//TODO MG implement beanMessageBuilder
		return null;
	}

}
