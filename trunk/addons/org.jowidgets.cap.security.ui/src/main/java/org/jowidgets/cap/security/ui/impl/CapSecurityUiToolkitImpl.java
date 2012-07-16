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

import org.jowidgets.api.model.item.IActionItemVisibilityAspectPlugin;
import org.jowidgets.cap.security.ui.api.ICapSecurityUiToolkit;
import org.jowidgets.cap.security.ui.api.ISecureActionItemVisibilityAspectPluginBuilder;
import org.jowidgets.cap.security.ui.api.ISecureBeanFormPluginBuilder;
import org.jowidgets.cap.security.ui.api.ISecureServiceActionDecoratorPluginBuilder;
import org.jowidgets.cap.security.ui.api.ISecureServiceProviderDecoratorBuilder;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.cap.ui.api.plugin.IServiceActionDecoratorPlugin;
import org.jowidgets.service.api.IServiceProviderDecoratorHolder;

public final class CapSecurityUiToolkitImpl implements ICapSecurityUiToolkit {

	@Override
	public <AUTHORIZATION_TYPE> ISecureServiceProviderDecoratorBuilder<AUTHORIZATION_TYPE> secureServiceProviderDecoratorBuilder() {
		return new SecureServiceProviderDecoratorBuilderImpl<AUTHORIZATION_TYPE>();
	}

	@Override
	public IServiceProviderDecoratorHolder secureServiceProviderDecorator() {
		return secureServiceProviderDecoratorBuilder().build();
	}

	@Override
	public <AUTHORIZATION_TYPE> ISecureServiceActionDecoratorPluginBuilder<AUTHORIZATION_TYPE> secureServiceActionDecoratorPluginBuilder() {
		return new SecureServiceActionDecoratorPluginBuilderImpl<AUTHORIZATION_TYPE>();
	}

	@Override
	public IServiceActionDecoratorPlugin secureServiceActionDecoratorPlugin() {
		return secureServiceActionDecoratorPluginBuilder().build();
	}

	@Override
	public IActionItemVisibilityAspectPlugin secureActionItemVisibilityAspectPlugin() {
		return secureActionItemVisibilityAspectPluginBuilder().build();
	}

	@Override
	public <AUTHORIZATION_TYPE> ISecureActionItemVisibilityAspectPluginBuilder<AUTHORIZATION_TYPE> secureActionItemVisibilityAspectPluginBuilder() {
		return new SecureActionItemVisibilityAspectPluginBuilderImpl<AUTHORIZATION_TYPE>();
	}

	@Override
	public <AUTHORIZATION_TYPE> ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> secureBeanFormPluginBuilder() {
		return new SecureBeanFormPluginBuilder<AUTHORIZATION_TYPE>();
	}

	@Override
	public IBeanFormPlugin secureBeanFormPlugin() {
		return secureBeanFormPluginBuilder().build();
	}

}
