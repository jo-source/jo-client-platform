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

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.filter.ArithmeticFilter;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.sample2.app.common.bean.IAuthorization;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.common.bean.IPersonPersonLink;
import org.jowidgets.cap.sample2.app.common.bean.IPersonRelationType;
import org.jowidgets.cap.sample2.app.common.bean.IPersonRoleLink;
import org.jowidgets.cap.sample2.app.common.bean.IRole;
import org.jowidgets.cap.sample2.app.common.bean.IRoleAuthorizationLink;
import org.jowidgets.cap.sample2.app.common.entity.EntityIds;
import org.jowidgets.cap.sample2.app.service.bean.Authorization;
import org.jowidgets.cap.sample2.app.service.bean.Country;
import org.jowidgets.cap.sample2.app.service.bean.Person;
import org.jowidgets.cap.sample2.app.service.bean.PersonPersonLink;
import org.jowidgets.cap.sample2.app.service.bean.PersonRelationType;
import org.jowidgets.cap.sample2.app.service.bean.PersonRoleLink;
import org.jowidgets.cap.sample2.app.service.bean.Role;
import org.jowidgets.cap.sample2.app.service.bean.RoleAuthorizationLink;
import org.jowidgets.cap.sample2.app.service.descriptor.AuthorizationDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.CountryDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.LinkedPersonOfSourcePersonDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.LinkedSourcePersonOfPersonDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.PersonDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.PersonOfSourcePersonLinkDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.PersonPersonLinkDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.PersonRelationTypeDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.RoleDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.descriptor.SourcePersonOfPersonLinkDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.service.loader.PersonRelationTypeLoader;
import org.jowidgets.cap.sample2.app.service.validation.PersonLoginNameConstraintValidator;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanEntityBluePrint;
import org.jowidgets.cap.service.api.entity.IBeanEntityLinkBluePrint;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.IPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.JpaQueryToolkit;
import org.jowidgets.cap.service.jpa.tools.entity.JpaEntityServiceBuilderWrapper;
import org.jowidgets.service.api.IServiceRegistry;

public class SampleEntityServiceBuilder extends JpaEntityServiceBuilderWrapper {

	private final List<PersonRelationType> personRelationTypes;

