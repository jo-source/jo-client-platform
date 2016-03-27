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

package org.jowidgets.cap.service.neo4j.api;

import java.util.Collection;

import org.jowidgets.cap.service.api.exception.IServiceExceptionLogger;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IExceptionLogger;

public interface INeo4JServicesDecoratorProviderBuilder {

	int DEFAULT_ORDER = 1;

	/**
	 * Sets the services that should be transactional
	 * 
	 * @param services The services (must be interfaces) that should be transactional
	 * 
	 * @return This builder
	 */
	INeo4JServicesDecoratorProviderBuilder setTransactionalServices(Collection<? extends Class<?>> services);

	INeo4JServicesDecoratorProviderBuilder addTransactionalServices(Class<?>... services);

	INeo4JServicesDecoratorProviderBuilder setExceptionDecorators(Collection<? extends IDecorator<Throwable>> decorators);

	/**
	 * Adds an exception decorator to the list of exception decorators.
	 * 
	 * Remark: Exception decorators will be invoked in reverse order. This will be done because the default decorators
	 * should be invoked after special decorators.
	 * 
	 * @param decorator The decorator to add
	 * @return This builder.
	 */
	INeo4JServicesDecoratorProviderBuilder addExceptionDecorator(IDecorator<Throwable> decorator);

	@Deprecated
	/**
	 * @deprecated  Exceptions will be logged by jowidgets logging api from now
	 *
	 * Sets the exception logger for the services 
	 * @param logger The logger to set
	 *
	 * @return The builder
	 */
	INeo4JServicesDecoratorProviderBuilder setExceptionLogger(IExceptionLogger logger);

	/**
	 * Sets the exception logger for all services
	 * 
	 * @param logger The logger to set, may be null for default behavior
	 * 
	 * @return This builder
	 */
	INeo4JServicesDecoratorProviderBuilder setExceptionLogger(IServiceExceptionLogger logger);

	INeo4JServicesDecoratorProviderBuilder setOrder(int order);

	IServicesDecoratorProvider build();

}
