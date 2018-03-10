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

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.cap.remoting.server.CapServerServicePublisher;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.message.impl.http.server.MessageServlet;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

public final class SecurityRemotingServlet extends GenericServlet {

	public static final String BROKER_ID_PARAMETER_NAME = "brokerId";
	public static final String EXECUTION_INTERCEPTORS_PARAMETER_NAME = MessageServlet.EXECUTION_INTERCEPTORS_PARAMETER_NAME;

	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig;
	private MessageServlet messageServlet;

	private String brokerId;

	public SecurityRemotingServlet() {
		this(RemotingBrokerId.DEFAULT_BROKER_ID);
	}

	public SecurityRemotingServlet(final String brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;
	}

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		this.servletConfig = servletConfig;
		final String brokerIdParameter = servletConfig.getInitParameter(BROKER_ID_PARAMETER_NAME);
		if (!EmptyCheck.isEmpty(brokerIdParameter)) {
			brokerId = brokerIdParameter;
		}
	}

	@Override
	public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
		getMessageServlet().service(req, res);
	}

	private MessageServlet getMessageServlet() throws ServletException {
		if (messageServlet == null) {
			createMessageServlet();
		}
		return messageServlet;
	}

	private synchronized MessageServlet createMessageServlet() throws ServletException {
		if (messageServlet == null) {
			messageServlet = new MessageServlet(brokerId);
			messageServlet.init(servletConfig);
			messageServlet.addExecutionInterceptor(new SecurityExecutionInterceptor());
			messageServlet.addExecutionInterceptor(new CurrentRequestExecutionInterceptor());
			MessageToolkit.addReceiverBroker(messageServlet);
			new CapServerServicePublisher(brokerId).publishServices();
		}
		return messageServlet;
	}

}
