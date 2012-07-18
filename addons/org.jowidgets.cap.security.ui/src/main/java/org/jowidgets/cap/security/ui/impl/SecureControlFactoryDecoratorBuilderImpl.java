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

package org.jowidgets.cap.security.ui.impl;

import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.security.common.api.AuthorizationChecker;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.ui.api.ISecureControlMapper;
import org.jowidgets.cap.security.ui.api.ISecureControlFactoryDecoratorBuilder;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.IWidgetFactory;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class SecureControlFactoryDecoratorBuilderImpl<WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<? extends WIDGET_TYPE>, AUTHORIZATION_TYPE> implements
		ISecureControlFactoryDecoratorBuilder<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> {

	private final ISecureControlMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> controlAuthorizationMapper;

	private ICustomWidgetCreator<? extends IControl> controlCreator;
	private IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureControlFactoryDecoratorBuilderImpl(
		final ISecureControlMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> mapper) {

		this.controlAuthorizationMapper = mapper;
		this.authorizationChecker = AuthorizationChecker.getDefault();
	}

	@Override
	public ISecureControlFactoryDecoratorBuilder<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> setControlCreator(
		final ICustomWidgetCreator<? extends IControl> controlCreator) {
		Assert.paramNotNull(controlCreator, "controlCreator");
		this.controlCreator = controlCreator;
		return this;
	}

	@Override
	public ISecureControlFactoryDecoratorBuilder<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> setAuthorizationChecker(
		final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker) {
		Assert.paramNotNull(authorizationChecker, "authorizationChecker");
		this.authorizationChecker = authorizationChecker;
		return this;
	}

	@Override
	public IDecorator<IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE>> build() {
		return new SecureControlFactoryDecoratorImpl<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE>(
			controlAuthorizationMapper,
			authorizationChecker,
			controlCreator);
	}

}
