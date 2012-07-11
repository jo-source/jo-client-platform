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

package org.jowidgets.cap.security.service.api;

import org.jowidgets.service.api.IServicesDecoratorProvider;

public interface ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> {

	int DEFAULT_ORDER = 2;

	/**
	 * Sets the decoration mode. The default mode is ALLOW_UNSECURE
	 * 
	 * @param mode The mode to set
	 * 
	 * @return This builder
	 */
	ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> setMode(DecorationMode mode);

	/**
	 * Sets the authorization checker. If no checker will be set, an default checker will be used, that gets the
	 * authorizations from the security context.
	 * 
	 * Remark: The default (not setting this explicit) only works, if the default context uses the IDefaultPrincipal
	 * 
	 * @param checker The checker to add
	 * 
	 * @return This builder
	 */
	ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> setAuthorizationChecker(IAuthorizationChecker<AUTHORIZATION_TYPE> checker);

	/**
	 * Sets the order of the service decorator. If not set, the DEFAULT_ORDER will be used.
	 * 
	 * @param order The order to set
	 * 
	 * @return This builder
	 */
	ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> setOrder(int order);

	/**
	 * @return A IServicesDecoratorProvider that decorates all services with the security aspect.
	 */
	IServicesDecoratorProvider build();

	enum DecorationMode {

		/**
		 * All services that have not a ISecureServiceId as id will be allowed and not security checked
		 */
		ALLOW_UNSECURE,

		/**
		 * For services that have not a ISecureServiceId as id an exception will be thrown on decoration
		 */
		DISALLOW_UNSECURE;

	}
}
