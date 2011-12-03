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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Index;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"loginName"}))
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

	@ManyToOne()
	@JoinColumn(name = "COUNTRY_ID", nullable = true, insertable = false, updatable = false)
	private Country country;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "person")
	@BatchSize(size = 1000)
	private List<PersonRoleLink> personRoleLinks = new LinkedList<PersonRoleLink>();

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

	public List<PersonRoleLink> getPersonRoleLinks() {
		return personRoleLinks;
	}

	public void setPersonRoleLinks(final List<PersonRoleLink> personRoleLinks) {
		this.personRoleLinks = personRoleLinks;
	}

	public Country getCountry() {
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
	}

	@Override
	public List<Long> getRoleIds() {
		final List<Long> result = new LinkedList<Long>();
		for (final PersonRoleLink personRoleLink : getPersonRoleLinks()) {
			result.add(personRoleLink.getRole().getId());
		}
		return result;
	}

	@Override
	public void setRoleIds(List<Long> newRoleIds) {
		final EntityManager em = EntityManagerProvider.get();

		if (newRoleIds == null) {
			newRoleIds = new LinkedList<Long>();
		}

		final Set<Long> newRoleIdsSet = new LinkedHashSet<Long>(newRoleIds);

		final List<PersonRoleLink> currentLinks = getPersonRoleLinks();
		final Map<Long, PersonRoleLink> currentLinksMap = new HashMap<Long, PersonRoleLink>();
		for (final PersonRoleLink personRoleLink : new LinkedList<PersonRoleLink>(currentLinks)) {
			final Long roleId = personRoleLink.getRole().getId();
			if (newRoleIdsSet.remove(roleId)) {
				currentLinksMap.put(roleId, personRoleLink);
			}
			else {
				currentLinks.remove(personRoleLink);
				em.remove(personRoleLink);
			}
		}

		final List<PersonRoleLink> newLinks = new LinkedList<PersonRoleLink>();

		for (final Long newId : newRoleIds) {
			final PersonRoleLink link = currentLinksMap.get(newId);
			if (link != null) {
				newLinks.add(link);
			}
			else {
				final PersonRoleLink newLink = new PersonRoleLink();
				if (getId() == null) {
					em.persist(this);
				}
				newLink.setPerson(this);
				final Role newRole = em.find(Role.class, newId);
				if (newRole != null) {
					newLink.setRole(newRole);
				}
				else {
					throw new IllegalArgumentException("Can not find role with the id '" + newId + "'");
				}
				em.persist(newLink);
				newLinks.add(newLink);
			}
		}

		setPersonRoleLinks(newLinks);
	}
}
