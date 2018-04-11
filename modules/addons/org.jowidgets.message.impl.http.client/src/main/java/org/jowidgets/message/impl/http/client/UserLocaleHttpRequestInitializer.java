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

package org.jowidgets.message.impl.http.client;

import java.util.Locale;

import org.apache.http.HttpRequest;
import org.jowidgets.i18n.api.LocaleHolder;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;

/**
 * {@link IHttpRequestInitializer} implementation that sets the #ACCEPT_LANGUAGE_KEY provided by the
 * {@link LocaleHolder#getUserLocale()} for each httpRequest.
 */
public final class UserLocaleHttpRequestInitializer implements IHttpRequestInitializer {

	public static final String ACCEPT_LANGUAGE_KEY = "Accept-Language";

	private static final ILogger LOGGER = LoggerProvider.get(UserLocaleHttpRequestInitializer.class);
	private static final IHttpRequestInitializer INSTANCE = new UserLocaleHttpRequestInitializer();

	private UserLocaleHttpRequestInitializer() {}

	public static IHttpRequestInitializer getInstance() {
		return INSTANCE;
	}

	@Override
	public void initialize(final HttpRequest httpRequest) {
		httpRequest.setHeader(ACCEPT_LANGUAGE_KEY, getAcceptLanguageTag());
	}

	private String getAcceptLanguageTag() {
		final Locale userLocale = getUserLocale();
		return userLocale.getLanguage() + "-" + userLocale.getCountry();
	}

	private Locale getUserLocale() {
		final Locale userLocale = LocaleHolder.getUserLocale();
		if (userLocale != null) {
			return userLocale;
		}
		else {
			LOGGER.warn("LocaleHolder has no user locale set, default locale will be used.");
			return Locale.getDefault();
		}

	}

}