	public SampleEntityServiceBuilder(final IServiceRegistry registry) {
		super(registry);

		this.personRelationTypes = PersonRelationTypeLoader.load();

		//IPerson
		IBeanEntityBluePrint entityBp = addEntity().setEntityId(EntityIds.PERSON).setBeanType(Person.class);
		entityBp.setDtoDescriptor(new PersonDtoDescriptorBuilder());
		entityBp.setCreatorService(createPersonCreatorService());
		entityBp.setUpdaterService(createPersonUpdaterService());
		addPersonLinkDescriptors(entityBp);

		//IRole
		entityBp = addEntity().setEntityId(EntityIds.ROLE).setBeanType(Role.class);
		entityBp.setDtoDescriptor(new RoleDtoDescriptorBuilder());
		addRoleLinkDescriptors(entityBp);

		//IAuthorization
		entityBp = addEntity().setEntityId(EntityIds.AUTHORIZATION).setBeanType(Authorization.class);
		entityBp.setDtoDescriptor(new AuthorizationDtoDescriptorBuilder());
		addAuthorizationRoleLinkDescriptor(entityBp);

		//IPersonLinkType
		entityBp = addEntity().setEntityId(EntityIds.PERSON_LINK_TYPE).setBeanType(PersonRelationType.class);
		entityBp.setDtoDescriptor(new PersonRelationTypeDtoDescriptorBuilder());

		//ICountry
		entityBp = addEntity().setEntityId(EntityIds.COUNTRY).setBeanType(Country.class);
		entityBp.setDtoDescriptor(new CountryDtoDescriptorBuilder());

		//IPersonsOfSourcePersonLink
		entityBp = addEntity().setEntityId(EntityIds.PERSONS_OF_SOURCE_PERSONS_LINK).setBeanType(PersonPersonLink.class);
		entityBp.setDtoDescriptor(new PersonOfSourcePersonLinkDtoDescriptorBuilder());

		//SourcePersonOfPersonLink
		entityBp = addEntity().setEntityId(EntityIds.SOURCE_PERSONS_OF_PERSONS_LINK).setBeanType(PersonPersonLink.class);
		entityBp.setDtoDescriptor(new SourcePersonOfPersonLinkDtoDescriptorBuilder());

		//Linked persons of source persons
		entityBp = addEntity().setEntityId(EntityIds.LINKED_PERSONS_OF_SOURCE_PERSONS).setBeanType(PersonPersonLink.class);
		entityBp.setDtoDescriptor(new LinkedPersonOfSourcePersonDtoDescriptorBuilder());
		entityBp.setReaderService(createLinkedPersonsOfSourcePersonsReader());
		entityBp.setCreatorService((ICreatorService) null);

		//Linked source person of person 
		entityBp = addEntity().setEntityId(EntityIds.LINKED_SOURCE_PERSONS_OF_PERSONS).setBeanType(PersonPersonLink.class);
		entityBp.setDtoDescriptor(new LinkedSourcePersonOfPersonDtoDescriptorBuilder());
		entityBp.setReaderService(createLinkedSourcePersonsOfPersonsReader());
		entityBp.setCreatorService((ICreatorService) null);

		//Linkable persons of  persons
		entityBp = addEntity().setEntityId(EntityIds.LINKABLE_PERSONS_OF_PERSONS).setBeanType(Person.class);
		entityBp.setDtoDescriptor(new PersonDtoDescriptorBuilder());

		//Linked persons of roles
		entityBp = addEntity().setEntityId(EntityIds.LINKED_PERSONS_OF_ROLES).setBeanType(Person.class);
		entityBp.setDtoDescriptor(new PersonDtoDescriptorBuilder());
		entityBp.setReaderService(createPersonsOfRolesReader(true));
		entityBp.setUpdaterService(createPersonUpdaterService());
		addPersonLinkDescriptors(entityBp);

		//Linkable persons of roles
		entityBp = addEntity().setEntityId(EntityIds.LINKABLE_PERSONS_OF_ROLES).setBeanType(Person.class);
		entityBp.setDtoDescriptor(new PersonDtoDescriptorBuilder());
		entityBp.setReaderService(createPersonsOfRolesReader(false));

		//Linked roles of persons
		entityBp = addEntity().setEntityId(EntityIds.LINKED_ROLES_OF_PERSONS).setBeanType(Role.class);
		entityBp.setDtoDescriptor(new RoleDtoDescriptorBuilder());
		entityBp.setReaderService(createRolesOfPersonsReader(true));
		entityBp.setDeleterService((IDeleterService) null);
		addRoleLinkDescriptors(entityBp);

		//Linkable roles of persons
		entityBp = addEntity().setEntityId(EntityIds.LINKABLE_ROLES_OF_PERSONS).setBeanType(Role.class);
		entityBp.setDtoDescriptor(new RoleDtoDescriptorBuilder());
		entityBp.setReaderService(createRolesOfPersonsReader(false));
		entityBp.setDeleterService((IDeleterService) null);

		//Linked roles of authorizations
		entityBp = addEntity().setEntityId(EntityIds.LINKED_ROLES_OF_AUTHORIZATIONS).setBeanType(Role.class);
		entityBp.setDtoDescriptor(new RoleDtoDescriptorBuilder());
		entityBp.setReaderService(createRolesOfAuthorizationsReader(true));
		entityBp.setDeleterService((IDeleterService) null);
		addRoleLinkDescriptors(entityBp);

		//Linkable roles of authorizations
		entityBp = addEntity().setEntityId(EntityIds.LINKABLE_ROLES_OF_AUTHORIZATIONS).setBeanType(Role.class);
		entityBp.setDtoDescriptor(new RoleDtoDescriptorBuilder());
		entityBp.setReaderService(createRolesOfAuthorizationsReader(false));
		entityBp.setDeleterService((IDeleterService) null);

		//Linked authorizations of roles
		entityBp = addEntity().setEntityId(EntityIds.LINKED_AUTHORIZATION_OF_ROLES).setBeanType(Authorization.class);
		entityBp.setDtoDescriptor(new AuthorizationDtoDescriptorBuilder());
		entityBp.setReaderService(createAuthorizationsOfRolesReader(true));
		entityBp.setDeleterService((IDeleterService) null);
		addAuthorizationRoleLinkDescriptor(entityBp);

		//Linkable authorizations of roles
		entityBp = addEntity().setEntityId(EntityIds.LINKABLE_AUTHORIZATIONS_OF_ROLES).setBeanType(Authorization.class);
		entityBp.setDtoDescriptor(new AuthorizationDtoDescriptorBuilder());
		entityBp.setReaderService(createAuthorizationsOfRolesReader(false));
		entityBp.setDeleterService((IDeleterService) null);
	}

