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

import org.jowidgets.cap.security.common.api.AuthorizationChecker;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.ui.api.ISecureServiceActionDecoratorPluginBuilder;
import org.jowidgets.cap.ui.api.plugin.IServiceActionDecoratorPlugin;
import org.jowidgets.util.Assert;

final class SecureServiceActionDecoratorPluginBuilderImpl<AUTHORIZATION_TYPE> implements
		ISecureServiceActionDecoratorPluginBuilder<AUTHORIZATION_TYPE> {

	private IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureServiceActionDecoratorPluginBuilderImpl() {
		this.authorizationChecker = AuthorizationChecker.get();
	}

	@Override
	public ISecureServiceActionDecoratorPluginBuilder<AUTHORIZATION_TYPE> setAuthorizationChecker(
		final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker) {
		Assert.paramNotNull(authorizationChecker, "authorizationChecker");
		this.authorizationChecker = authorizationChecker;
		return this;
	}

	@Override
	public IServiceActionDecoratorPlugin build() {
		return new SecureServiceActionDecoratorPluginImpl<AUTHORIZATION_TYPE>(authorizationChecker);
	}
}
