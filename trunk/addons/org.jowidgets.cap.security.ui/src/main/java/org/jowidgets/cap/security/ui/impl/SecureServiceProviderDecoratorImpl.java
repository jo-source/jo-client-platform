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

package org.jowidgets.cap.security.ui.impl;

import java.util.Set;

import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class SecureServiceProviderDecoratorImpl implements IDecorator<IServiceProvider> {

	@Override
	public IServiceProvider decorate(final IServiceProvider original) {
		return new DecoratedServiceProvider(original);
	}

	private final class DecoratedServiceProvider implements IServiceProvider {

		private final IServiceProvider original;

		private DecoratedServiceProvider(final IServiceProvider original) {
			Assert.paramNotNull(original, "original");
			this.original = original;
		}

		@Override
		public Set<IServiceId<?>> getAvailableServices() {
			return getFilteredServices(original.getAvailableServices());
		}

		@Override
		public <SERVICE_TYPE> SERVICE_TYPE get(final IServiceId<SERVICE_TYPE> id) {
			return getFilteredService(id, original.get(id));
		}

		private Set<IServiceId<?>> getFilteredServices(final Set<IServiceId<?>> original) {
			return original;
		}

		private <SERVICE_TYPE> SERVICE_TYPE getFilteredService(final IServiceId<SERVICE_TYPE> id, final SERVICE_TYPE service) {
			return service;
		}
	}
}