	private void addPersonLinkDescriptors(final IBeanEntityBluePrint entityBp) {
		addPersonLinkDescriptors(entityBp, true);
	}

	private void addPersonLinkDescriptors(final IBeanEntityBluePrint entityBp, final boolean createEntities) {
		addPersonRoleLinkDescriptor(entityBp);
		for (final IPersonRelationType personRelationType : personRelationTypes) {
			addPersonPersonLinkDescriptor(
					entityBp,
					createEntities,
					false,
					personRelationType.getId(),
					personRelationType.getReverseRelationName(),
					personRelationType.getReverseRelationName());

			addPersonPersonLinkDescriptor(
					entityBp,
					createEntities,
					true,
					personRelationType.getId(),
					personRelationType.getRelationName(),
					personRelationType.getRelationName());
		}

		addPersonsOfSourcePersonsLinkDescriptor(entityBp);
		addSourcePersonsOfPersonsLinkDescriptor(entityBp);
	}

	private void addRoleLinkDescriptors(final IBeanEntityBluePrint entityBp) {
		addRolePersonLinkDescriptor(entityBp);
		addRoleAuthorizationLinkDescriptor(entityBp);
	}

	private void addPersonRoleLinkDescriptor(final IBeanEntityBluePrint entityBp) {
		final IBeanEntityLinkBluePrint bp = entityBp.addLink();
		bp.setLinkEntityId(EntityIds.PERSON_ROLE_LINK);
		bp.setLinkBeanType(PersonRoleLink.class);
		bp.setLinkedEntityId(EntityIds.LINKED_ROLES_OF_PERSONS);
		bp.setLinkableEntityId(EntityIds.LINKABLE_ROLES_OF_PERSONS);
		bp.setSourceProperties(IPersonRoleLink.PERSON_ID_PROPERTY);
		bp.setDestinationProperties(IPersonRoleLink.ROLE_ID_PROPERTY);
	}

	private void addPersonsOfSourcePersonsLinkDescriptor(final IBeanEntityBluePrint entityBp) {
		final IBeanEntityLinkBluePrint bp = entityBp.addLink();
		bp.setLinkEntityId(EntityIds.PERSONS_OF_SOURCE_PERSONS_LINK);
		bp.setLinkBeanType(PersonPersonLink.class);
		bp.setLinkedEntityId(EntityIds.LINKED_PERSONS_OF_SOURCE_PERSONS);
		bp.setLinkableEntityId(EntityIds.LINKABLE_PERSONS_OF_PERSONS);
		bp.setSourceProperties(IPersonPersonLink.SOURCE_PERSON_ID_PROPERTY);
		bp.setDestinationProperties(IPersonPersonLink.DESTINATION_PERSON_ID_PROPERTY);
	}

	private void addSourcePersonsOfPersonsLinkDescriptor(final IBeanEntityBluePrint entityBp) {
		final IBeanEntityLinkBluePrint bp = entityBp.addLink();
		bp.setLinkEntityId(EntityIds.SOURCE_PERSONS_OF_PERSONS_LINK);
		bp.setLinkBeanType(PersonPersonLink.class);
		bp.setLinkedEntityId(EntityIds.LINKED_SOURCE_PERSONS_OF_PERSONS);
		bp.setLinkableEntityId(EntityIds.LINKABLE_PERSONS_OF_PERSONS);
		bp.setSourceProperties(IPersonPersonLink.DESTINATION_PERSON_ID_PROPERTY);
		bp.setDestinationProperties(IPersonPersonLink.SOURCE_PERSON_ID_PROPERTY);
	}

