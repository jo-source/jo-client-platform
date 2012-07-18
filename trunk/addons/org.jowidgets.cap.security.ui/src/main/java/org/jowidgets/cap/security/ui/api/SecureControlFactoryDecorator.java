/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.security.ui.api;

import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.factory.IWidgetFactory;
import org.jowidgets.util.IDecorator;

public final class SecureControlFactoryDecorator {

	private SecureControlFactoryDecorator() {}

	public static <BEAN_TYPE> IDecorator<IWidgetFactory<IBeanTable<BEAN_TYPE>, IBeanTableBluePrint<BEAN_TYPE>>> beanTable() {
		final ISecureControlAuthorizationMapper<IBeanTable<BEAN_TYPE>, IBeanTableBluePrint<BEAN_TYPE>, Object> mapper = SecureControlAuthorizationMapperFactory.beanTable();
		return create(mapper);
	}

	public static <BEAN_TYPE, AUTHORIZATION_TYPE> ISecureControlFactoryDecoratorBuilder<IBeanTable<BEAN_TYPE>, IBeanTableBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> beanTableBuilder() {
		final ISecureControlAuthorizationMapper<IBeanTable<BEAN_TYPE>, IBeanTableBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> mapper = SecureControlAuthorizationMapperFactory.beanTable();
		return builder(mapper);
	}

	public static <WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<? extends WIDGET_TYPE>, AUTHORIZATION_TYPE> IDecorator<IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE>> create(
		final ISecureControlAuthorizationMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> mapper) {
		return CapSecurityUiToolkit.secureControlFactoryDecorator(mapper);
	}

	public static <WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<? extends WIDGET_TYPE>, AUTHORIZATION_TYPE> ISecureControlFactoryDecoratorBuilder<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> builder(
		final ISecureControlAuthorizationMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> mapper) {
		return CapSecurityUiToolkit.secureControlFactoryDecoratorBuilder(mapper);
	}
}
