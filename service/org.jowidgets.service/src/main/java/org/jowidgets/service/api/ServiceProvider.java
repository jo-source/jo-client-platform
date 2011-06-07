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

package org.jowidgets.service.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.service.impl.CompositeServiceProviderHolder;
import org.jowidgets.util.Assert;

public final class ServiceProvider {

	private static CompositeServiceProviderHolder compositeServiceProviderHolder;

	private ServiceProvider() {}

	public static synchronized void registerServiceProviderHolder(final IServiceProviderHolder serviceProviderHolder) {
		Assert.paramNotNull(serviceProviderHolder, "serviceProviderHolder");
		getCompositeServiceProviderHolder().add(serviceProviderHolder);
	}

	private static synchronized CompositeServiceProviderHolder getCompositeServiceProviderHolder() {
		if (compositeServiceProviderHolder == null) {
			compositeServiceProviderHolder = new CompositeServiceProviderHolder();
			final ServiceLoader<IServiceProviderHolder> widgetServiceLoader = ServiceLoader.load(IServiceProviderHolder.class);
			final Iterator<IServiceProviderHolder> iterator = widgetServiceLoader.iterator();
			while (iterator.hasNext()) {
				compositeServiceProviderHolder.add(iterator.next());
			}
		}
		return compositeServiceProviderHolder;
	}

	public static IServiceProvider getInstance() {
		return getCompositeServiceProviderHolder().getServiceProvider();
	}

	public static <SERVICE_TYPE> SERVICE_TYPE getService(final IServiceId<SERVICE_TYPE> id) {
		return getInstance().get(id);
	}

}