	private void addPersonPersonLinkDescriptor(
		final IBeanEntityBluePrint entityBp,
		final boolean createEntities,
		final boolean reverse,
		final Object relationTypeId,
		final String labelSingular,
		final String labelPlural) {
		final IBeanEntityLinkBluePrint link = entityBp.addLink();

		final String linkEntityId = "PERSON_PERSON_LINK_" + relationTypeId + "_" + reverse;
		final String linkedEntityId = "PERSON_PERSON_LINKED_" + relationTypeId + "_" + reverse;
		final String linkableEntityId = "PERSON_PERSON_LINKABLE_" + relationTypeId + "_" + reverse;

		link.setLinkEntityId(linkEntityId);
		link.setLinkBeanType(PersonPersonLink.class);
		link.setLinkedEntityId(linkedEntityId);
		link.setLinkableEntityId(linkableEntityId);

		if (reverse) {
			link.setSourceProperties(IPersonPersonLink.DESTINATION_PERSON_ID_PROPERTY);
			link.setDestinationProperties(IPersonPersonLink.SOURCE_PERSON_ID_PROPERTY);
		}
		else {
			link.setSourceProperties(IPersonPersonLink.SOURCE_PERSON_ID_PROPERTY);
			link.setDestinationProperties(IPersonPersonLink.DESTINATION_PERSON_ID_PROPERTY);
		}

		if (createEntities) {

			final String personPersonLinkAttribute;
			final String personAttribute;
			if (reverse) {
				personPersonLinkAttribute = "sourcePersonOfPersonLinks";
				personAttribute = "destinationPerson";
			}
			else {
				personPersonLinkAttribute = "personOfSourcePersonLinks";
				personAttribute = "sourcePerson";
			}

			final IBeanEntityBluePrint linkEntityBp = addEntity();
			linkEntityBp.setEntityId(linkEntityId).setBeanType(PersonPersonLink.class);
			linkEntityBp.setDtoDescriptor(new PersonPersonLinkDtoDescriptorBuilder(
				relationTypeId,
				labelSingular + " link",
				labelPlural + " links"));

			final IPredicateCreator<Void> relationTypePredicateCreator = new IPredicateCreator<Void>() {
				@Override
				public Predicate createPredicate(
					final CriteriaBuilder criteriaBuilder,
					final Root<?> bean,
					final CriteriaQuery<?> query,
					final List<IBeanKey> parentBeanKeys,
					final List<Object> parentBeanIds,
					final Void parameter) {
					final Join<Object, Object> linkPath = bean.join(personPersonLinkAttribute);
					final Path<Object> relationTypePath = linkPath.get("relationType").get(IBean.ID_PROPERTY);
					return relationTypePath.in(relationTypeId);
				}
			};

			final ICriteriaQueryCreatorBuilder<Void> linkedQueryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Person.class);
			linkedQueryBuilder.setParentPropertyPath(true, personPersonLinkAttribute, personAttribute);
			linkedQueryBuilder.addPredicateCreator(relationTypePredicateCreator);
			final IReaderService<Void> linkedReader = getServiceFactory().readerService(
					Person.class,
					linkedQueryBuilder.build(),
					IPerson.ALL_PROPERTIES);

			final IBeanEntityBluePrint linkedEntityBp = addEntity().setEntityId(linkedEntityId).setBeanType(Person.class);
			linkedEntityBp.setDtoDescriptor(new PersonDtoDescriptorBuilder(labelSingular, labelPlural));
			linkedEntityBp.setReaderService(linkedReader);
			linkedEntityBp.setUpdaterService(createPersonUpdaterService());
			linkedEntityBp.setCreatorService((ICreatorService) null);
			addPersonLinkDescriptors(linkedEntityBp, false);

			final ICriteriaQueryCreatorBuilder<Void> linakbleQueryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Person.class);
			linakbleQueryBuilder.setParentPropertyPath(false, personPersonLinkAttribute, personAttribute);
			linkedQueryBuilder.addPredicateCreator(relationTypePredicateCreator);
			final IReaderService<Void> linkableReader = getServiceFactory().readerService(
					Person.class,
					linakbleQueryBuilder.build(),
					IPerson.ALL_PROPERTIES);

