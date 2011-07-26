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

package org.jowidgets.security.impl.http.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpRequest;
import org.jowidgets.message.impl.http.client.IHttpRequestInitializer;

public final class BasicAuthenticationInitializer implements IHttpRequestInitializer {

	private static final BasicAuthenticationInitializer INSTANCE = new BasicAuthenticationInitializer();

	private String username;
	private String password;

	private BasicAuthenticationInitializer() {}

	public static BasicAuthenticationInitializer getInstance() {
		return INSTANCE;
	}

	public synchronized void setCredentials(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public synchronized void clearCredentials() {
		username = null;
		password = null;
	}

	@Override
	public void initialize(final HttpRequest httpRequest) {
		final String user;
		final String pwd;
		synchronized (this) {
			user = this.username;
			pwd = this.password;
		}
		if (user != null && pwd != null) {
			final String credentials = user + ":" + pwd;
			final String encodedCredentials = Base64.encodeBase64String(StringUtils.getBytesUtf8(credentials));
			httpRequest.setHeader("Authorization", "Basic " + encodedCredentials);
		}
	}
}
