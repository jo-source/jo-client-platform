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
import org.jowidgets.api.model.item.IActionItemVisibilityAspect;
import org.jowidgets.api.model.item.IActionItemVisibilityAspectPlugin;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.util.priority.IPriorityValue;
import org.jowidgets.util.priority.LowHighPriority;
import org.jowidgets.util.priority.PriorityValue;
import org.jowidgets.util.wrapper.WrapperUtil;

final class SecureActionItemVisibilityAspectPluginImpl<AUTHORIZATION_TYPE> implements IActionItemVisibilityAspectPlugin {

	private static final IPriorityValue<Boolean, LowHighPriority> NOT_VISIBLE_HIGH = new PriorityValue<Boolean, LowHighPriority>(
		Boolean.FALSE,
		LowHighPriority.HIGH);

	private final int order;
	private final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;
	private final IActionItemVisibilityAspect actionItemVisibilityAspect;

	SecureActionItemVisibilityAspectPluginImpl(
		final int order,
		final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker) {
		this.order = order;
		this.authorizationChecker = authorizationChecker;
		this.actionItemVisibilityAspect = new SecureActionItemVisibilityAspect();
	}

	@Override
	public IActionItemVisibilityAspect getVisibilityAspect() {
		return actionItemVisibilityAspect;
	}

	@Override
	public int getOrder() {
		return order;
	}

	private final class SecureActionItemVisibilityAspect implements IActionItemVisibilityAspect {

		@SuppressWarnings("unchecked")
		@Override
		public IPriorityValue<Boolean, LowHighPriority> getVisibility(final IAction action) {
			final ISecureObject<AUTHORIZATION_TYPE> secureObject = WrapperUtil.tryToCast(action, ISecureObject.class);
			if (secureObject != null) {
				if (!authorizationChecker.hasAuthorization(secureObject.getAuthorization())) {
					return NOT_VISIBLE_HIGH;
				}
			}
			return null;
		}

	}

}
