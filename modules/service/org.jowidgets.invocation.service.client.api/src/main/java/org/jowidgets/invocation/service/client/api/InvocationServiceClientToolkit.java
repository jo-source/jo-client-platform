/*
 * Copyright (c) 2010, grossmann
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

package org.jowidgets.invocation.service.client.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.classloading.api.SharedClassLoader;

public final class InvocationServiceClientToolkit {

	private static IInvocationServiceClientToolkit toolkit;

	private InvocationServiceClientToolkit() {}

	public static void initialize(final IInvocationServiceClientToolkit toolkit) {
		if (toolkit == null) {
			throw new IllegalArgumentException("The parameter 'toolkit' must not be null");
		}
		if (InvocationServiceClientToolkit.toolkit != null) {
			throw new IllegalStateException("Toolkit is already initialized");
		}
		InvocationServiceClientToolkit.toolkit = toolkit;
	}

	public static boolean isInitialized() {
		return toolkit != null;
	}

	public static synchronized IInvocationServiceClientToolkit getInstance() {
		if (toolkit == null) {
			final ServiceLoader<IInvocationServiceClientToolkit> toolkitProviderLoader = ServiceLoader.load(
					IInvocationServiceClientToolkit.class,
					SharedClassLoader.getCompositeClassLoader());
			final Iterator<IInvocationServiceClientToolkit> iterator = toolkitProviderLoader.iterator();

			if (!iterator.hasNext()) {
				throw new IllegalStateException("No implementation found for '"
					+ IInvocationServiceClientToolkit.class.getName()
					+ "'");
			}

			InvocationServiceClientToolkit.toolkit = iterator.next();

			if (iterator.hasNext()) {
				throw new IllegalStateException("More than one implementation found for '"
					+ IInvocationServiceClientToolkit.class.getName()
					+ "'");
			}

		}
		return toolkit;
	}

	public static IInvocationServiceClient getClient(final Object brokerId) {
		return getInstance().getClient(brokerId);
	}

	public static IInvocationServiceClientBuilder getClientBuilder(final Object brokerId) {
		return getInstance().getClientBuilder(brokerId);
	}

}
