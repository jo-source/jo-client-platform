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

package org.jowidgets.cap.service.spring.jpa;

import java.beans.PropertyDescriptor;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.tools.bean.BeanDtoDescriptorBuilder;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.annotation.CapService;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.impl.jpa.JpaBeanAccess;
import org.jowidgets.cap.service.impl.jpa.JpaCreatorService;
import org.jowidgets.cap.service.impl.jpa.JpaDeleterService;
import org.jowidgets.cap.service.impl.jpa.JpaReaderService;
import org.jowidgets.cap.service.impl.jpa.jpql.CriteriaQueryCreator;
import org.jowidgets.cap.service.spring.BeanTypeUtil;
import org.jowidgets.cap.service.spring.SpringServiceProvider;
import org.jowidgets.cap.service.tools.entity.EntityServiceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

@CapService
public final class JpaEntityService implements IEntityService, InitializingBean {

	@PersistenceContext
	private EntityManager entityManager;
	private PlatformTransactionManager transactionManager;
	private IEntityService entityService;

	@Required
	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() {
		final EntityServiceBuilder entityServiceBuilder = new EntityServiceBuilder();
		for (final EntityType<?> type : entityManager.getMetamodel().getEntities()) {
			final Class<?> clazz = type.getBindableJavaType();
			if (IBean.class.isAssignableFrom(clazz)) {
				final Class<? extends IBean> beanType = (Class<? extends IBean>) clazz;
				final Class<? extends IBean> beanInterface = new BeanTypeUtil(beanType).getBeanInterface();
				addServicesProviderAndDescriptor(entityServiceBuilder, beanInterface, beanType);
			}
		}
		entityService = entityServiceBuilder.build();
	}

	private void addServicesProviderAndDescriptor(
		final EntityServiceBuilder entityServiceBuilder,
		final Class<? extends IBean> beanInterface,
		final Class<? extends IBean> beanType) {
		final IBeanDtoDescriptor descriptor = createDescriptor(beanInterface);
		final IBeanServicesProvider servicesProvider = createServicesProvider(beanInterface, beanType, descriptor);
		entityServiceBuilder.add(beanInterface, descriptor, servicesProvider);
	}

	private IBeanDtoDescriptor createDescriptor(final Class<? extends IBean> beanInterface) {
		final IBeanDtoDescriptorBuilder builder = new BeanDtoDescriptorBuilder(beanInterface);
		for (final String propertyName : getPropertyNames(beanInterface)) {
			builder.addProperty(propertyName);
		}
		return builder.build();
	}

	private List<String> getPropertyNames(final Class<? extends IBean> beanInterface) {
		final List<String> names = new LinkedList<String>();
		for (final PropertyDescriptor descr : BeanUtils.getPropertyDescriptors(beanInterface)) {
			names.add(descr.getName());
		}
		return names;
	}

	private <T extends IBean> IBeanServicesProvider createServicesProvider(
		final Class<? extends IBean> beanInterface,
		final Class<T> beanType,
		final IBeanDtoDescriptor descriptor) {

		final IBeanServicesProviderBuilder builder = CapServiceToolkit.beanServicesProviderBuilder(
				SpringServiceProvider.getInstance(),
				IEntityService.ID,
				beanInterface);

		final List<String> propertyNames = new LinkedList<String>();
		for (final IProperty property : descriptor.getProperties()) {
			propertyNames.add(property.getName());
		}

		final JpaBeanAccess<T> beanAccess = new JpaBeanAccess<T>(beanType);
		beanAccess.setEntityManager(entityManager);

		final JpaCreatorService creatorService = new JpaCreatorService(beanAccess, propertyNames);
		creatorService.setEntityManager(entityManager);
		builder.setCreatorService(createTransactionProxy(creatorService, "create"));

		final JpaDeleterService deleterService = new JpaDeleterService(beanAccess);
		deleterService.setEntityManager(entityManager);
		builder.setDeleterService(createTransactionProxy(deleterService, "delete"));

		final JpaReaderService<Void> readerService = new JpaReaderService<Void>(new CriteriaQueryCreator(beanType), propertyNames);
		readerService.setEntityManager(entityManager);
		builder.setReaderService(readerService);

		builder.setRefreshService(CapServiceToolkit.refreshServiceBuilder(beanAccess).build());

		final IUpdaterService updaterService = CapServiceToolkit.updaterServiceBuilder(beanAccess).build();
		builder.setUpdaterService(createTransactionProxy(updaterService, "update"));

		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private <T> T createTransactionProxy(final T target, final String method) {
		final TransactionProxyFactoryBean tpfb = new TransactionProxyFactoryBean();
		tpfb.setTransactionManager(transactionManager);
		tpfb.setTarget(target);
		final Properties transactionAttributes = new Properties();
		transactionAttributes.setProperty(method, "PROPAGATION_REQUIRED");
		tpfb.setTransactionAttributes(transactionAttributes);
		tpfb.afterPropertiesSet();
		return (T) tpfb.getObject();
	}

	@Override
	public IBeanDtoDescriptor getDescriptor(final Object entityTypeId) {
		return entityService.getDescriptor(entityTypeId);
	}

	@Override
	public IBeanServicesProvider getBeanServices(final Object entityTypeId) {
		return entityService.getBeanServices(entityTypeId);
	}

}
