/*
 * Copyright (c) 2011, riegen
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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.jowidgets.cap.sample2.app.common.bean.IPhone;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"phone"}))
public class Phone extends Bean implements IPhone {

	@Basic
	private String phone;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PERSONID", nullable = false, insertable = false, updatable = false)
	private Person person;

	@Column(name = "PERSONID", nullable = true)
	private Long personId;

	@Override
	public String getPhone() {
		return phone;
	}

	@Override
	public void setPhone(final String phone) {
		this.phone = phone;
	}

	@Override
	public String getPersonLabel() {
		final Person currentPerson = getPerson();
		if (currentPerson != null) {
			return currentPerson.getName() + " " + currentPerson.getLastname();
		}
		else {
			return null;
		}
	}

	@Override
	public Long getPersonId() {
		return personId;
	}

	@Override
	public void setPersonId(final Long id) {
		this.personId = id;
		this.person = null;
	}

	public Person getPerson() {
		if (person == null && personId != null) {
			person = EntityManagerProvider.get().find(Person.class, personId);
		}
		return person;
	}

	public void setPerson(final Person person) {
		this.person = person;
		personId = person != null ? person.getId() : null;
	}
}
