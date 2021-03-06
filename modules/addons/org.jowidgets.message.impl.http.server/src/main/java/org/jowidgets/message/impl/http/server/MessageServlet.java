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
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.ObjectName;
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

/**
 * Messaging servlet that provides a MessageBroker with help of a servlet.
 * 
 * Example configuration:
 * 
 * <servlet>
 * <servlet-name>remoting</servlet-name>
 * <servlet-class>org.jowidgets.security.impl.http.server.SecurityRemotingServlet</servlet-class>
 * 
 * <init-param>
 * <param-name>messageServletMBeanObjectName</param-name>
 * <param-value>
 * myorg.mypackage.myapplication.MessageServlet</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>messageExecutionsWatchdogListeners</param-name>
 * <param-value>
 * org.jowidgets.message.impl.http.server.LoggingWatchdogListener;de.maycompany.MyWatchdogListener</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>executionInterceptors</param-name>
 * <param-value>org.jowidgets.message.impl.http.server.UserLocaleExecutionInterceptor</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>executorThreadCount</param-name>
 * <param-value>80</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>pollIntervalMillis</param-name>
 * <param-value>10000</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>watchdogIntervalMillis</param-name>
 * <param-value>1000</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>sessionInactivityMillis</param-name>
 * <param-value>60000</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>denyRequestPendingTimeoutMillis</param-name>
 * <param-value>20000</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>haraKiriTimeoutMillis</param-name>
 * <param-value>1800000</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>haraKiriPendingThreshold</param-name>
 * <param-value>10000</param-value>
 * </init-param>
 * 
 * </servlet>
 *
 */
public final class MessageServlet extends HttpServlet implements IMessageReceiverBroker, IMessageServletMXBean {

	public static final String EXECUTION_INTERCEPTORS_PARAMETER_NAME = "executionInterceptors";
	public static final String MESSAGE_EXECUTIONS_WATCHDOG_LISTENERS_PARAMETER_NAME = "messageExecutionsWatchdogListeners";
	public static final String POLL_INTERVAL_MILLIS_PARAMETER_NAME = "pollIntervalMillis";
	public static final String WATCHDOG_INTERVAL_MILLIS_PARAMETER_NAME = "watchdogIntervalMillis";
	public static final String SESSION_INACTIVITY_MILLIS_PARAMETER_NAME = "sessionInactivityMillis";
	public static final String DENY_REQUEST_PENDING_TIMEOUT_MILLIS_PARAMETER_NAME = "denyRequestPendingTimeoutMillis";
	public static final String HARA_KIRI_TIMEOUT_MILLIS_PARAMETER_NAME = "haraKiriTimeoutMillis";
	public static final String HARA_KIRI_PENDING_THRESHOLD_PARAMETER_NAME = "haraKiriPendingThreshold";
	public static final String EXECUTOR_THREAD_COUNT_PARAMETER_NAME = "executorThreadCount";
	public static final String MESSAGE_SERVLET_MBEAN_OBJECT_NAME_PARAMETER_NAME = "messageServletMBeanObjectName";

	static final String MESSAGE_CHANNEL_ATTRIBUTE_NAME = MessageServlet.class.getName() + "#channel";

	private static final long DEFAULT_POLL_INTERVAL_MILLIS = 10000; // wait up to 10 seconds on get() for messages to receive
	private static final long DEFAULT_WATCHDOG_INTERVAL_MILLIS = 1000; // watch executions every 1 second
	private static final long DEFAULT_SESSION_INACTIVITY_TIMEOUT = 60 * 1000; // cancel session if client is more than 1 minute inactive
	private static final long DEFAULT_DENY_REQUEST_PENDING_TIMEOUT_MILLIS = 20 * 1000; // deny new requests if system is 20 seconds inactive
	private static final long DEFAULT_HARA_KIRI_TIMEOUT = 1000 * 60 * 30; //do not allow more than 30 minutes system inactivity
	private static final long DEFAULT_HARA_KIRI_PENDING_THRESHHOLD = 10000; // do not allow more than 10000 pending messages
	private static final long DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors() * 10; // use up to 10 thread's for each core

	private static final long serialVersionUID = 1L;
	private static final String CLASS_NAME = MessageServlet.class.getName();
	private static final ILogger LOGGER = LoggerProvider.get(MessageServlet.class);

	private final Object brokerId;
	private final ISystemTimeProvider systemTimeProvider;

	private final MessageExecutionsWatchdog watchdog;
	private final ScheduledExecutorService watchdogExecutor;
	private final CopyOnWriteArraySet<IExecutionInterceptor<Object>> executionInterceptors;

