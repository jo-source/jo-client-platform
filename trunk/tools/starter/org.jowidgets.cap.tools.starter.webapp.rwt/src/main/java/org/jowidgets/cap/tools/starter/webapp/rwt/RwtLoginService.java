/*
 * Copyright (c) 2011, grossmann
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

package org.jowidgets.cap.tools.starter.webapp.rwt;

import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.login.ILoginService;
import org.jowidgets.common.image.IImageConstant;

public class RwtLoginService implements ILoginService {

	private final IImageConstant logo;
	private final String loginLabel;
	private final Boolean decoratedLoginDialog;

	public RwtLoginService(final String loginLabel) {
		this(null, loginLabel, null);
	}

	public RwtLoginService(final IImageConstant logo) {
		this(logo, null, null);
	}

	public RwtLoginService(final IImageConstant logo, final boolean decoratedLoginDialog) {
		this(logo, null, Boolean.valueOf(decoratedLoginDialog));
	}

	public RwtLoginService(final String loginLabel, final boolean decoratedLoginDialog) {
		this(null, loginLabel, Boolean.valueOf(decoratedLoginDialog));
	}

	private RwtLoginService(final IImageConstant logo, final String loginLabel, final Boolean decoratedLoginDialog) {
		this.logo = logo;
		this.loginLabel = loginLabel;
		this.decoratedLoginDialog = decoratedLoginDialog;
	}

	@Override
	public boolean doLogin() {
		final ILoginInterceptor loginInterceptor = new RwtLoginInterceptor();
		if (logOn(loginInterceptor)) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean logOn(final ILoginInterceptor loginInterceptor) {
		if (decoratedLoginDialog != null && logo != null) {
			return Toolkit.getLoginPane().login(decoratedLoginDialog.booleanValue(), logo, loginInterceptor).isLoggedOn();
		}
		else if (decoratedLoginDialog != null && loginLabel != null) {
			return Toolkit.getLoginPane().login(decoratedLoginDialog.booleanValue(), loginLabel, loginInterceptor).isLoggedOn();
		}
		else if (decoratedLoginDialog == null && logo != null) {
			return Toolkit.getLoginPane().login(logo, loginInterceptor).isLoggedOn();
		}
		else if (decoratedLoginDialog == null && loginLabel != null) {
			return Toolkit.getLoginPane().login(loginLabel, loginInterceptor).isLoggedOn();
		}
		else {
			return Toolkit.getLoginPane().login((String) null, loginInterceptor).isLoggedOn();
		}
	}
}