			final IBeanEntityBluePrint linkableEntityBp = addEntity().setEntityId(linkableEntityId).setBeanType(Person.class);
			linkableEntityBp.setReaderService(linkableReader);
			linkableEntityBp.setDtoDescriptor(new PersonDtoDescriptorBuilder());
		}
	}

	private void addRolePersonLinkDescriptor(final IBeanEntityBluePrint entityBp) {
		final IBeanEntityLinkBluePrint bp = entityBp.addLink();
		bp.setLinkEntityId(EntityIds.PERSON_ROLE_LINK);
		bp.setLinkBeanType(PersonRoleLink.class);
		bp.setLinkedEntityId(EntityIds.LINKED_PERSONS_OF_ROLES);
		bp.setLinkableEntityId(EntityIds.LINKABLE_PERSONS_OF_ROLES);
		bp.setSourceProperties(IPersonRoleLink.ROLE_ID_PROPERTY);
		bp.setDestinationProperties(IPersonRoleLink.PERSON_ID_PROPERTY);
	}

	private void addRoleAuthorizationLinkDescriptor(final IBeanEntityBluePrint entityBp) {
		final IBeanEntityLinkBluePrint bp = entityBp.addLink();
		bp.setLinkEntityId(EntityIds.ROLE_AUTHORIZATION_LINK);
		bp.setLinkBeanType(RoleAuthorizationLink.class);
		bp.setLinkedEntityId(EntityIds.LINKED_AUTHORIZATION_OF_ROLES);
		bp.setLinkableEntityId(EntityIds.LINKABLE_AUTHORIZATIONS_OF_ROLES);
		bp.setSourceProperties(IRoleAuthorizationLink.ROLE_ID_PROPERTY);
		bp.setDestinationProperties(IRoleAuthorizationLink.AUTHORIZATION_ID_PROPERTY);
	}

	private void addAuthorizationRoleLinkDescriptor(final IBeanEntityBluePrint entityBp) {
		final IBeanEntityLinkBluePrint bp = entityBp.addLink();
		bp.setLinkEntityId(EntityIds.ROLE_AUTHORIZATION_LINK);
		bp.setLinkBeanType(RoleAuthorizationLink.class);
		bp.setLinkedEntityId(EntityIds.LINKED_ROLES_OF_AUTHORIZATIONS);
		bp.setLinkableEntityId(EntityIds.LINKABLE_ROLES_OF_AUTHORIZATIONS);
		bp.setSourceProperties(IRoleAuthorizationLink.AUTHORIZATION_ID_PROPERTY);
		bp.setDestinationProperties(IRoleAuthorizationLink.ROLE_ID_PROPERTY);
	}

	private IReaderService<Void> createLinkedPersonsOfSourcePersonsReader() {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(PersonPersonLink.class);
		queryBuilder.setParentPropertyPath("sourcePerson");
		return getServiceFactory().readerService(
				PersonPersonLink.class,
				queryBuilder.build(),
				IPersonPersonLink.PERSONS_OF_SOURCE_PERSONS_PROPERTIES);
	}

	private IReaderService<Void> createLinkedSourcePersonsOfPersonsReader() {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(PersonPersonLink.class);
		queryBuilder.setParentPropertyPath("destinationPerson");
		return getServiceFactory().readerService(
				PersonPersonLink.class,
				queryBuilder.build(),
				IPersonPersonLink.SOURCE_PERSONS_OF_PERSONS_PROPERTIES);
	}

	private IReaderService<Void> createPersonsOfRolesReader(final boolean linked) {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Person.class);
		queryBuilder.setParentPropertyPath(linked, "personRoleLinks", "role");
		if (!linked) {
			final IFilter filter = ArithmeticFilter.create(IPerson.ACTIVE_PROPERTY, ArithmeticOperator.EQUAL, Boolean.TRUE);
			queryBuilder.addFilter(filter);
		}
		return getServiceFactory().readerService(Person.class, queryBuilder.build(), IPerson.ALL_PROPERTIES);
	}

	private IReaderService<Void> createRolesOfPersonsReader(final boolean linked) {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Role.class);
		queryBuilder.setParentPropertyPath(linked, "personRoleLinks", "person");
		return getServiceFactory().readerService(Role.class, queryBuilder.build(), IRole.ALL_PROPERTIES);
	}

	private IReaderService<Void> createRolesOfAuthorizationsReader(final boolean linked) {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Role.class);
		queryBuilder.setParentPropertyPath(linked, "roleAuthorizationLinks", "authorization");
		return getServiceFactory().readerService(Role.class, queryBuilder.build(), IRole.ALL_PROPERTIES);
	}

	private IReaderService<Void> createAuthorizationsOfRolesReader(final boolean linked) {
		final ICriteriaQueryCreatorBuilder<Void> queryBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(Authorization.class);
		queryBuilder.setParentPropertyPath(linked, "roleAuthorizationLinks", "role");
		return getServiceFactory().readerService(Authorization.class, queryBuilder.build(), IAuthorization.ALL_PROPERTIES);
	}

	private IUpdaterService createPersonUpdaterService() {
		final IUpdaterServiceBuilder<Person> builder = getServiceFactory().updaterServiceBuilder(Person.class);
		builder.setBeanDtoFactoryAndBeanModifier(IPerson.ALL_PROPERTIES);
		builder.addBeanValidator(new PersonLoginNameConstraintValidator());
		return builder.build();
	}

	private ICreatorService createPersonCreatorService() {
		final ICreatorServiceBuilder<Person> builder = getServiceFactory().creatorServiceBuilder(Person.class);
		builder.setBeanDtoFactoryAndBeanInitializer(IPerson.ALL_PROPERTIES);
		builder.addBeanValidator(new PersonLoginNameConstraintValidator());
		return builder.build();
	}
}
