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
package org.jowidgets.cap.sample2.app.service.bean;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Index;
import org.jowidgets.cap.common.api.annotation.BeanValidator;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;
import org.jowidgets.cap.sample2.app.service.validation.PersonLoginNameConstraintValidator;
import org.jowidgets.cap.service.jpa.api.query.QueryPath;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"loginName"}))
@BeanValidator(PersonLoginNameConstraintValidator.class)
public class Person extends Bean implements IPerson {

	@Basic
	@Index(name = "PersonNameIndex")
	private String name;

	@Basic
	@Index(name = "PersonLastnameIndex")
	private String lastname;

	@Basic
	private String loginName;

	@Basic
	private String gender;

	@Basic
	private Boolean active;

	@Column(name = "COUNTRY_ID", nullable = true)
	private Long countryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COUNTRY_ID", nullable = true, insertable = false, updatable = false)
	private Country country;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "person")
	@BatchSize(size = 1000)
	@MapKey(name = "roleId")
	@OrderBy("roleId ASC")
	private Map<Long, PersonRoleLink> personRoleLinks = new LinkedHashMap<Long, PersonRoleLink>();

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "destinationPerson")
	@BatchSize(size = 1000)
	private List<PersonPersonLink> personOfSourcePersonLinks;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "sourcePerson")
	@BatchSize(size = 1000)
	private List<PersonPersonLink> sourcePersonOfPersonLinks;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "person")
	@BatchSize(size = 1000)
	@MapKey(name = "PERSONID")
	private List<Phone> phones;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getLastname() {
		return lastname;
	}

	@Override
	public void setLastname(final String lastname) {
		this.lastname = lastname;
	}

	@Override
	public String getLoginName() {
		return loginName;
	}

	@Override
	public void setLoginName(final String loginName) {
		this.loginName = loginName;
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public void setGender(final String gender) {
		this.gender = gender;
	}

	@Override
	public Boolean getActive() {
		return active;
	}

	@Override
	public void setActive(final Boolean active) {
		this.active = active;
	}

	public Country getCountry() {
		if (country == null && countryId != null) {
			country = EntityManagerProvider.get().find(Country.class, countryId);
		}
		return country;
	}

	public void setCountry(final Country country) {
		this.country = country;
		countryId = country != null ? country.getId() : null;
	}

	@Override
	public Long getCountryId() {
		return countryId;
	}

	@Override
	public void setCountryId(final Long id) {
		this.countryId = id;
		this.country = null;
	}

	public Map<Long, PersonRoleLink> getPersonRoleLinks() {
		return personRoleLinks;
	}

	public void setPersonRoleLinks(final Map<Long, PersonRoleLink> personRoleLinks) {
		this.personRoleLinks = personRoleLinks;
	}

	public List<PersonPersonLink> getPersonOfSourcePersonLinks() {
		return personOfSourcePersonLinks;
	}

	public void setPersonOfSourcePersonLinks(final List<PersonPersonLink> personOfSourcePersonLinks) {
		this.personOfSourcePersonLinks = personOfSourcePersonLinks;
	}

	public List<PersonPersonLink> getSourcePersonOfPersonLinks() {
		return sourcePersonOfPersonLinks;
	}

	public void setSourcePersonOfPersonLinks(final List<PersonPersonLink> sourcePersonOfPersonLinks) {
		this.sourcePersonOfPersonLinks = sourcePersonOfPersonLinks;
	}

	@Override
	@QueryPath(path = {"personRoleLinks", "roleId"})
	public List<Long> getRoleIds() {
		return new LinkedList<Long>(personRoleLinks.keySet());
	}

	@Override
	public void setRoleIds(List<Long> newRoleIds) {
		if (newRoleIds == null) {
			newRoleIds = new LinkedList<Long>();
		}
		final Set<Long> newRoleIdsSet = new LinkedHashSet<Long>(newRoleIds);
		final Map<Long, PersonRoleLink> newPersonRoleLinks = new LinkedHashMap<Long, PersonRoleLink>();
		final EntityManager em = EntityManagerProvider.get();

		for (final Entry<Long, PersonRoleLink> entry : personRoleLinks.entrySet()) {
			if (!newRoleIdsSet.contains(entry.getKey())) {
				em.remove(entry.getValue());
			}
		}

		for (final Long newId : newRoleIds) {
			PersonRoleLink personRoleLink = personRoleLinks.get(newId);
			if (personRoleLink == null) {
				personRoleLink = new PersonRoleLink();
				if (getId() == null) {
					em.persist(this);
				}
				personRoleLink.setPersonId(getId());
				personRoleLink.setRoleId(newId);
				em.persist(personRoleLink);
			}
			newPersonRoleLinks.put(newId, personRoleLink);
		}

		personRoleLinks = newPersonRoleLinks;
	}

	public List<Phone> getPhones() {
		return phones;
	}

	public void setPhones(final List<Phone> phones) {
		this.phones = phones;
	}
}
