/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.security.impl.http.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.jowidgets.security.api.AuthenticationService;
import org.jowidgets.security.api.AuthorizationService;
import org.jowidgets.security.api.SecurityContextHolder;
import org.jowidgets.security.tools.DefaultCredentials;

public final class BasicAuthenticationFilter implements Filter {

	private static final String PRINCIPAL_KEY = BasicAuthenticationFilter.class.getName() + ".principal";

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
			ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			final Object principal = session.getAttribute(PRINCIPAL_KEY);
			if (principal != null) {
				SecurityContextHolder.setSecurityContext(principal);
			}
		}

		if (SecurityContextHolder.getSecurityContext() == null) {
			String credentials = httpRequest.getHeader("Authorization");
			if (credentials != null) {
				credentials = credentials.substring(credentials.indexOf(' ') + 1);
				credentials = new String(Base64.decodeBase64(credentials), "UTF-8");
				final int i = credentials.indexOf(':');
				final String username = credentials.substring(0, i);
				final String password;
				if (credentials.length() > i) {
					password = credentials.substring(i + 1);
				}
				else {
					password = "";
				}
				Object principal = AuthenticationService.authenticate(new DefaultCredentials(username, password));
				if (principal != null) {
					principal = AuthorizationService.authorize(principal);
					if (principal != null) {
						SecurityContextHolder.setSecurityContext(principal);
					}
				}
			}
		}

		try {
			chain.doFilter(request, response);
		}
		finally {
			session = httpRequest.getSession(false);
			if (session != null) {
				final Object principal = SecurityContextHolder.getSecurityContext();
				if (principal != null) {
					session.setAttribute(PRINCIPAL_KEY, principal);
				}
			}
			SecurityContextHolder.clearSecurityContext();
		}
	}

	@Override
	public void destroy() {}

}