	private final AtomicBoolean initialize;
	private final AtomicBoolean initialized;

	private long pollIntervalMillis;
	private long watchDogIntervalMillis;
	private ScheduledFuture<?> watchDogExecution;
	private long denyRequestPendingTimeoutMillis;
	private long sessionInactivityTimeoutMillis;
	private int threadCount;
	private ExecutorService executor;
	private volatile IMessageReceiver receiver;

	public MessageServlet(final Object brokerId) {
		this(brokerId, DefaultSystemTimeProvider.getInstance());
	}

	MessageServlet(final Object brokerId, final ISystemTimeProvider systemTimeProvider) {
		Assert.paramNotNull(brokerId, "brokerId");
		this.brokerId = brokerId;

		this.systemTimeProvider = DefaultSystemTimeProvider.getInstance();
		this.executionInterceptors = new CopyOnWriteArraySet<IExecutionInterceptor<Object>>();
		this.watchdog = new MessageExecutionsWatchdog(DEFAULT_SESSION_INACTIVITY_TIMEOUT);
		this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.create(CLASS_NAME + ".Watchdog"));

		this.initialize = new AtomicBoolean(false);
		this.initialized = new AtomicBoolean(false);

		this.executor = (ExecutorService) Proxy.newProxyInstance(
				Executors.class.getClassLoader(),
				new Class[] {ExecutorService.class},
				new DummyExecutorServiceInvocationHandler());

