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

import java.util.LinkedList;

import org.jowidgets.cap.security.common.api.AuthorizationChecker;
import org.jowidgets.cap.security.common.api.CrudAuthorizationMapperFactory;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ICrudAuthorizationMapper;
import org.jowidgets.cap.security.ui.api.ISecureBeanFormPluginBuilder;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.util.Assert;

@SuppressWarnings({"rawtypes", "unchecked"})
final class SecureBeanFormPluginBuilderImpl<AUTHORIZATION_TYPE> implements ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> {

	private final LinkedList mappers;

	private IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureBeanFormPluginBuilderImpl() {
		this.authorizationChecker = AuthorizationChecker.get();

		this.mappers = new LinkedList();
		mappers.addFirst(CrudAuthorizationMapperFactory.beanTypeAnnotationAuthorizationMapper());
		mappers.addFirst(CrudAuthorizationMapperFactory.entityIdAnnotationAuthorizationMapper());
		mappers.addFirst(CrudAuthorizationMapperFactory.secureEntityIdAuthorizationMapper());
	}

	@Override
	public ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> addMapper(final ICrudAuthorizationMapper<AUTHORIZATION_TYPE> mapper) {
		Assert.paramNotNull(mapper, "mapper");
		mappers.addFirst(mapper);
		return this;
	}

	@Override
	public ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> setAuthorizationChecker(
		final IAuthorizationChecker<AUTHORIZATION_TYPE> checker) {
		Assert.paramNotNull(checker, "checker");
		this.authorizationChecker = checker;
		return this;
	}

	@Override
	public IBeanFormPlugin build() {
		return new SecureBeanFormPluginImpl(mappers, authorizationChecker);
	}

}
