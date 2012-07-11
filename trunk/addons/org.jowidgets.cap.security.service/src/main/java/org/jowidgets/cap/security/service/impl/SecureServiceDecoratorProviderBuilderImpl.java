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

package org.jowidgets.cap.security.service.impl;

import org.jowidgets.cap.common.api.exception.AuthorizationFailedException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.security.service.api.IAuthorizationChecker;
import org.jowidgets.cap.security.service.api.ISecureServiceDecoratorBuilder;
import org.jowidgets.security.api.IDefaultPrincipal;
import org.jowidgets.security.api.SecurityContextHolder;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;

final class SecureServiceDecoratorProviderBuilderImpl<AUTHORIZATION_TYPE> implements
		ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> {

	private DecorationMode decorationMode;
	private int order;
	private IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureServiceDecoratorProviderBuilderImpl() {
		this.decorationMode = DecorationMode.ALLOW_UNSECURE;
		this.authorizationChecker = new DefaultAuthorizationChecker();
		this.order = ISecureServiceDecoratorBuilder.DEFAULT_ORDER;
	}

	@Override
	public ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> setMode(final DecorationMode mode) {
		Assert.paramNotNull(decorationMode, "decorationMode");
		this.decorationMode = mode;
		return this;
	}

	@Override
	public ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> setOrder(final int order) {
		this.order = order;
		return this;
	}

	@Override
	public ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> setAuthorizationChecker(
		final IAuthorizationChecker<AUTHORIZATION_TYPE> checker) {
		Assert.paramNotNull(authorizationChecker, "authorizationChecker");
		this.authorizationChecker = checker;
		return this;
	}

	@Override
	public IServicesDecoratorProvider build() {
		return new SecureServiceDecoratorProviderImpl(authorizationChecker, decorationMode, order);
	}

	private final class DefaultAuthorizationChecker implements IAuthorizationChecker<AUTHORIZATION_TYPE> {

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public void checkAuthorization(final AUTHORIZATION_TYPE authorization) {
			final Object securityContext = SecurityContextHolder.getSecurityContext();
			if (securityContext instanceof IDefaultPrincipal) {
				final IDefaultPrincipal<AUTHORIZATION_TYPE> defaultPrincipal = (IDefaultPrincipal) securityContext;
				if (!defaultPrincipal.getGrantedAuthorities().contains(authorization)) {
					throw new AuthorizationFailedException(defaultPrincipal.getUsername(), authorization);
				}
			}
			else if (securityContext != null) {
				throw new ServiceException("Security Context has wrong type. '"
					+ IDefaultPrincipal.class
					+ "' assumed, but '"
					+ securityContext.getClass().getName()
					+ "' found.");
			}
			else {
				throw new ServiceException("No security context set");
			}

		}
	}
}
