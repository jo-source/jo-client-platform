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

import org.jowidgets.cap.service.hibernate.api.ICancelServicesDecoratorProviderBuilder;
import org.jowidgets.cap.service.hibernate.api.IHibernateServiceToolkit;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionHandler;
import org.jowidgets.util.concurrent.ThreadInterruptObserver;

public final class HibernateServiceToolkitImpl implements IHibernateServiceToolkit {

	private static final ILogger LOGGER = LoggerProvider.get(HibernateServiceToolkitImpl.class);

	private final IDecorator<Throwable> exceptionDecorator;
	private final ThreadInterruptObserver threadInterruptObserver;

	public HibernateServiceToolkitImpl() {
		this.exceptionDecorator = new HibernateExceptionDecoratorImpl();
		this.threadInterruptObserver = new ThreadInterruptObserver(
			HibernateServiceToolkitImpl.class.getName() + "-CancelQueryThreadInterruptWatchdog-",
			new LoggingExceptionHandler());
	}

	@Override
	public IDecorator<Throwable> exceptionDecorator() {
		return exceptionDecorator;
	}

	@Override
	public ICancelServicesDecoratorProviderBuilder cancelServiceDecoratorProviderBuilder(final String persistenceUnitName) {
		if (!threadInterruptObserver.isRunning()) {
			threadInterruptObserver.start();
		}
		return new CancelServicesDecoratorProviderBuilder(persistenceUnitName, threadInterruptObserver);
	}

	private class LoggingExceptionHandler implements IExceptionHandler {
		@Override
		public void handleException(final Throwable exception) {
			LOGGER.error(exception);
		}
	}

}
