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

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.jowidgets.cap.sample2.app.common.bean.IPersonRoleLink;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;

@Entity
@Table(name = "PERSON_ROLE_LINK")
public class PersonRoleLink extends Bean implements IPersonRoleLink {

	private Person person;
	private Role role;

	@ManyToOne()
	@JoinColumn(name = "PERSON_ID", nullable = false)
	public Person getPerson() {
		return person;
	}

	public void setPerson(final Person person) {
		this.person = person;
	}

	@ManyToOne()
	@JoinColumn(name = "ROLE_ID", nullable = false)
	public Role getRole() {
		return role;
	}

	public void setRole(final Role role) {
		this.role = role;
	}

	@Transient
	@Override
	public Long getPersonId() {
		if (person != null) {
			return person.getId();
		}
		else {
			return null;
		}
	}

	@Override
	public void setPersonId(final Long id) {
		if (id != null) {
			final EntityManager em = EntityManagerProvider.get();
			final Person found = em.find(Person.class, id);
			if (found != null) {
				setPerson(found);
			}
			else {
				throw new IllegalArgumentException("Can not find person with the id '" + id + "'");
			}
		}
		else {
			setPerson(null);
		}
	}

	@Transient
	@Override
	public Long getRoleId() {
		if (role != null) {
			return role.getId();
		}
		else {
			return null;
		}
	}

	@Override
	public void setRoleId(final Long id) {
		if (id != null) {
			final EntityManager em = EntityManagerProvider.get();
			final Role found = em.find(Role.class, id);
			if (found != null) {
				setRole(found);
			}
			else {
				throw new IllegalArgumentException("Can not find role with the id '" + id + "'");
			}
		}
		else {
			setRole(null);
		}
	}

}
