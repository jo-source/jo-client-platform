/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.hibernate.impl;

import java.util.concurrent.ThreadFactory;

import org.jowidgets.cap.service.hibernate.api.ICancelServicesDecoratorProviderBuilder;
import org.jowidgets.cap.service.hibernate.api.IHibernateServiceToolkit;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.DefaultProvider;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionHandler;
import org.jowidgets.util.IFactory;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.concurrent.DaemonThreadFactory;
import org.jowidgets.util.concurrent.IThreadInterruptObservable;
import org.jowidgets.util.concurrent.ThreadInterruptObserver;

public final class HibernateServiceToolkitImpl implements IHibernateServiceToolkit {

	private static final IExceptionHandler THREAD_INTERRUPT_OBSERVER_EXCEPTION_HANDLER = new LoggingExceptionHandler(
		LoggerProvider.get(CancelServicesDecoratorProviderImpl.class, LoggingExceptionHandler.class));

	private static final ThreadFactory THREAD_INTERRUPT_OBSERVER_THREAD_FACTORY = DaemonThreadFactory.create(
			HibernateServiceToolkitImpl.class.getName() + ".QueryThreadInterruptObserver");

	private static final long THREAD_INTERRUPT_OBSERVER_DEFAULT_PERIOD = 1000;

	private final IDecorator<Throwable> exceptionDecorator;
	private final IProvider<IThreadInterruptObservable> threadInterruptObservableProvider;

	public HibernateServiceToolkitImpl() {
		this.exceptionDecorator = new HibernateExceptionDecoratorImpl();
		this.threadInterruptObservableProvider = new DefaultProvider<IThreadInterruptObservable>(
			new DefaultThreadInterruptObserverFactory());
	}

	@Override
	public IDecorator<Throwable> exceptionDecorator() {
		return exceptionDecorator;
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder cancelServiceDecoratorProviderBuilder(final String persistenceUnitName) {
		return new CancelServicesDecoratorProviderBuilder(persistenceUnitName, threadInterruptObservableProvider);
	}

	private class DefaultThreadInterruptObserverFactory implements IFactory<IThreadInterruptObservable> {
		@Override
		public IThreadInterruptObservable create() {
			final ThreadInterruptObserver result = new ThreadInterruptObserver(
				THREAD_INTERRUPT_OBSERVER_THREAD_FACTORY,
				THREAD_INTERRUPT_OBSERVER_EXCEPTION_HANDLER,
				THREAD_INTERRUPT_OBSERVER_DEFAULT_PERIOD);
			result.start();
			return result;
		}
	}

}
