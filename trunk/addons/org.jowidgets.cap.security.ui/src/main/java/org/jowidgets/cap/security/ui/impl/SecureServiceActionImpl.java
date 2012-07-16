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

import org.jowidgets.api.command.IAction;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.tools.command.ActionWrapper;

final class SecureServiceActionImpl<AUTHORIZATION_TYPE> extends ActionWrapper implements ISecureObject<AUTHORIZATION_TYPE> {

	private final AUTHORIZATION_TYPE authorization;
	private final boolean hasAuthorization;

	private final String authorizationFailed = Messages.getString("SecureServiceActionImpl.authorizationFailed");

	SecureServiceActionImpl(final IAction action, final AUTHORIZATION_TYPE authorization, final boolean hasAuthorization) {
		super(action);
		this.authorization = authorization;
		this.hasAuthorization = hasAuthorization;
	}

	@Override
	public AUTHORIZATION_TYPE getAuthorization() {
		return authorization;
	}

	@Override
	public String getToolTipText() {
		if (hasAuthorization) {
			return super.getToolTipText();
		}
		else {
			return authorizationFailed;
		}
	}

	@Override
	public boolean isEnabled() {
		if (hasAuthorization) {
			return super.isEnabled();
		}
		else {
			return false;
		}
	}

}