		this.pollIntervalMillis = DEFAULT_POLL_INTERVAL_MILLIS;
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		if (initialize.compareAndSet(false, true)) {
			super.init(config);
			initialize(config);
			initialized.set(true);
		}
		else {
			LOGGER.warn("Servlet must not be initialized more than once");
		}
	}

	private void initialize(final ServletConfig config) throws ServletException {
		this.pollIntervalMillis = getLongFromConfig(config, POLL_INTERVAL_MILLIS_PARAMETER_NAME, DEFAULT_POLL_INTERVAL_MILLIS);
		LOGGER.info("Set poll interval to: " + pollIntervalMillis);

		this.denyRequestPendingTimeoutMillis = getLongFromConfig(
				config,
				DENY_REQUEST_PENDING_TIMEOUT_MILLIS_PARAMETER_NAME,
				DEFAULT_DENY_REQUEST_PENDING_TIMEOUT_MILLIS);
		LOGGER.info("Set deny request pending timeout millis to: " + denyRequestPendingTimeoutMillis);

		initializeExecutor(config);
		initializeWatchdog(config);
		initializeMBean(config);
	}

	private void initializeExecutor(final ServletConfig config) {
		this.threadCount = (int) getLongFromConfig(config, EXECUTOR_THREAD_COUNT_PARAMETER_NAME, DEFAULT_THREAD_COUNT);
		watchdog.setThreadCount(threadCount);

		this.executor = Executors.newFixedThreadPool(threadCount, DaemonThreadFactory.multi(CLASS_NAME + ".Executor"));
		LOGGER.info("Set executor thread count to: " + threadCount);

		for (final IExecutionInterceptor<?> executionInterceptor : getExecutionInterceptors(config)) {
			addExecutionInterceptor(executionInterceptor);
			LOGGER.info("Add execution interceptor: " + executionInterceptor.getClass().getName());
		}
	}

	private void initializeWatchdog(final ServletConfig config) {
		sessionInactivityTimeoutMillis = getLongFromConfig(
				config,
				SESSION_INACTIVITY_MILLIS_PARAMETER_NAME,
				DEFAULT_SESSION_INACTIVITY_TIMEOUT);
		final long sessionInactivityTimeout = getLongFromConfig(
				config,
				SESSION_INACTIVITY_MILLIS_PARAMETER_NAME,
				DEFAULT_SESSION_INACTIVITY_TIMEOUT);
		watchdog.setSessionInactivityTimeout(sessionInactivityTimeout);
		LOGGER.info("Set session inactivity timeout to: " + sessionInactivityTimeout);

		final long haraKiriTimeout = getLongFromConfig(
				config,
				HARA_KIRI_TIMEOUT_MILLIS_PARAMETER_NAME,
				DEFAULT_HARA_KIRI_TIMEOUT);
		watchdog.setHaraKiriTimeout(haraKiriTimeout);
		LOGGER.info("Set hara-kiri timeout to: " + haraKiriTimeout);

		final long haraKiriPendingThreshold = getLongFromConfig(
				config,
				HARA_KIRI_PENDING_THRESHOLD_PARAMETER_NAME,
				DEFAULT_HARA_KIRI_PENDING_THRESHHOLD);
		watchdog.setHaraKiriPendingThreshold(haraKiriPendingThreshold);
		LOGGER.info("Set hara-kiri pending threshold to: " + haraKiriPendingThreshold);

		watchDogIntervalMillis = getLongFromConfig(
				config,
				WATCHDOG_INTERVAL_MILLIS_PARAMETER_NAME,
				DEFAULT_WATCHDOG_INTERVAL_MILLIS);
		setWatchDogIntervalMillis(
				getLongFromConfig(config, WATCHDOG_INTERVAL_MILLIS_PARAMETER_NAME, DEFAULT_WATCHDOG_INTERVAL_MILLIS));
		LOGGER.info("Set watchdog interval to: " + watchDogIntervalMillis);

		for (final IMessageExecutionWatchdogListener listener : createExecutionWatchdogListeners(config)) {
			watchdog.addWatchdogListener(listener);
			LOGGER.info("Add watchdog listener: " + listener.getClass().getName());
		}
	}

	private void initializeMBean(final ServletConfig config) {
		final String mBeanName = config.getInitParameter(MESSAGE_SERVLET_MBEAN_OBJECT_NAME_PARAMETER_NAME);
		if (EmptyCheck.isEmpty(mBeanName)) {
			LOGGER.info("No mbean name defined, jmx disabled for MessageServlet");
			return;
		}
		try {
			ManagementFactory.getPlatformMBeanServer().registerMBean(
					this,
					new ObjectName(mBeanName + ":type=" + MessageServlet.class.getSimpleName()));
			LOGGER.info("Registered MessageServlet to MBeanServer with name: " + mBeanName);
		}
		catch (final Exception e) {
			LOGGER.error("Error register '" + MessageServlet.class + "' to mbean server.", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> createInstancesFromConfig(final ServletConfig config, final String parameterName) {
		final List<T> result = new LinkedList<T>();
		final String param = config.getInitParameter(parameterName);
		if (!EmptyCheck.isEmpty(param)) {
			final String[] classNames = param.split(";");
			for (final String className : classNames) {
				try {
					result.add((T) Class.forName(className.trim()).newInstance());
				}
				catch (final Exception e) {
					LOGGER.error("Error instantiating '" + className + "'.", e);
				}
			}
		}
		return result;
	}

	private long getLongFromConfig(final ServletConfig config, final String parameterName, final long defaultValue) {
		final String param = config.getInitParameter(parameterName);
		if (!EmptyCheck.isEmpty(param)) {
			try {
				return Long.parseLong(param);
			}
			catch (final Exception e) {
				LOGGER.error("Error parsing parameter '" + parameterName + "' with value '" + param + "' to a long.", e);
			}
		}
		return defaultValue;
	}

	private List<IExecutionInterceptor<?>> getExecutionInterceptors(final ServletConfig servletConfig) {
		return createInstancesFromConfig(servletConfig, EXECUTION_INTERCEPTORS_PARAMETER_NAME);
	}

	private List<IMessageExecutionWatchdogListener> createExecutionWatchdogListeners(final ServletConfig servletConfig) {
		return createInstancesFromConfig(servletConfig, MESSAGE_EXECUTIONS_WATCHDOG_LISTENERS_PARAMETER_NAME);
	}

	@Override
	public int getThreadCount() {
		return threadCount;
	}

	@Override
	public long getPollIntervalMillis() {
		return pollIntervalMillis;
	}

	@Override
	public long getWatchDogIntervalMillis() {
		return watchDogIntervalMillis;
	}

	@Override
	public synchronized void setWatchDogIntervalMillis(final long interval) {
		if (interval <= 0) {
			throw new IllegalArgumentException("Must be positive");
		}
		this.watchDogIntervalMillis = interval;
		if (watchDogExecution != null) {
			watchDogExecution.cancel(false);
		}
		watchDogExecution = watchdogExecutor.scheduleAtFixedRate(
				new WatchDogRunner(),
				0,
				watchDogIntervalMillis,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public long getSessionInactivityTimeoutMillis() {
		return sessionInactivityTimeoutMillis;
	}

	@Override
	public void setSessionInactivityTimeoutMillis(final long timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException("Must not be negative");
		}
		this.sessionInactivityTimeoutMillis = timeout;
		watchdog.setSessionInactivityTimeout(timeout);
	}

	@Override
	public long getDenyRequestPendingTimeoutMillis() {
		return denyRequestPendingTimeoutMillis;
	}

	@Override
	public void setDenyRequestPendingTimeoutMillis(final long timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException("Must not be negative");
		}
		this.denyRequestPendingTimeoutMillis = timeout;
	}

	@Override
	public Long getHaraKiriTimeoutMillis() {
		return watchdog.getHaraKiriTimeout();
	}

	@Override
	public void setHaraKiriTimeoutMillis(final Long timeout) {
		if (timeout != null && timeout.longValue() < 0) {
			throw new IllegalArgumentException("Must not be negative");
		}
		watchdog.setHaraKiriTimeout(timeout);
	}

	@Override
	public Long getHaraKiriPendingThreshold() {
		return watchdog.getHaraKiriPendingThreshold();
	}

	@Override
	public void setHaraKiriPendingThreshold(final Long threshold) {
		if (threshold != null && threshold.longValue() <= 0) {
			throw new IllegalArgumentException("Must be positive");
		}
		watchdog.setHaraKiriPendingThreshold(threshold);
	}

	@Override
	public void disableHaraKiri() {
		setHaraKiriPendingThreshold(null);
		setHaraKiriTimeoutMillis(null);
	}

	@Override
	public void cancelAllExecutions() {
		watchdog.cancelAllExecutions();
	}

	@Override
	public void cancelMaxRuntimeExecution() {
		final MessageExecution execution = watchdog.getLastWatchEvent().getMaxRuntimeExecution(true);
		if (execution != null) {
			execution.cancel();
		}
	}

	@Override
	public Object getBrokerId() {
		return brokerId;
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

	private boolean checkInitialized(final HttpServletResponse response) throws IOException {
		if (initialized.get()) {
			return true;
		}
		else {
			response.sendError(500, "Servlet not initialized");
			return false;
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		if (!checkInitialized(response)) {
			return;
		}

		final ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());

		final HttpSession session = request.getSession();
		if (session.isNew()) {
			LOGGER.debug("Intialial session created for: " + request.getRemoteAddr());
			session.setMaxInactiveInterval((int) (sessionInactivityTimeoutMillis / 1000));
			// return immediately to send new session id to client
			oos.writeInt(0);
		}
		else {
			final MessageChannel channel = getOrCreateMessageChannel(session);
			final List<Object> messages = channel.pollMessages(pollIntervalMillis);
			oos.writeInt(messages.size());
			for (final Object message : messages) {
				try {
					LOGGER.debug("Write message to response stream: " + message);
					oos.flush();
					oos.writeObject(message);
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
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		if (!checkInitialized(response)) {
			return;
		}

		final HttpSession session = getHttpSession(request);
		if (session == null) {
			throw new ServletException("Invalid session");
		}
		final MessageChannel conn = getOrCreateMessageChannel(session);
		try {
			final WatchDogEvent watchEvent = watchdog.getLastWatchEvent();
			if (watchEvent.getPendingExecutions(denyRequestPendingTimeoutMillis).size() > 0) {
				LOGGER.debug("Reject message, to many pending messages");
				watchdog.cancelExecutionsOfSession(session);
				response.sendError(503, "Message rejected, to many pending messages.");
				return;
			}
			conn.onMessage(new ObjectInputStream(request.getInputStream()).readObject(), executionInterceptors);
		}
		catch (final ClassNotFoundException e) {
			MessageToolkit.handleExceptions(brokerId, e);
			throw new ServletException(e);
		}
	}

	private HttpSession getHttpSession(final HttpServletRequest request) {
		if (request.isRequestedSessionIdValid()) {
			try {
				return request.getSession(false);
			}
			catch (final Exception e) {
				LOGGER.error("Exception on get session", e);
			}
		}
		return null;
	}

	private MessageChannel getOrCreateMessageChannel(final HttpSession session) {
		synchronized (session) {
			MessageChannel channel = (MessageChannel) session.getAttribute(MESSAGE_CHANNEL_ATTRIBUTE_NAME);
			if (channel == null) {
				LOGGER.debug("Create a new channel for session, max inactive interval: " + session.getMaxInactiveInterval());
				channel = new MessageChannel(receiver, executor, session, watchdog, systemTimeProvider);
				session.setAttribute(MESSAGE_CHANNEL_ATTRIBUTE_NAME, channel);
			}
			return channel;
		}
	}

	private final class WatchDogRunner implements Runnable {
		@Override
		public void run() {
			try {
				watchdog.watchExecutions();
			}
			catch (final Exception e) {
				LOGGER.error("Exception on watch executions", e);
			}
		}
	}

	private final class DummyExecutorServiceInvocationHandler implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			throw new UnsupportedOperationException("Executor is not initialized");
		}
	}

}
