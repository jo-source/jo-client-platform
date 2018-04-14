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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Filter;

import org.jowidgets.message.impl.http.server.IExecutionInterceptor;
import org.jowidgets.message.impl.http.server.IMessageExecutionWatchdogListener;

/**
 * Builder for the {@link MessageServletConfig} used for CapServerStarter.
 */
public final class MessageServletConfigBuilder {

	private Long pollIntervalMillis;
	private Long watchdogIntervalMillis;
	private Long sessionInactivityMillis;
	private Long denyRequestPendingtimeoutMillis;
	private Long haraKiriTimeoutMillis;
	private Long haraKiriPendingThreshold;
	private Long executorThreadCount;
	private final List<Class<IExecutionInterceptor<?>>> executionInterceptors;
	private final List<Class<IMessageExecutionWatchdogListener>> messageExecutionWatchdogListeners;
	private final List<Class<Filter>> servletFilters;

	public MessageServletConfigBuilder() {
		this.executionInterceptors = new LinkedList<Class<IExecutionInterceptor<?>>>();
		this.messageExecutionWatchdogListeners = new LinkedList<Class<IMessageExecutionWatchdogListener>>();
		this.servletFilters = new LinkedList<Class<Filter>>();
	}

	public MessageServletConfigBuilder setPollIntervalMillis(final Long pollIntervalMillis) {
		this.pollIntervalMillis = pollIntervalMillis;
		return this;
	}

	public MessageServletConfigBuilder setWatchdogIntervalMillis(final Long watchdogIntervalMillis) {
		this.watchdogIntervalMillis = watchdogIntervalMillis;
		return this;
	}

	public MessageServletConfigBuilder setSessionInactivityMillis(final Long sessionInactivityMillis) {
		this.sessionInactivityMillis = sessionInactivityMillis;
		return this;
	}

	public MessageServletConfigBuilder setDenyRequestPendingtimeoutMillis(final Long denyRequestPendingtimeoutMillis) {
		this.denyRequestPendingtimeoutMillis = denyRequestPendingtimeoutMillis;
		return this;
	}

	public MessageServletConfigBuilder setHaraKiriTimeoutMillis(final Long haraKiriTimeoutMillis) {
		this.haraKiriTimeoutMillis = haraKiriTimeoutMillis;
		return this;
	}

	public MessageServletConfigBuilder setHaraKiriPendingThreashold(final Long haraKiriPendingThreashold) {
		this.haraKiriPendingThreshold = haraKiriPendingThreashold;
		return this;
	}

	public MessageServletConfigBuilder setExecutorThreadCount(final int executorThreadCount) {
		return setExecutorThreadCount(Long.valueOf(executorThreadCount));
	}

	public MessageServletConfigBuilder setExecutorThreadCount(final Long executorThreadCount) {
		this.executorThreadCount = executorThreadCount;
		return this;
	}

	@SuppressWarnings("unchecked")
	public MessageServletConfigBuilder addExecutionInterceptors(
		final Class<? extends IExecutionInterceptor<?>> executionInterceptor) {
		this.executionInterceptors.add((Class<IExecutionInterceptor<?>>) executionInterceptor);
		return this;
	}

	@SuppressWarnings("unchecked")
	public MessageServletConfigBuilder addMessageExecutionWatchdogListeners(
		final Class<? extends IMessageExecutionWatchdogListener> messageExecutionWatchdogListener) {
		this.messageExecutionWatchdogListeners.add((Class<IMessageExecutionWatchdogListener>) messageExecutionWatchdogListener);
		return this;
	}

	@SuppressWarnings("unchecked")
	public MessageServletConfigBuilder addServletFilter(final Class<? extends Filter> filter) {
		this.servletFilters.add((Class<Filter>) filter);
		return this;
	}

	public MessageServletConfig build() {
		return new MessageServletConfig(this);
	}

	Long getPollIntervalMillis() {
		return pollIntervalMillis;
	}

	Long getWatchdogIntervalMillis() {
		return watchdogIntervalMillis;
	}

	Long getSessionInactivityMillis() {
		return sessionInactivityMillis;
	}

	Long getDenyRequestPendingtimeoutMillis() {
		return denyRequestPendingtimeoutMillis;
	}

	Long getHaraKiriTimeoutMillis() {
		return haraKiriTimeoutMillis;
	}

	Long getHaraKiriPendingThreshold() {
		return haraKiriPendingThreshold;
	}

	Long getExecutorThreadCount() {
		return executorThreadCount;
	}

	List<Class<IExecutionInterceptor<?>>> getExecutionInterceptors() {
		return Collections.unmodifiableList(executionInterceptors);
	}

	List<Class<IMessageExecutionWatchdogListener>> getMessageExecutionWatchdogListeners() {
		return Collections.unmodifiableList(messageExecutionWatchdogListeners);
	}

	List<Class<Filter>> getServletFilters() {
		return Collections.unmodifiableList(servletFilters);
	}

}
