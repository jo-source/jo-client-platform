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

package org.jowidgets.cap.tools.starter.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.message.impl.http.server.MessageServlet;
import org.jowidgets.security.impl.http.server.BasicAuthenticationFilter;
import org.jowidgets.security.impl.http.server.SecurityRemotingServlet;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.StringUtils;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

public final class CapServerStarter {

	private static final ILogger LOGGER = LoggerProvider.get(CapServerStarter.class);
	private static final MessageServletConfig DEFAULT_CONFIG = new MessageServletConfig();
	private static final int DEFAULT_PORT = 8080;

	private CapServerStarter() {}

	public static void startServer() throws Exception {
		startServer(DEFAULT_CONFIG);
	}

	public static void startServer(final int port) throws Exception {
		startServer(port, DEFAULT_CONFIG);
	}

	public static void startServer(final String brokerId) throws Exception {
		startServer(brokerId, DEFAULT_CONFIG);
	}

	public static void startServer(final String brokerId, final int port) throws Exception {
		startServer(brokerId, port, DEFAULT_CONFIG);
	}

	public static void startServer(final MessageServletConfig config) throws Exception {
		startServer(RemotingBrokerId.DEFAULT_BROKER_ID, DEFAULT_PORT, config);
	}

	public static void startServer(final int port, final MessageServletConfig config) throws Exception {
		startServer(RemotingBrokerId.DEFAULT_BROKER_ID, port, config);
	}

	public static void startServer(final String brokerId, final MessageServletConfig config) throws Exception {
		startServer(brokerId, DEFAULT_PORT, config);
	}

	public static void startServer(final String brokerId, final int port, final MessageServletConfig config) throws Exception {
		Assert.paramNotNull(brokerId, "brokerId");
		final Server server = new Server(port);

		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
			32,
			256,
			60,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(),
			DaemonThreadFactory.multi(CapServerStarter.class.getName() + ".ServletExecutor"));
		server.setThreadPool(new ExecutorThreadPool(threadPoolExecutor));

		final ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);

		final ServletHolder servletHolder = new ServletHolder(new SecurityRemotingServlet(brokerId));
		setConfigParameters(servletHolder, config);
		root.addServlet(servletHolder, "/");
		root.addFilter(new FilterHolder(new BasicAuthenticationFilter()), "/", FilterMapping.DEFAULT);
		addServletFilters(root, config);

		server.setHandler(root);
		server.start();
		server.join();
	}

	public static void addServletFilters(final ServletContextHandler root, final MessageServletConfig config) {
		for (final Class<Filter> filter : config.getServletFilters()) {
			try {
				root.addFilter(new FilterHolder(filter.newInstance()), "/", FilterMapping.DEFAULT);
			}
			catch (final Throwable t) {
				LOGGER.warn("Exception creating servlet filter " + filter + ".", t);
			}
		}
	}

	private static void setConfigParameters(final Holder<?> holder, final MessageServletConfig config) {
		final String executionInterceptors = StringUtils.concat(";", getClassNames(config.getExecutionInterceptors()));
		if (!EmptyCheck.isEmpty(executionInterceptors)) {
			holder.setInitParameter(MessageServlet.EXECUTION_INTERCEPTORS_PARAMETER_NAME, executionInterceptors);
		}

		final String watchdogListeners = StringUtils.concat(";", getClassNames(config.getMessageExecutionWatchdogListeners()));
		if (!EmptyCheck.isEmpty(watchdogListeners)) {
			holder.setInitParameter(MessageServlet.MESSAGE_EXECUTIONS_WATCHDOG_LISTENERS_PARAMETER_NAME, watchdogListeners);
		}

		final Long pollIntervalMillis = config.getPollIntervalMillis();
		if (pollIntervalMillis != null) {
			holder.setInitParameter(MessageServlet.POLL_INTERVAL_MILLIS_PARAMETER_NAME, pollIntervalMillis.toString());
		}

		final Long watchdogIntervalMillis = config.getWatchdogIntervalMillis();
		if (watchdogIntervalMillis != null) {
			holder.setInitParameter(MessageServlet.WATCHDOG_INTERVAL_MILLIS_PARAMETER_NAME, watchdogIntervalMillis.toString());
		}

		final Long sessionInactivityMillis = config.getSessionInactivityMillis();
		if (sessionInactivityMillis != null) {
			holder.setInitParameter(MessageServlet.SESSION_INACTIVITY_MILLIS_PARAMETER_NAME, sessionInactivityMillis.toString());
		}

		final Long denyRequestPendingTimeoutMillis = config.getDenyRequestPendingtimeoutMillis();
		if (denyRequestPendingTimeoutMillis != null) {
			holder.setInitParameter(
					MessageServlet.DENY_REQUEST_PENDING_TIMEOUT_MILLIS_PARAMETER_NAME,
					denyRequestPendingTimeoutMillis.toString());
		}

		final Long haraKiriTimeoutMillis = config.getHaraKiriTimeoutMillis();
		if (haraKiriTimeoutMillis != null) {
			holder.setInitParameter(MessageServlet.HARA_KIRI_TIMEOUT_MILLIS_PARAMETER_NAME, haraKiriTimeoutMillis.toString());
		}

		final Long haraKiriPendingThreshold = config.getHaraKiriPendingThreashold();
		if (haraKiriPendingThreshold != null) {
			holder.setInitParameter(
					MessageServlet.HARA_KIRI_PENDING_THRESHOLD_PARAMETER_NAME,
					haraKiriPendingThreshold.toString());
		}

		final Long executorThreadCount = config.getExecutorThreadCount();
		if (executorThreadCount != null) {
			holder.setInitParameter(MessageServlet.EXECUTOR_THREAD_COUNT_PARAMETER_NAME, executorThreadCount.toString());
		}

	}

	private static List<String> getClassNames(final List<? extends Class<?>> classes) {
		final List<String> result = new ArrayList<String>(classes.size());
		for (final Class<?> clazz : classes) {
			result.add(clazz.getName());
		}
		return result;

	}

}
