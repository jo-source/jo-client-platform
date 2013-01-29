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

import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ICrudAuthorizationMapper;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;

public interface ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> {

	/**
	 * Adds a mapper. Even if no mappers will be added, the default mappers will be used
	 * 
	 * Remark: Mappers will be invoked in reverse order, so mapping results (not null) of later
	 * added mappers will override the results from earlier added mappers.
	 * 
	 * @param mapper The mapper to add
	 * 
	 * @return This builder
	 */
	ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> addMapper(ICrudAuthorizationMapper<AUTHORIZATION_TYPE> mapper);

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
	ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> setAuthorizationChecker(IAuthorizationChecker<AUTHORIZATION_TYPE> checker);

	IBeanFormPlugin build();

}
