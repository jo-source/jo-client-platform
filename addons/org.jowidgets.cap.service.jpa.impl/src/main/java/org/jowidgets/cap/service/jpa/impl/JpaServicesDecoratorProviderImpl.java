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

package org.jowidgets.cap.service.jpa.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.jowidgets.cap.service.jpa.api.EntityManagerFactoryProvider;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class JpaServicesDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final EntityManagerFactory entityManagerFactory;
	private final Set<Class<?>> entityManagerServices;
	private final Set<Class<?>> transactionalServices;
	private final int order;

	JpaServicesDecoratorProviderImpl(
		final String persistenceUnitName,
		final Set<Class<?>> entityManagerServices,
		final Set<Class<?>> transactionalServices,
		final int order) {

		Assert.paramNotNull(persistenceUnitName, "persistenceUnitName");
		Assert.paramNotNull(entityManagerServices, "entityManagerServices");
		Assert.paramNotNull(transactionalServices, "transactionalServices");

		this.entityManagerFactory = EntityManagerFactoryProvider.get(persistenceUnitName);
		if (entityManagerFactory == null && !entityManagerServices.isEmpty()) {
			throw new IllegalArgumentException("Could not create an EntityManagerFactory for persistence unit name '"
				+ persistenceUnitName
				+ "'.");
		}

		this.entityManagerServices = new HashSet<Class<?>>(entityManagerServices);
		this.transactionalServices = new HashSet<Class<?>>(transactionalServices);
		this.order = order;
	}

	@Override
	public IDecorator<Object> getDefaultDecorator() {
		return null;
	}

	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final Class<? extends SERVICE_TYPE> type) {
		return new IDecorator<SERVICE_TYPE>() {
			@Override
			public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
				if (entityManagerServices.contains(type) || transactionalServices.contains(type)) {
					//CHECKSTYLE:OFF
					System.out.println("TODO: Decorate: " + original);
					//CHECKSTYLE:ON
					return original;
				}
				else {
					return original;
				}
			}
		};
	}

	@Override
	public int getOrder() {
		return order;
	}

}
