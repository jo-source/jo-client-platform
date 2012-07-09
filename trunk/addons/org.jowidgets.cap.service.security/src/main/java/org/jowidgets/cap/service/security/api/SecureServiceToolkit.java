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

package org.jowidgets.cap.service.security.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.cap.service.security.impl.SecureServiceToolkitImpl;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.Assert;

public final class SecureServiceToolkit {

	private static ISecureServiceToolkit instance;

	private SecureServiceToolkit() {}

	public static synchronized void initialize(final ISecureServiceToolkit instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			SecureServiceToolkit.instance = instance;
		}
		else {
			throw new IllegalStateException("The SecurityServiceToolkit is already initialized");
		}
	}

	public static <SERVICE_TYPE, AUTHORIZATION_TYPE> ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> serviceId(
		final Object id,
		final Class<?> serviceType,
		final AUTHORIZATION_TYPE authorization) {
		return getInstance().serviceId(id, serviceType, authorization);
	}

	public static <SERVICE_TYPE, AUTHORIZATION_TYPE> ISecureServiceId<SERVICE_TYPE, AUTHORIZATION_TYPE> serviceId(
		final IServiceId<SERVICE_TYPE> serviceId,
		final AUTHORIZATION_TYPE authorization) {
		return getInstance().serviceId(serviceId, authorization);
	}

	public static <AUTHORIZATION_TYPE> ISecureEntityId<AUTHORIZATION_TYPE> entityId(
		final Object id,
		final AUTHORIZATION_TYPE create,
		final AUTHORIZATION_TYPE read,
		final AUTHORIZATION_TYPE update,
		final AUTHORIZATION_TYPE delete) {
		return getInstance().entityId(id, create, read, update, delete);
	}

	public static ISecureServiceToolkit getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<ISecureServiceToolkit> serviceLoader = ServiceLoader.load(ISecureServiceToolkit.class);
			final Iterator<ISecureServiceToolkit> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				instance = new SecureServiceToolkitImpl();
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ ISecureServiceToolkit.class.getName()
						+ "'");
				}
			}
		}
	}
}
