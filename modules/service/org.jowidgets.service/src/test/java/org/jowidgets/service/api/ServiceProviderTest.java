/*
 * Copyright (c) 2016, grossmann
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

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.ValueHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class ServiceProviderTest {

	private static final IServiceId<Object> SERVICE_ID_1 = new ServiceId<Object>(1, Object.class);
	private static final IServiceId<Object> SERVICE_ID_2 = new ServiceId<Object>(2, Object.class);
	private static final IServiceId<Object> SERVICE_ID_3 = new ServiceId<Object>(3, Object.class);

	private static final Object SERVICE_1 = new Object();
	private static final Object SERVICE_2 = new Object();
	private static final Object SERVICE_3 = new Object();

	private static final ServiceProviderMock SERVICE_PROVIDER_HOLDER_1 = new ServiceProviderMock(createServiceProviderMap1());
	private static final ServiceProviderMock SERVICE_PROVIDER_HOLDER_2 = new ServiceProviderMock(createServiceProviderMap2());

	private static final Map<IServiceId<?>, Object> createServiceProviderMap1() {
		final Map<IServiceId<?>, Object> result = new HashMap<IServiceId<?>, Object>();
		result.put(SERVICE_ID_1, SERVICE_1);
		result.put(SERVICE_ID_2, SERVICE_2);
		return result;
	}

	private static final Map<IServiceId<?>, Object> createServiceProviderMap2() {
		final Map<IServiceId<?>, Object> result = new HashMap<IServiceId<?>, Object>();
		result.put(SERVICE_ID_3, SERVICE_3);
		return result;
	}

	@Before
	public void setUp() {
		ServiceProvider.reset();
	}

	@Test
	public void testServiceProviderHolderRegistration() {
		ServiceProvider.registerServiceProviderHolder(SERVICE_PROVIDER_HOLDER_1);
		ServiceProvider.registerServiceProviderHolder(SERVICE_PROVIDER_HOLDER_2);

		Assert.assertEquals(SERVICE_1, ServiceProvider.getService(SERVICE_ID_1));
		Assert.assertEquals(SERVICE_2, ServiceProvider.getService(SERVICE_ID_2));
		Assert.assertEquals(SERVICE_3, ServiceProvider.getService(SERVICE_ID_3));
	}

	@Test
	public void testConcurrentServiceRegistration() {
		ServiceProvider.registerServiceProviderHolder(SERVICE_PROVIDER_HOLDER_1);
		ServiceProvider.registerServiceProviderHolder(SERVICE_PROVIDER_HOLDER_2);
		SERVICE_PROVIDER_HOLDER_1.lock();
		SERVICE_PROVIDER_HOLDER_2.lock();

		final ValueHolder<ConcurrentModificationException> exceptionHolder = new ValueHolder<ConcurrentModificationException>();

		final CountDownLatch latch1 = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(1);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					latch1.countDown();
					ServiceProvider.getService(SERVICE_ID_1);
				}
				catch (final ConcurrentModificationException exception) {
					exceptionHolder.set(exception);
				}
				finally {
					latch2.countDown();
				}
			}
		}).start();

		try {
			latch1.await();
		}
		catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

		ServiceProvider.registerServiceProviderHolder(Mockito.mock(IServiceProviderHolder.class));

		SERVICE_PROVIDER_HOLDER_1.unlock();
		SERVICE_PROVIDER_HOLDER_2.unlock();

		try {
			latch2.await();
		}
		catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

		Assert.assertNull(exceptionHolder.get());
	}

	private static final class ServiceProviderMock implements IServiceProviderHolder, IServiceProvider {

		private final Map<IServiceId<?>, Object> services;

		private CountDownLatch latch;

		ServiceProviderMock(final Map<IServiceId<?>, Object> services) {
			this.services = services;
		}

		void lock() {
			latch = new CountDownLatch(1);
		}

		void unlock() {
			latch.countDown();
		}

		@Override
		public IServiceProvider getServiceProvider() {
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <SERVICE_TYPE> SERVICE_TYPE get(final IServiceId<SERVICE_TYPE> id) {
			blockIfLocked();
			return (SERVICE_TYPE) services.get(id);
		}

		@Override
		public Set<IServiceId<?>> getAvailableServices() {
			blockIfLocked();
			return services.keySet();
		}

		private void blockIfLocked() {
			if (latch != null) {
				try {
					latch.await();
				}
				catch (final InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
