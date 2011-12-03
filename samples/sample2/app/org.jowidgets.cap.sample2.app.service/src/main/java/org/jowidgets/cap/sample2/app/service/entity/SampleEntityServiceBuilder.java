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

package org.jowidgets.cap.sample2.app.service.entity;

import java.util.Collections;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptorBuilder;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.sample2.app.common.bean.ICountry;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.common.bean.IPersonRelationType;
import org.jowidgets.cap.sample2.app.common.bean.IPersonRoleLink;
import org.jowidgets.cap.sample2.app.common.bean.IRole;
import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.sample2.app.service.bean.Country;
import org.jowidgets.cap.sample2.app.service.bean.Person;
import org.jowidgets.cap.sample2.app.service.bean.PersonRelationType;
import org.jowidgets.cap.sample2.app.service.bean.PersonRoleLink;
import org.jowidgets.cap.sample2.app.service.bean.Role;
import org.jowidgets.cap.sample2.app.service.descriptor.CountryDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.PersonDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.PersonRelationTypeDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.RoleDtoDescriptorBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.jpa.api.IJpaServiceFactory;
import org.jowidgets.cap.service.jpa.api.JpaServiceToolkit;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.JpaQueryToolkit;
import org.jowidgets.cap.service.tools.entity.EntityServiceBuilder;
import org.jowidgets.service.api.IServiceRegistry;

public class SampleEntityServiceBuilder extends EntityServiceBuilder {

	private final IJpaServiceFactory serviceFactory;

	public SampleEntityServiceBuilder(final IServiceRegistry registry) {

		this.serviceFactory = JpaServiceToolkit.serviceFactory();

		IBeanDtoDescriptor descriptor;
		IBeanServicesProviderBuilder servicesBuilder;

		//IPerson
		descriptor = new PersonDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(registry, EntityIds.PERSON, Person.class, IPerson.ALL_PROPERTIES);
		add(EntityIds.PERSON, descriptor, servicesBuilder.build(), Collections.singletonList(createPersonRoleLinkDescriptor()));

		//IRole
		descriptor = new RoleDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(registry, EntityIds.ROLE, Role.class, IRole.ALL_PROPERTIES);
		add(EntityIds.ROLE, descriptor, servicesBuilder.build(), Collections.singletonList(createRolePersonLinkDescriptor()));

		//IPersonLinkType
		descriptor = new PersonRelationTypeDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(
				registry,
				EntityIds.PERSON_LINK_TYPE,
				PersonRelationType.class,
				IPersonRelationType.ALL_PROPERTIES);
		add(EntityIds.PERSON_LINK_TYPE, descriptor, servicesBuilder.build());

		//ICountry
		descriptor = new CountryDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(registry, EntityIds.COUNTRY, Country.class, ICountry.ALL_PROPERTIES);
		add(EntityIds.COUNTRY, descriptor, servicesBuilder.build());

		//IPersonRoleLink
		descriptor = new RoleDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(
				registry,
				EntityIds.PERSON_ROLE_LINK,
				PersonRoleLink.class,
				IPersonRoleLink.ALL_PROPERTIES);
		add(EntityIds.PERSON_ROLE_LINK, descriptor, servicesBuilder.build());

		//Linked persons of roles
		descriptor = new PersonDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(
				registry,
				EntityIds.LINKED_PERSONS_OF_ROLES,
				Person.class,
				IPerson.ALL_PROPERTIES);
		servicesBuilder.setReaderService(createPersonsOfRolesReader(true));
		add(EntityIds.LINKED_PERSONS_OF_ROLES, descriptor, servicesBuilder.build());

		//Linkable persons of roles
		descriptor = new PersonDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(
				registry,
				EntityIds.LINKABLE_PERSONS_OF_ROLES,
				Person.class,
				IPerson.ALL_PROPERTIES);
		servicesBuilder.setReaderService(createPersonsOfRolesReader(false));
		add(EntityIds.LINKABLE_PERSONS_OF_ROLES, descriptor, servicesBuilder.build());

		//Linked roles of persons
		descriptor = new RoleDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(
				registry,
				EntityIds.LINKED_ROLES_OF_PERSONS,
				Role.class,
				IRole.ALL_PROPERTIES);
		servicesBuilder.setReaderService(createRolesOfPersonsReader(true));
		servicesBuilder.setDeleterService((IDeleterService) null);
		add(EntityIds.LINKED_ROLES_OF_PERSONS, descriptor, servicesBuilder.build());

		//Linkable roles of persons
		descriptor = new RoleDtoDescriptorBuilder().build();
		servicesBuilder = serviceFactory.beanServicesBuilder(
				registry,
				EntityIds.LINKABLE_ROLES_OF_PERSONS,
				Role.class,
				IRole.ALL_PROPERTIES);
		servicesBuilder.setReaderService(createRolesOfPersonsReader(false));
		add(EntityIds.LINKABLE_ROLES_OF_PERSONS, descriptor, servicesBuilder.build());
	}

	private IEntityLinkDescriptor createRolePersonLinkDescriptor() {
		final IEntityLinkDescriptorBuilder builder = CapCommonToolkit.entityLinkDescriptorBuilder();
		builder.setLinkTypeId(EntityIds.PERSON_ROLE_LINK);
		builder.setLinkedTypeId(EntityIds.LINKED_PERSONS_OF_ROLES);
		builder.setLinkableTypeId(EntityIds.LINKABLE_PERSONS_OF_ROLES);
		builder.setSourceProperties(IRole.ID_PROPERTY, IPersonRoleLink.ROLE_ID_PROPERTY);
		builder.setDestinationProperties(IPerson.ID_PROPERTY, IPersonRoleLink.PERSON_ID_PROPERTY);
		return builder.build();
	}

	private IReaderService<Void> createPersonsOfRolesReader(final boolean linked) {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Person.class);
		queryBuilder.setParentPropertyPath(linked, "personRoleLinks", "role");
		if (!linked) {
			final IFilter filter = CapCommonToolkit.filterFactory().arithmeticFilter(
					IPerson.ACTIVE_PROPERTY,
					ArithmeticOperator.EQUAL,
					Boolean.TRUE);
			queryBuilder.addFilter(filter);
		}
		return serviceFactory.readerService(Person.class, queryBuilder.build(), IPerson.ALL_PROPERTIES);
	}

	private IEntityLinkDescriptor createPersonRoleLinkDescriptor() {
		final IEntityLinkDescriptorBuilder builder = CapCommonToolkit.entityLinkDescriptorBuilder();
		builder.setLinkTypeId(EntityIds.PERSON_ROLE_LINK);
		builder.setLinkedTypeId(EntityIds.LINKED_ROLES_OF_PERSONS);
		builder.setLinkableTypeId(EntityIds.LINKABLE_ROLES_OF_PERSONS);
		builder.setSourceProperties(IPerson.ID_PROPERTY, IPersonRoleLink.PERSON_ID_PROPERTY);
		builder.setDestinationProperties(IRole.ID_PROPERTY, IPersonRoleLink.ROLE_ID_PROPERTY);
		return builder.build();
	}

	private IReaderService<Void> createRolesOfPersonsReader(final boolean linked) {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Role.class);
		queryBuilder.setParentPropertyPath(linked, "personRoleLinks", "person");
		return serviceFactory.readerService(Role.class, queryBuilder.build(), IRole.ALL_PROPERTIES);
	}
}
