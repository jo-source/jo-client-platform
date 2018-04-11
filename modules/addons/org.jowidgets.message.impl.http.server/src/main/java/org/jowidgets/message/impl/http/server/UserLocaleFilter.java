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

import java.io.IOException;
import java.util.Locale;
import java.util.ServiceLoader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jowidgets.i18n.api.LocaleHolder;
import org.jowidgets.i18n.tools.ThreadLocalLocaleHolder;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.EmptyCheck;

/**
 * {@link Filter} implementation that set's the user locale to the {@link LocaleHolder} for servlet request thread and clears the
 * locale after request terminated.
 * 
 * Be aware to inject the {@link ThreadLocalLocaleHolder} with {@link ServiceLoader} mechanism and to add
 * {@link UserLocaleExecutionInterceptor} to {@link MessageServlet} to make this work correctly.
 * 
 * Also {@link UserLocaleFilter#ACCEPT_LANGUAGE_KEY} header must be set with help of the UserLocaleHttpRequestInitializer on
 * client.
 */
public final class UserLocaleFilter implements Filter {

	public static final String ACCEPT_LANGUAGE_KEY = "Accept-Language";

	private static final ILogger LOGGER = LoggerProvider.get(UserLocaleFilter.class);

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		try {
			try {
				setLocaleFromHeaderToLocaleHolder((HttpServletRequest) request);
			}
			catch (final Throwable e) {
				LOGGER.warn("Exception while setting user locale");
			}
			chain.doFilter(request, response);
		}
		finally {
			LocaleHolder.clearUserLocale();
		}
	}

	private void setLocaleFromHeaderToLocaleHolder(final HttpServletRequest httpRequest) {
		final String acceptLanguage = httpRequest.getHeader(ACCEPT_LANGUAGE_KEY);
		if (!EmptyCheck.isEmpty(acceptLanguage)) {
			final Locale userLocale = getUserLocaleFromAcceptLanguageTag(acceptLanguage);
			if (userLocale != null) {
				LocaleHolder.setUserLocale(userLocale);
				LOGGER.info("Set user locale to: " + userLocale);
			}
		}
		else {
			LOGGER.warn(ACCEPT_LANGUAGE_KEY + " is not set in request header, user locale will not be set");
		}
	}

	private Locale getUserLocaleFromAcceptLanguageTag(final String acceptLanguage) {
		final String language;
		final String country;
		final int indexOfMinus = acceptLanguage.indexOf('-');
		if (indexOfMinus != -1 && acceptLanguage.length() >= indexOfMinus + 1) {
			language = acceptLanguage.substring(0, indexOfMinus);
			country = getCountry(acceptLanguage.substring(indexOfMinus + 1, acceptLanguage.length()));
		}
		else if (indexOfMinus != -1) {
			language = acceptLanguage.substring(0, indexOfMinus);
			country = null;
		}
		else {
			language = acceptLanguage;
			country = null;
		}

		if (country != null) {
			return new Locale(language, country);
		}
		else {
			return new Locale(language);
		}
	}

	private String getCountry(final String country) {
		final int indexOfColon = country.indexOf(',');
		if (indexOfColon != -1) {
			return country.substring(0, indexOfColon);
		}
		else {
			return country;
		}
	}

	@Override
	public void destroy() {}

}
