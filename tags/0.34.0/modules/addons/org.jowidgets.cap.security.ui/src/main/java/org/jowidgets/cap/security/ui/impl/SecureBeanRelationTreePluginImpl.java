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

import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.cap.ui.api.plugin.IBeanRelationTreePlugin;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.util.IFilter;

final class SecureBeanRelationTreePluginImpl<AUTHORIZATION_TYPE> implements IBeanRelationTreePlugin<Object> {

	private final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureBeanRelationTreePluginImpl(final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker) {
		this.authorizationChecker = authorizationChecker;
	}

	@Override
	public void modifySetup(final IPluginProperties properties, final IBeanRelationTreeBluePrint<Object> builder) {
		builder.addChildRelationFilter(new SecureBeanRelationNodeModelFilter());
	}

	private final class SecureBeanRelationNodeModelFilter implements IFilter<IBeanRelationNodeModel<Object, Object>> {

		@SuppressWarnings("unchecked")
		@Override
		public boolean accept(final IBeanRelationNodeModel<Object, Object> childRelationModel) {
			final IReaderService<Object> readerService = childRelationModel.getReaderService();
			if (readerService instanceof ISecureObject<?>) {
				return authorizationChecker.hasAuthorization(((ISecureObject<AUTHORIZATION_TYPE>) readerService).getAuthorization());
			}
			else {
				return true;
			}
		}

	}

}
