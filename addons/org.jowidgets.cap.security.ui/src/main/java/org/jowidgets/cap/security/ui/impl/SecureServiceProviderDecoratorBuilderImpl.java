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
import org.jowidgets.cap.security.ui.api.ISecureServiceProviderDecoratorBuilder;
import org.jowidgets.service.api.IServiceProviderDecoratorHolder;

final class SecureServiceProviderDecoratorBuilderImpl<AUTHORIZATION_TYPE> implements
		ISecureServiceProviderDecoratorBuilder<AUTHORIZATION_TYPE> {

	private int order;
	private final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureServiceProviderDecoratorBuilderImpl() {
		this.order = ISecureServiceProviderDecoratorBuilder.DEFAULT_ORDER;
		this.authorizationChecker = AuthorizationChecker.getDefault();
	}

	@Override
	public ISecureServiceProviderDecoratorBuilder<AUTHORIZATION_TYPE> setOrder(final int order) {
		this.order = order;
		return this;
	}

	@Override
	public ISecureServiceProviderDecoratorBuilder<AUTHORIZATION_TYPE> setAuthorizationChecker(
		final IAuthorizationChecker<AUTHORIZATION_TYPE> checker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IServiceProviderDecoratorHolder build() {
		return new SecureServiceProviderDecoratorHolderImpl<AUTHORIZATION_TYPE>(order, authorizationChecker);
	}

}
