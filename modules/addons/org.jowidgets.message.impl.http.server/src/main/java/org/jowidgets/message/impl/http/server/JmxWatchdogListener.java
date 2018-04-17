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

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.Assert;

/**
 * A {@link IMessageExecutionWatchdogListener} implementation that makes a {@link IMessageServletStatusMXBean} object available
 * over JMX.
 * 
 * The listener was designed to be subclassed to create a listener with default constructor but custom jmx object name or custom
 * mbean.
 */
public class JmxWatchdogListener implements IMessageExecutionWatchdogListener {

	private static final ILogger LOGGER = LoggerProvider.get(JmxWatchdogListener.class);

	private final MessageServletStatus status;

	/**
	 * Creates a new instance an registers a {@link IMessageServletStatusMXBean} with the object name
	 * 'org.jowidgets.message.impl.http.server.MessageServletStatus' to the platform mbean server.
	 */
	public JmxWatchdogListener() {
		this("org.jowidgets.message.impl.http.server.MessageServletStatus");
	}

	/**
	 * Creates a new instance an registers a {@link IMessageServletStatusMXBean} with the given object name to the platform mbean
	 * server.
	 * 
	 * @param objectName The object name to register the {@link IMessageServletStatusMXBean}
	 */
	public JmxWatchdogListener(final String objectName) {
		this(createAndRegisterMessageServletStatus(objectName));
	}

	/**
	 * Creates a new instance for a given {@link MessageServletStatus} instance. The invoker is responsible to register the given
	 * mbean for itself.
	 * 
	 * @param messageServletStatus The mbean to use, must not be null
	 */
	public JmxWatchdogListener(final MessageServletStatus messageServletStatus) {
		Assert.paramNotNull(messageServletStatus, "messageServletStatus");
		this.status = messageServletStatus;
	}

	private static MessageServletStatus createAndRegisterMessageServletStatus(final String moduleName) {
		Assert.paramNotNull(moduleName, "moduleName");
		final MessageServletStatus result = new MessageServletStatus();
		try {
			ManagementFactory.getPlatformMBeanServer().registerMBean(
					result,
					new ObjectName(moduleName + ":type=MessageServletStatus"));
		}
		catch (final Exception e) {
			LOGGER.error("Error register '" + MessageServletStatus.class + "' to mbean server.", e);
		}
		return result;
	}

	@Override
	public final void onExecutionCancel(final Object message, final long cancelTimeMillis) {}

	@Override
	public void onExecutionRemove(final MessageExecution execution) {
		status.calculateAverageTerminationRuntimeInMillis(execution);
	}

	@Override
	public final void onExecutionsHaraKiri(final WatchDogEvent event) {
		status.incrementHaraKiriCount();
		status.setLastHaraKiri(new Date(event.getWatchTimeMillis()));
	}

	@Override
	public final void onExecutionsWatch(final WatchDogEvent event) {
		status.setThreadCount(event.getThreadCount());
		status.setLastExecutionWatch(new Date(event.getWatchTimeMillis()));
		status.setActiveSessionCount(event.getActiveSessionCount());

		status.setRunningExecutionsCount(event.getRunningExecutions().size());
		status.setPendingExecutionsCount(event.getPendingExecutions().size());
		status.setUnfinishedCancelExecutionsCount(event.getUnfinishedCancelExecutions().size());

		status.setLastMaxRuntimeInSeconds(TimeUnit.MILLISECONDS.toSeconds(event.getMaxRuntimeMillis()));
		status.setLastMaxPendingDurationInSeconds(TimeUnit.MILLISECONDS.toSeconds(event.getMaxPendingDurationMillis()));
		status.setLastMaxUnfinishedCancelDurationInSeconds(
				TimeUnit.MILLISECONDS.toSeconds(event.getMaxUnfinishedCancelDurationMillis()));
	}
}
