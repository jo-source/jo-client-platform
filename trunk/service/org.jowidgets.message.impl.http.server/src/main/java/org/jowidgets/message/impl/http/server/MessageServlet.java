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
package org.jowidgets.message.impl.http.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.IMessageReceiverBroker;
import org.jowidgets.util.Assert;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public final class MessageServlet extends HttpServlet implements IMessageReceiverBroker {

	private static final long serialVersionUID = 1L;
	private static final String CONNECTION_ATTRIBUTE_NAME = MessageServlet.class.getName() + "#connection";

	private final String brokerId;
	private long pollInterval = 10000;
	private Executor executor = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors() * 10,
			new DaemonThreadFactory());
	private IExecutionInterceptor<?> executionInterceptor = new DefaultExecutionInterceptor();
	private volatile IMessageReceiver receiver;

	public MessageServlet(final String brokerId) {
		super();
		this.brokerId = brokerId;
	}

	@Override
	public Object getBrokerId() {
		return brokerId;
	}

	public void setPollInterval(final long pollInterval) {
		this.pollInterval = pollInterval;
	}

	public void setExecutor(final Executor executor) {
		Assert.paramNotNull(executor, "executor");
		this.executor = executor;
	}

	public void setExecutionInterceptor(final IExecutionInterceptor<?> executionInterceptor) {
		Assert.paramNotNull(executionInterceptor, "executionInterceptor");
		this.executionInterceptor = executionInterceptor;
	}

	@Override
	public void setReceiver(final IMessageReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());

		final HttpSession session = req.getSession();
		if (session.isNew()) {
			// return immediately to send new session id to client
			oos.writeInt(0);

		}
		else {
			final Connection conn = getConnection(session);
			final List<Object> messages = conn.pollMessages(pollInterval);
			oos.writeInt(messages.size());
			for (final Object msg : messages) {
				oos.flush();
				oos.writeObject(msg);
			}
		}

		oos.flush();
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final HttpSession session = req.getSession(false);
		if (session == null) {
			throw new ServletException("invalid session");
		}
		final Connection conn = getConnection(session);
		try {
			final Object msg = new ObjectInputStream(req.getInputStream()).readObject();
			conn.onMessage(msg, executionInterceptor);
		}
		catch (final ClassNotFoundException e) {
			throw new ServletException(e);
		}
	}

	private Connection getConnection(final HttpSession session) {
		synchronized (session) {
			Connection conn = (Connection) session.getAttribute(CONNECTION_ATTRIBUTE_NAME);
			if (conn == null) {
				conn = new Connection(receiver, executor);
				session.setAttribute(CONNECTION_ATTRIBUTE_NAME, conn);
			}
			return conn;
		}
	}

}
