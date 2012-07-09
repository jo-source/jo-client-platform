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

package org.jowidgets.cap.service.security.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jowidgets.cap.service.security.api.IAuthorizationChecker;
import org.jowidgets.cap.service.security.api.ISecureServiceDecoratorBuilder.DecorationMode;
import org.jowidgets.cap.service.security.api.ISecureServiceId;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IdentityTransformationDecorator;

final class SecureServiceDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final int order;
	private final IAuthorizationChecker<Object> authorizationChecker;
	private final DecorationMode decorationMode;

	@SuppressWarnings("unchecked")
	public SecureServiceDecoratorProviderImpl(
		final IAuthorizationChecker<? extends Object> authorizationChecker,
		final DecorationMode decorationMode,
		final int order) {
		this.order = order;
		this.authorizationChecker = (IAuthorizationChecker<Object>) authorizationChecker;
		this.decorationMode = decorationMode;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final IServiceId<SERVICE_TYPE> id) {
		Assert.paramNotNull(id, "id");
		if (id instanceof ISecureServiceId) {
			final Object authorization = ((ISecureServiceId) id).getAuthorization();
			final Class<? extends SERVICE_TYPE> serviceType = id.getServiceType();
			return new IDecorator<SERVICE_TYPE>() {
				@Override
				public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
					final InvocationHandler invocationHandler = new SecurityInvocationHandler(original, authorization);
					return (SERVICE_TYPE) Proxy.newProxyInstance(
							serviceType.getClassLoader(),
							new Class[] {serviceType},
							invocationHandler);
				}
			};
		}
		else if (DecorationMode.ALLOW_UNSECURE == decorationMode) {
			return new IdentityTransformationDecorator<SERVICE_TYPE>();
		}
		else {
			throw new RuntimeException("The service with the id '"
				+ id
				+ "' has not the type '"
				+ ISecureServiceId.class.getName()
				+ "'");
		}
	}

	@Override
	public int getOrder() {
		return order;
	}

	private final class SecurityInvocationHandler implements InvocationHandler {

		private final Object original;
		private final Object authorization;

		private SecurityInvocationHandler(final Object original, final Object authorization) {
			this.original = original;
			this.authorization = authorization;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			authorizationChecker.checkAuthorization(authorization);
			return method.invoke(original, args);
		}

	}

}
