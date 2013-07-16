/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.cap.service.spring;

import org.jowidgets.cap.service.api.annotation.CapService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceId;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public final class ServicePostProcessor implements BeanPostProcessor, ApplicationContextAware {

	private ListableBeanFactory beanFactory;
	private BeanProxyFactory beanProxyFactory;

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) {
		try {
			final CapService serviceAnnotation = beanFactory.findAnnotationOnBean(beanName, CapService.class);
			if (serviceAnnotation != null) {
				final Class<?> serviceType;
				if (serviceAnnotation.type() != void.class) {
					serviceType = serviceAnnotation.type();
				}
				else {
					if (bean.getClass().getInterfaces().length == 0) {
						throw new RuntimeException(beanName
							+ " must implement at least one interface or type must be set on @CapService annotation");
					}
					serviceType = bean.getClass().getInterfaces()[0];
				}
				String id = serviceAnnotation.id();
				if (id.isEmpty()) {
					id = serviceType.getName();
				}
				final IServiceId<Object> serviceId = new ServiceId<Object>(id, serviceType);
				SpringServiceProvider.getInstance().addService(serviceId, beanProxyFactory.createProxy(beanName, serviceType));
			}
		}
		catch (final NoSuchBeanDefinitionException e) {
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
		return bean;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		beanFactory = applicationContext;
		beanProxyFactory = new BeanProxyFactory(beanFactory);
	}

}
