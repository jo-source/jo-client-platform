/*
 * Copyright (c) 2011, H.Westphal, 2018 grossmann
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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.message.api.IMessageReceiver;
import org.jowidgets.message.api.IMessageReceiverBroker;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.util.Assert;
import org.jowidgets.util.DefaultSystemTimeProvider;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.ISystemTimeProvider;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public final class MessageServlet extends HttpServlet implements IMessageReceiverBroker {

	public static final String EXECUTION_INTERCEPTORS_PARAMETER_NAME = "executionInterceptors";
	public static final String MESSAGE_EXECUTIONS_WATCHDOG_LISTENERS_PARAMETER_NAME = "messageExecutionsWatchdogListeners";
	public static final String POLL_INTERVAL_MILLIS_PARAMETER_NAME = "pollIntervalMillis";
	public static final String WATCHDOG_INTERVAL_MILLIS_PARAMETER_NAME = "watchdogIntervalMillis";
	public static final String SESSION_INACTIVITY_MILLIS_PARAMETER_NAME = "sessionInactivityMillis";
	public static final String EXECUTOR_THREAD_COUNT_PARAMETER_NAME = "executorThreadCount";

	private static final long serialVersionUID = 1L;

	private static final ILogger LOGGER = LoggerProvider.get(MessageServlet.class);

	private static final String CONNECTION_ATTRIBUTE_NAME = MessageServlet.class.getName() + "#connection";

	private static final long DEFAULT_POLL_INTERVAL_MILLIS = 1000;
	private static final long DEFAULT_WATCHDOG_INTERVAL_MILLIS = 1000;
	private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 20;

	private final Object brokerId;
	private final ISystemTimeProvider systemTimeProvider;
	private final MessageExecutionsWatchdog watchdog;

	private final CopyOnWriteArraySet<IExecutionInterceptor<Object>> executionInterceptors;

	private long pollInterval;
	private ExecutorService executor;
	private volatile IMessageReceiver receiver;

	public MessageServlet(final Object brokerId) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;

		this.systemTimeProvider = DefaultSystemTimeProvider.getInstance();
		this.executionInterceptors = new CopyOnWriteArraySet<IExecutionInterceptor<Object>>();
		this.watchdog = new MessageExecutionsWatchdog();

		final ScheduledExecutorService watchdogExecutor = Executors.newSingleThreadScheduledExecutor(
				new DaemonThreadFactory("org.jowidgets.message.impl.http.server.MessageServlet.Watchdog"));

		watchdogExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				watchdog.watchExecutions();
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);

		this.executor = Executors.newFixedThreadPool(
				DEFAULT_THREAD_COUNT,
				new DaemonThreadFactory("org.jowidgets.message.impl.http.server.MessageServlet.executor-"));

		this.pollInterval = DEFAULT_POLL_INTERVAL_MILLIS;
	}

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		for (final IExecutionInterceptor<?> executionInterceptor : getExecutionInterceptors(servletConfig)) {
			addExecutionInterceptor(executionInterceptor);
		}

		for (final IMessageExecutionWatchdogListener listener : getExecutionWatchdogListeners(servletConfig)) {
			watchdog.addWatchdogListener(listener);
		}
	}

	private List<IExecutionInterceptor<?>> getExecutionInterceptors(final ServletConfig servletConfig) {
		final List<IExecutionInterceptor<?>> result = new LinkedList<IExecutionInterceptor<?>>();
		final String param = servletConfig.getInitParameter(EXECUTION_INTERCEPTORS_PARAMETER_NAME);
		if (!EmptyCheck.isEmpty(param)) {
			final String[] interceptors = param.split(";");
			for (final String interceptorClassName : interceptors) {
				try {
					result.add((IExecutionInterceptor<?>) Class.forName(interceptorClassName.trim()).newInstance());
				}
				catch (final Exception e) {
					LOGGER.error("Error instantiating IExecutionInterceptor '" + interceptorClassName + "'.", e);
				}
			}
		}
		return result;
	}

	private List<IMessageExecutionWatchdogListener> getExecutionWatchdogListeners(final ServletConfig servletConfig) {
		final List<IMessageExecutionWatchdogListener> result = new LinkedList<IMessageExecutionWatchdogListener>();
		final String param = servletConfig.getInitParameter(MESSAGE_EXECUTIONS_WATCHDOG_LISTENERS_PARAMETER_NAME);
		if (!EmptyCheck.isEmpty(param)) {
			final String[] listeners = param.split(";");
			for (final String listenerClassName : listeners) {
				try {
					result.add((IMessageExecutionWatchdogListener) Class.forName(listenerClassName.trim()).newInstance());
				}
				catch (final Exception e) {
					LOGGER.error("Error instantiating IMessageExecutionWatchdogListener '" + listenerClassName + "'.", e);
				}
			}
		}
		return result;
	}

	@Override
	public Object getBrokerId() {
		return brokerId;
	}

	public void setPollInterval(final long pollInterval) {
		this.pollInterval = pollInterval;
	}

	public void setExecutor(final ExecutorService executor) {
		Assert.paramNotNull(executor, "executor");
		this.executor = executor;
	}

	@SuppressWarnings("unchecked")
	public void addExecutionInterceptor(final IExecutionInterceptor<?> executionInterceptor) {
		Assert.paramNotNull(executionInterceptor, "executionInterceptor");
		executionInterceptors.add((IExecutionInterceptor<Object>) executionInterceptor);
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
				try {
					oos.flush();
					oos.writeObject(msg);
				}
				catch (final IOException e) {
					MessageToolkit.handleExceptions(brokerId, e);
					throw e;
				}
				catch (final RuntimeException e) {
					MessageToolkit.handleExceptions(brokerId, e);
					throw e;
				}
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
			//			final WatchDogResult watchResult = watchdog.getLastWatchResult();
			//			if (watchResult.getPendingExecutions(2000).size() > 0 || watchResult.getRunningExecutions(2000).size() >= 4) {
			//				resp.sendError(503, "To many executions");
			//				return;
			//			}
			conn.onMessage(msg, executionInterceptors);
		}
		catch (final ClassNotFoundException e) {
			MessageToolkit.handleExceptions(brokerId, e);
			throw new ServletException(e);
		}
	}

	private Connection getConnection(final HttpSession session) {
		synchronized (session) {
			Connection conn = (Connection) session.getAttribute(CONNECTION_ATTRIBUTE_NAME);
			if (conn == null) {
				conn = new Connection(receiver, executor, session, watchdog, systemTimeProvider);
				session.setAttribute(CONNECTION_ATTRIBUTE_NAME, conn);
			}
			return conn;
		}
	}

}
