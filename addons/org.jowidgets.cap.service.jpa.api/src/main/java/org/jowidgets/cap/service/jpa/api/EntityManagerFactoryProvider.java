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

package org.jowidgets.cap.service.jpa.api;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jowidgets.util.Assert;

public final class EntityManagerFactoryProvider {

	private static IEntityManagerFactoryProvider instance;

	private EntityManagerFactoryProvider() {}

	public static synchronized void initialize(final IEntityManagerFactoryProvider instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			EntityManagerFactoryProvider.instance = instance;
		}
		else {
			throw new IllegalStateException("The EntityManagerFactoryProvider is already initialized");
		}
	}

	public static IEntityManagerFactoryProvider getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	public static EntityManagerFactory get(final String persistenceUnitName) {
		return getInstance().get(persistenceUnitName);
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<IEntityManagerFactoryProvider> serviceLoader = ServiceLoader.load(IEntityManagerFactoryProvider.class);
			final Iterator<IEntityManagerFactoryProvider> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				instance = new DefaultEntityManagerFactoryProvider();
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ IEntityManagerFactoryProvider.class.getName()
						+ "'");
				}
			}
		}
	}

	private static final class DefaultEntityManagerFactoryProvider implements IEntityManagerFactoryProvider {

		private final Map<String, EntityManagerFactory> factories;

		private DefaultEntityManagerFactoryProvider() {
			this.factories = new ConcurrentHashMap<String, EntityManagerFactory>();
		}

		@Override
		public EntityManagerFactory get(final String persistenceUnitName) {
			Assert.paramNotEmpty(persistenceUnitName, "persistenceUnitName");
			final EntityManagerFactory result = factories.get(persistenceUnitName);
			if (result != null) {
				return result;
			}
			else {
				return createFactory(persistenceUnitName);
			}
		}

		private synchronized EntityManagerFactory createFactory(final String persistenceUnitName) {
			EntityManagerFactory result = factories.get(persistenceUnitName);
			if (result == null) {
				result = Persistence.createEntityManagerFactory(persistenceUnitName);
				if (result != null) {
					factories.put(persistenceUnitName, result);
				}
			}
			return result;
		}

	}

}
