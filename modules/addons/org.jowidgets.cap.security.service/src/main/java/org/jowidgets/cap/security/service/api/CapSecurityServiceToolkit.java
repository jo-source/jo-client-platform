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

package org.jowidgets.cap.security.service.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.cap.security.service.impl.CapSecurityServiceToolkitImpl;
import org.jowidgets.cap.service.api.plugin.IServiceIdDecoratorPlugin;
import org.jowidgets.classloading.api.SharedClassLoader;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;

public final class CapSecurityServiceToolkit {

	private static ICapSecurityServiceToolkit instance;

	private CapSecurityServiceToolkit() {}

	public static synchronized void initialize(final ICapSecurityServiceToolkit instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			CapSecurityServiceToolkit.instance = instance;
		}
		else {
			throw new IllegalStateException("The CapSecurityServiceToolkit is already initialized");
		}
	}

	public static IServicesDecoratorProvider serviceDecorator() {
		return getInstance().serviceDecorator();
	}

	public static <AUTHORIZATION_TYPE> ISecureServiceDecoratorBuilder<AUTHORIZATION_TYPE> serviceDecoratorBuilder() {
		return getInstance().serviceDecoratorBuilder();
	}

	public static IServiceIdDecoratorPlugin serviceIdDecoratorPlugin() {
		return getInstance().serviceIdDecoratorPlugin();
	}

	public static <AUTHORIZATION_TYPE> IServiceIdDecoratorPluginBuilder<AUTHORIZATION_TYPE> serviceIdDecoratorPluginBuilder() {
		return getInstance().serviceIdDecoratorPluginBuilder();
	}

	public static ICapSecurityServiceToolkit getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<ICapSecurityServiceToolkit> serviceLoader = ServiceLoader.load(
					ICapSecurityServiceToolkit.class,
					SharedClassLoader.getCompositeClassLoader());
			final Iterator<ICapSecurityServiceToolkit> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				instance = new CapSecurityServiceToolkitImpl();
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ ICapSecurityServiceToolkit.class.getName()
						+ "'");
				}
			}
		}
	}
}
