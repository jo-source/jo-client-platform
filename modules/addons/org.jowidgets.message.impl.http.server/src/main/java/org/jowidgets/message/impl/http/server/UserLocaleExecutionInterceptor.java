/*
 * Copyright (c) 2018, grossmann
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

package org.jowidgets.message.impl.http.server;

import java.util.Locale;
import java.util.ServiceLoader;

import org.jowidgets.i18n.api.LocaleHolder;
import org.jowidgets.i18n.tools.ThreadLocalLocaleHolder;

/**
 * {@link IExecutionInterceptor} implementation that allows to set the user locale to the execution thread and clears is after the
 * execution.
 * 
 * Be aware to inject the {@link ThreadLocalLocaleHolder} with {@link ServiceLoader} mechanism and to add
 * {@link UserLocaleFilter} to {@link MessageServlet} to make this work correctly. Also
 * {@link UserLocaleFilter#ACCEPT_LANGUAGE_KEY} header must be set with help of the UserLocaleHttpRequestInitializer on client.
 */
public final class UserLocaleExecutionInterceptor implements IExecutionInterceptor<Locale> {

	@Override
	public Locale getExecutionContext() {
		return LocaleHolder.getUserLocale();
	}

	@Override
	public void beforeExecution(final Locale userLocale) {
		LocaleHolder.setUserLocale(userLocale);
	}

	@Override
	public void afterExecution() {
		LocaleHolder.clearUserLocale();
	}

}