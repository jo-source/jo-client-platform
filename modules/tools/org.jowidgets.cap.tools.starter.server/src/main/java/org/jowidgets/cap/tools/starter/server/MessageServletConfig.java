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

package org.jowidgets.cap.tools.starter.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;

import org.jowidgets.message.impl.http.server.IExecutionInterceptor;
import org.jowidgets.message.impl.http.server.IMessageExecutionWatchdogListener;
import org.jowidgets.message.impl.http.server.MessageServlet;
import org.jowidgets.util.Assert;

/**
 * Configuration for the {@link MessageServlet}.
 * 
 * This class is only useful in combination with the {@link CapServerStarter}. If using in production, the parameters will
 * be configured with help of the web.xml like this:
 * 
 * <servlet>
 * <servlet-name>remoting</servlet-name>
 * <servlet-class>org.jowidgets.security.impl.http.server.SecurityRemotingServlet</servlet-class>
 * 
 * <init-param>
 * <param-name>executionInterceptors</param-name>
 * <param-value>org.jowidgets.message.impl.http.server.UserLocaleExecutionInterceptor</param-value>
 * </init-param>
 * 
 * <init-param>
 * <param-name>messageExecutionsWatchdogListeners</param-name>
 * <param-value>org.jowidgets.cap.tools.starter.server.LoggingWatchdogListener</param-value>
 * </init-param>
 * 
 * </servlet>
 */
public final class MessageServletConfig {

	private final Long pollIntervalMillis;
	private final Long watchdogIntervalMillis;
	private final Long sessionInactivityMillis;
	private final Long denyRequestPendingtimeoutMillis;
	private final Long haraKiriTimeoutMillis;
	private final Long haraKiriPendingThreshold;
	private final Long executorThreadCount;
	private final List<Class<IExecutionInterceptor<?>>> executionInterceptors;
	private final List<Class<IMessageExecutionWatchdogListener>> messageExecutionWatchdogListeners;

	private final List<Class<Filter>> servletFilters;

	MessageServletConfig() {
		this(new MessageServletConfigBuilder());
	}

	MessageServletConfig(final MessageServletConfigBuilder builder) {

		Assert.paramNotNull(builder.getExecutionInterceptors(), "builder.getExecutionInterceptors()");
		Assert.paramNotNull(builder.getMessageExecutionWatchdogListeners(), "builder.getMessageExecutionWatchdogListeners()");

		this.pollIntervalMillis = builder.getPollIntervalMillis();
		this.watchdogIntervalMillis = builder.getWatchdogIntervalMillis();
		this.sessionInactivityMillis = builder.getSessionInactivityMillis();
		this.denyRequestPendingtimeoutMillis = builder.getDenyRequestPendingtimeoutMillis();
		this.haraKiriTimeoutMillis = builder.getHaraKiriTimeoutMillis();
		this.haraKiriPendingThreshold = builder.getHaraKiriPendingThreshold();
		this.executorThreadCount = builder.getExecutorThreadCount();

		this.executionInterceptors = Collections.unmodifiableList(
				new ArrayList<Class<IExecutionInterceptor<?>>>(builder.getExecutionInterceptors()));

		this.messageExecutionWatchdogListeners = Collections.unmodifiableList(
				new ArrayList<Class<IMessageExecutionWatchdogListener>>(builder.getMessageExecutionWatchdogListeners()));

		this.servletFilters = Collections.unmodifiableList(new ArrayList<Class<Filter>>(builder.getServletFilters()));
	}

	public Long getPollIntervalMillis() {
		return pollIntervalMillis;
	}

	public Long getWatchdogIntervalMillis() {
		return watchdogIntervalMillis;
	}

	public Long getSessionInactivityMillis() {
		return sessionInactivityMillis;
	}

	public Long getDenyRequestPendingtimeoutMillis() {
		return denyRequestPendingtimeoutMillis;
	}

	public Long getHaraKiriTimeoutMillis() {
		return haraKiriTimeoutMillis;
	}

	public Long getHaraKiriPendingThreshold() {
		return haraKiriPendingThreshold;
	}

	public Long getExecutorThreadCount() {
		return executorThreadCount;
	}

	public List<Class<IExecutionInterceptor<?>>> getExecutionInterceptors() {
		return executionInterceptors;
	}

	public List<Class<IMessageExecutionWatchdogListener>> getMessageExecutionWatchdogListeners() {
		return messageExecutionWatchdogListeners;
	}

	public List<Class<Filter>> getServletFilters() {
		return servletFilters;
	}

}
