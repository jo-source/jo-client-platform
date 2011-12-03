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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.jowidgets.cap.sample2.app.common.bean.IPersonRoleLink;

@Entity
@Table(name = "PERSON_ROLE_LINK")
public class PersonRoleLink extends Bean implements IPersonRoleLink {

	@ManyToOne()
	@JoinColumn(name = "PERSON_ID", nullable = false, insertable = false, updatable = false)
	private Person person;

	@Column(name = "PERSON_ID", nullable = false)
	private Long personId;

	@ManyToOne()
	@JoinColumn(name = "ROLE_ID", nullable = false, insertable = false, updatable = false)
	private Role role;

	@Column(name = "ROLE_ID", nullable = false)
	private Long roleId;

	@Override
	public Person getPerson() {
		return person;
	}

	public void setPerson(final Person person) {
		this.person = person;
		personId = person != null ? person.getId() : null;
	}

	@Override
	public Role getRole() {
		return role;
	}

	public void setRole(final Role role) {
		this.role = role;
		roleId = role != null ? role.getId() : null;
	}

	@Override
	public Long getPersonId() {
		return personId;
	}

	@Override
	public void setPersonId(final Long id) {
		this.personId = id;
	}

	@Override
	public Long getRoleId() {
		return roleId;
	}

	@Override
	public void setRoleId(final Long id) {
		this.roleId = id;
	}

}
