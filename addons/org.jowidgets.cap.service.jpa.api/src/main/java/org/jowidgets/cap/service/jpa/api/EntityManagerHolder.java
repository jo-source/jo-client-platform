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
import java.util.ServiceLoader;

import javax.persistence.EntityManager;

import org.jowidgets.util.Assert;

public final class EntityManagerHolder {

	private static IEntityManagerHolder instance;

	private EntityManagerHolder() {}

	public static synchronized void initialize(final IEntityManagerHolder instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			EntityManagerHolder.instance = instance;
		}
		else {
			throw new IllegalStateException("The EntityManagerHolder is already initialized");
		}
	}

	public static IEntityManagerHolder getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	public static EntityManager get() {
		return getInstance().get();
	}

	public static void set(final EntityManager entityManager) {
		getInstance().set(entityManager);
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<IEntityManagerHolder> serviceLoader = ServiceLoader.load(IEntityManagerHolder.class);
			final Iterator<IEntityManagerHolder> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				instance = new DefaultEntityManagerHolder();
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ IEntityManagerHolder.class.getName()
						+ "'");
				}
			}
		}
	}

	private static final class DefaultEntityManagerHolder implements IEntityManagerHolder {

		private final ThreadLocal<EntityManager> threadLocal;

		private DefaultEntityManagerHolder() {
			this.threadLocal = new ThreadLocal<EntityManager>();
		}

		@Override
		public EntityManager get() {
			return threadLocal.get();
		}

		@Override
		public void set(final EntityManager entityManager) {
			threadLocal.set(entityManager);
		}

	}

}
