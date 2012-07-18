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

package org.jowidgets.cap.sample1.service.entity;

import java.util.Collections;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptorBuilder;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.sample1.common.entity.EntityIds;
import org.jowidgets.cap.sample1.common.entity.ISampleConfig;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.service.datastore.AuthorizationInitializer;
import org.jowidgets.cap.sample1.service.datastore.GenericBeanInitializer;
import org.jowidgets.cap.sample1.service.datastore.RoleInitializer;
import org.jowidgets.cap.sample1.service.datastore.UserRoleLinkInitializer;
import org.jowidgets.cap.sample1.service.reader.LinkableRolesOfUsersReaderService;
import org.jowidgets.cap.sample1.service.reader.LinkableUserOfRolesReaderService;
import org.jowidgets.cap.sample1.service.reader.LinkedRolesOfUsersReaderService;
import org.jowidgets.cap.sample1.service.reader.LinkedUserOfRolesReaderService;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataStore;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.cap.service.impl.dummy.service.DummyServiceFactory;
import org.jowidgets.cap.service.tools.entity.EntityServiceBuilder;
import org.jowidgets.service.api.IServiceRegistry;

public class SampleEntityServiceBuilder extends EntityServiceBuilder {

	@SuppressWarnings("unchecked")
	public SampleEntityServiceBuilder(final IServiceRegistry registry) {

		//IUser
		IBeanDtoDescriptor descriptor = new UserDtoDescriptorBuilder().build();
		IBeanServicesProvider servicesProvider = DummyServiceFactory.beanServices(
				registry,
				IUser.class,
				EntityDataStore.getEntityData(IUser.class),
				IUser.ALL_PROPERTIES);
		add(IUser.class, descriptor, servicesProvider, Collections.singletonList(createUserRoleLinkDescriptor()));

		//Linked users of roles
		descriptor = new UserDtoDescriptorBuilder(IUser.LINK_PROPERTIES).build();
		IBeanServicesProviderBuilder servicesProviderBuilder = DummyServiceFactory.beanServicesBuilder(
				registry,
				EntityIds.VIRTUAL_USERS_OF_ROLES,
				EntityDataStore.getEntityData(IUser.class),
				IUser.ALL_PROPERTIES);
		servicesProviderBuilder.setReaderService(new LinkedUserOfRolesReaderService());
		add(EntityIds.VIRTUAL_USERS_OF_ROLES, descriptor, servicesProviderBuilder.build());

		//Linkable users of roles
		descriptor = new UserDtoDescriptorBuilder(IUser.LINK_PROPERTIES).build();
		servicesProviderBuilder = DummyServiceFactory.beanServicesBuilder(
				registry,
				EntityIds.VIRTUAL_LINKABLE_USERS_OF_ROLES,
				EntityDataStore.getEntityData(IUser.class),
				IUser.ALL_PROPERTIES);
		servicesProviderBuilder.setReaderService(new LinkableUserOfRolesReaderService());
		add(EntityIds.VIRTUAL_LINKABLE_USERS_OF_ROLES, descriptor, servicesProviderBuilder.build());

		//Role
		descriptor = new RoleDtoDescriptorBuilder().build();
		servicesProvider = DummyServiceFactory.beanPropertyMapServices(
				registry,
				EntityIds.ROLE,
				(IEntityData<? extends IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.ROLE),
				RoleInitializer.ALL_PROPERTIES);
		add(EntityIds.ROLE, descriptor, servicesProvider, Collections.singletonList(createRoleUserLinkDescriptor()));

		//Linked roles of user
		descriptor = new RoleDtoDescriptorBuilder(RoleInitializer.LINK_PROPERTIES).build();
		servicesProviderBuilder = DummyServiceFactory.beanPropertyMapServicesBuilder(
				registry,
				EntityIds.VIRTUAL_ROLES_OF_USERS,
				(IEntityData<? extends IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.ROLE),
				RoleInitializer.ALL_PROPERTIES);
		servicesProviderBuilder.setReaderService(new LinkedRolesOfUsersReaderService());
		add(EntityIds.VIRTUAL_ROLES_OF_USERS, descriptor, servicesProviderBuilder.build());

		//Linkable roles of user
		descriptor = new RoleDtoDescriptorBuilder(RoleInitializer.LINK_PROPERTIES).build();
		servicesProviderBuilder = DummyServiceFactory.beanPropertyMapServicesBuilder(
				registry,
				EntityIds.VIRTUAL_LINKABLE_ROLES_OF_USERS,
				(IEntityData<? extends IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.ROLE),
				RoleInitializer.ALL_PROPERTIES);
		servicesProviderBuilder.setReaderService(new LinkableRolesOfUsersReaderService());
		add(EntityIds.VIRTUAL_LINKABLE_ROLES_OF_USERS, descriptor, servicesProviderBuilder.build());

		//User - Role link
		descriptor = new RoleDtoDescriptorBuilder().build();
		servicesProviderBuilder = DummyServiceFactory.beanPropertyMapServicesBuilder(
				registry,
				EntityIds.USER_ROLE_LINK,
				(IEntityData<? extends IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.USER_ROLE_LINK),
				UserRoleLinkInitializer.ALL_PROPERTIES);
		add(EntityIds.USER_ROLE_LINK, descriptor, servicesProviderBuilder.build());

		//Authorization
		descriptor = new AuthorizationDtoDescriptorBuilder().build();
		servicesProvider = DummyServiceFactory.beanPropertyMapServices(
				registry,
				EntityIds.AUTHORIZATION,
				(IEntityData<? extends IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.AUTHORIZATION),
				AuthorizationInitializer.ALL_PROPERTIES);
		add(EntityIds.AUTHORIZATION, descriptor, servicesProvider);

		//GenericBean
		descriptor = new GenericBeanDtoDescriptorBuilder().build();
		servicesProvider = DummyServiceFactory.beanPropertyMapServices(
				registry,
				EntityIds.GENERIC_BEAN,
				(IEntityData<? extends IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.GENERIC_BEAN),
				GenericBeanInitializer.ALL_PROPERTIES);
		add(EntityIds.GENERIC_BEAN, descriptor, servicesProvider);

		//IUser
		descriptor = new SampleConfigDtoDescriptorBuilder().build();
		servicesProvider = DummyServiceFactory.beanServices(
				registry,
				ISampleConfig.class,
				EntityDataStore.getEntityData(ISampleConfig.class),
				ISampleConfig.ALL_PROPERTIES);
		add(ISampleConfig.class, descriptor, servicesProvider);

	}

