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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.jowidgets.cap.sample2.app.common.bean.IPersonPersonLink;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;

@Entity
@Table(name = "PERSON_PERSON_LINK")
public class PersonPersonLink extends Bean implements IPersonPersonLink {

	@Basic()
	@Column(name = "LINK_COMMENT")
	private String comment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_PERSON_ID", nullable = false, insertable = false, updatable = false)
	private Person sourcePerson;

	@Column(name = "SOURCE_PERSON_ID", nullable = false)
	private Long sourcePersonId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DESTINATION_PERSON_ID", nullable = false, insertable = false, updatable = false)
	private Person destinationPerson;

	@Column(name = "DESTINATION_PERSON_ID", nullable = false)
	private Long destinationPersonId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RELATION_TYPE_ID", nullable = false, insertable = false, updatable = false)
	private PersonRelationType relationType;

	@Column(name = "RELATION_TYPE_ID", nullable = false)
	private Long relationTypeId;

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(final String comment) {
		this.comment = comment;
	}

	public Person getSourcePerson() {
		if (sourcePerson == null && sourcePersonId != null) {
			sourcePerson = EntityManagerProvider.get().find(Person.class, sourcePersonId);
		}
		return sourcePerson;
	}

	public void setSourcePerson(final Person person) {
		this.sourcePerson = person;
		sourcePersonId = person != null ? person.getId() : null;
	}

	@Override
	public Long getSourcePersonId() {
		return sourcePersonId;
	}

	@Override
	public void setSourcePersonId(final Long id) {
		this.sourcePersonId = id;
		this.sourcePerson = null;
	}

	public Person getDestinationPerson() {
		if (destinationPerson == null && destinationPersonId != null) {
			destinationPerson = EntityManagerProvider.get().find(Person.class, destinationPersonId);
		}
		return destinationPerson;
	}

	public void setDestinationPerson(final Person person) {
		this.destinationPerson = person;
		destinationPersonId = person != null ? person.getId() : null;
	}

	@Override
	public Long getDestinationPersonId() {
		return destinationPersonId;
	}

	@Override
	public void setDestinationPersonId(final Long id) {
		this.destinationPersonId = id;
		this.destinationPerson = null;
	}

	public PersonRelationType getRelationType() {
		if (relationType == null && relationTypeId != null) {
			relationType = EntityManagerProvider.get().find(PersonRelationType.class, relationTypeId);
		}
		return relationType;
	}

	public void setRelationType(final PersonRelationType relationType) {
		this.relationType = relationType;
		relationTypeId = relationType != null ? relationType.getId() : null;
	}

	@Override
	public Long getRelationTypeId() {
		return relationTypeId;
	}

	@Override
	public void setRelationTypeId(final Long id) {
		this.relationTypeId = id;
		this.relationType = null;
	}
}
