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

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Index;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.service.jpa.api.EntityManagerHolder;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"loginName"}))
public class Person extends Bean implements IPerson {

	private String name;
	private String lastname;
	private String loginName;
	private List<PersonRoleLink> setOfPersonRoleLink = new LinkedList<PersonRoleLink>();

	@Index(name = "PersonNameIndex")
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Index(name = "PersonLastnameIndex")
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
	@BatchSize(size = 1000)
	public List<PersonRoleLink> getSetOfPersonRoleLink() {
		return setOfPersonRoleLink;
	}

	public void setSetOfPersonRoleLink(final List<PersonRoleLink> setOfPersonRoleLink) {
		this.setOfPersonRoleLink = setOfPersonRoleLink;
	}

	@Override
	@Transient
	public List<Long> getRoleIds() {
		final List<Long> result = new LinkedList<Long>();
		for (final PersonRoleLink personRoleLink : getSetOfPersonRoleLink()) {
			result.add(personRoleLink.getRole().getId());
		}
		return result;
	}

	@Override
	public void setRoleIds(List<Long> newRoleIds) {
		final EntityManager em = EntityManagerHolder.get();

		if (newRoleIds == null) {
			newRoleIds = new LinkedList<Long>();
		}

		final Set<Long> newRoleIdsSet = new LinkedHashSet<Long>(newRoleIds);

		final List<PersonRoleLink> currentLinks = getSetOfPersonRoleLink();
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

		setSetOfPersonRoleLink(newLinks);
	}
}