	private IEntityLinkDescriptor createUserRoleLinkDescriptor() {
		final IEntityLinkDescriptorBuilder builder = CapCommonToolkit.entityLinkDescriptorBuilder();
		builder.setLinkEntityId(EntityIds.USER_ROLE_LINK);
		builder.setLinkedEntityId(EntityIds.VIRTUAL_ROLES_OF_USERS);
		builder.setLinkableEntityId(EntityIds.VIRTUAL_LINKABLE_ROLES_OF_USERS);
		builder.setSourceProperties(IUser.ID_PROPERTY, UserRoleLinkInitializer.USER_ID);
		builder.setDestinationProperties(IBean.ID_PROPERTY, UserRoleLinkInitializer.ROLE_ID);
		return builder.build();
	}

	private IEntityLinkDescriptor createRoleUserLinkDescriptor() {
		final IEntityLinkDescriptorBuilder builder = CapCommonToolkit.entityLinkDescriptorBuilder();
		builder.setLinkEntityId(EntityIds.USER_ROLE_LINK);
		builder.setLinkedEntityId(EntityIds.VIRTUAL_USERS_OF_ROLES);
		builder.setLinkableEntityId(EntityIds.VIRTUAL_LINKABLE_USERS_OF_ROLES);
		builder.setSourceProperties(IBean.ID_PROPERTY, UserRoleLinkInitializer.ROLE_ID);
		builder.setDestinationProperties(IUser.ID_PROPERTY, UserRoleLinkInitializer.USER_ID);
		return builder.build();
	}
}
