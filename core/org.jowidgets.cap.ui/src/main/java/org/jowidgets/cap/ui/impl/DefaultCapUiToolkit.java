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
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.executor.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IDataApiBluePrintFactory;
import org.jowidgets.cap.ui.impl.attribute.AttributeToolkit;
import org.jowidgets.cap.ui.impl.bean.BeanKeyFactory;
import org.jowidgets.cap.ui.impl.bean.DataBeanFactory;
import org.jowidgets.cap.ui.impl.executor.ExecutionTaskFactory;
import org.jowidgets.cap.ui.impl.table.BeanTableModelBuilder;
import org.jowidgets.cap.ui.impl.widgets.DataApiBluePrintFactory;

public final class DefaultCapUiToolkit implements ICapUiToolkit {

	private IDataApiBluePrintFactory bluePrintFactory;
	private IExecutionTaskFactory executionTaskFactory;
	private IBeanKeyFactory beanKeyFactory;
	private IAttributeToolkit attributeToolkit;

	@Override
	public IDataApiBluePrintFactory getBluePrintFactory() {
		if (bluePrintFactory == null) {
			bluePrintFactory = new DataApiBluePrintFactory();
		}
		return bluePrintFactory;
	}

	@Override
	public IExecutionTaskFactory getExecutionTaskFactory() {
		if (executionTaskFactory == null) {
			executionTaskFactory = new ExecutionTaskFactory();
		}
		return executionTaskFactory;
	}

	@Override
	public IAttributeToolkit getAttributeToolkit() {
		if (attributeToolkit == null) {
			attributeToolkit = new AttributeToolkit();
		}
		return attributeToolkit;
	}

	@Override
	public <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> createBeanProxyFactory(final Class<? extends BEAN_TYPE> beanType) {
		return new DataBeanFactory<BEAN_TYPE>(beanType);
	}

	@Override
	public IBeanKeyFactory getBeanKeyFactory() {
		if (beanKeyFactory == null) {
			beanKeyFactory = new BeanKeyFactory();
		}
		return beanKeyFactory;
	}

	@Override
	public <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> createBeanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return new BeanTableModelBuilder<BEAN_TYPE>(beanType);
	}

}
