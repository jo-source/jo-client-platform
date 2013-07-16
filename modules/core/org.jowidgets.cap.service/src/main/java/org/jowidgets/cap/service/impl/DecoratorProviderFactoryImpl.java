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

package org.jowidgets.cap.service.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jowidgets.cap.service.api.decorator.IAsyncDecoratorProviderBuilder;
import org.jowidgets.cap.service.api.decorator.IDecoratorProviderFactory;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.concurrent.DaemonThreadFactory;

final class DecoratorProviderFactoryImpl implements IDecoratorProviderFactory {

	private final Executor executor;
	private final ScheduledExecutorService scheduledExecutorService;

	DecoratorProviderFactoryImpl() {
		this.executor = Executors.newFixedThreadPool(50, new DaemonThreadFactory());
		this.scheduledExecutorService = Executors.newScheduledThreadPool(20, new DaemonThreadFactory());
	}

	@Override
	public IServicesDecoratorProvider asyncDecoratorProvider(
		final Executor executor,
		final ScheduledExecutorService scheduledExecutorService,
		final Long executorCallbackDelay) {
		Assert.paramNotNull(executor, "executor");
		Assert.paramNotNull(scheduledExecutorService, "scheduledExecutorService");
		Assert.paramNotNull(executorCallbackDelay, "executorCallbackDelay");
		final IAsyncDecoratorProviderBuilder builder = asyncDecoratorProviderBuilder().setExecutor(executor);
		builder.setDelayExecutor(scheduledExecutorService);
		builder.setExecutorCallbackDelay(Long.valueOf(executorCallbackDelay));
		return builder.build();
	}

	@Override
	public IServicesDecoratorProvider asyncDecoratorProvider(final Long executorCallbackDelay) {
		Assert.paramNotNull(executorCallbackDelay, "executorCallbackDelay");
		return asyncDecoratorProviderBuilder().setExecutorCallbackDelay(executorCallbackDelay.longValue()).build();
	}

	@Override
	public IServicesDecoratorProvider asyncDecoratorProvider() {
		return asyncDecoratorProviderBuilder().build();
	}

	@Override
	public IAsyncDecoratorProviderBuilder asyncDecoratorProviderBuilder() {
		return new AsyncDecoratorProviderBuilderImpl(executor, scheduledExecutorService);
	}

}
