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

package org.jowidgets.cap.security.common.impl;

import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.plugin.IAuthorizationCheckerDecoratorPlugin;
import org.jowidgets.plugin.api.PluginProvider;

final class LazyAuthorizationChecker implements IAuthorizationChecker<Object> {

	private final IAuthorizationChecker<Object> defaultAuthorizationChecker;

	private IAuthorizationChecker<Object> decoratedAuthorizationChecker;

	@SuppressWarnings("unchecked")
	LazyAuthorizationChecker(final IAuthorizationChecker<?> defaultAuthorizationChecker) {
		this.defaultAuthorizationChecker = (IAuthorizationChecker<Object>) defaultAuthorizationChecker;
	}

	@Override
	public boolean hasAuthorization(final Object authorization) {
		return getDecoratedAuthorizationChecker().hasAuthorization(authorization);
	}

	private IAuthorizationChecker<Object> getDecoratedAuthorizationChecker() {
		if (decoratedAuthorizationChecker == null) {
			decoratedAuthorizationChecker = createDecoratedAuthorizationChecker();
		}
		return decoratedAuthorizationChecker;
	}

	private IAuthorizationChecker<Object> createDecoratedAuthorizationChecker() {
		IAuthorizationChecker<Object> result = defaultAuthorizationChecker;
		for (final IAuthorizationCheckerDecoratorPlugin plugin : PluginProvider.getPlugins(IAuthorizationCheckerDecoratorPlugin.ID)) {
			result = plugin.decorate(result);
			if (result == null) {
				throw new IllegalStateException("IAuthorizationCheckerDecoratorPlugin must not return null");
			}
		}
		return result;
	}

}
