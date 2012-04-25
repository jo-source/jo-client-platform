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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.jowidgets.cap.sample2.app.common.bean.IAuthorization;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"key"}))
public class Authorization extends Bean implements IAuthorization {

	@Basic
	@Index(name = "KeyIndex")
	private String key;

	@Basic
	private String description;

	@OneToMany(mappedBy = "authorization")
	private List<RoleAuthorizationLink> roleAuthorizationLinks = new LinkedList<RoleAuthorizationLink>();

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void setKey(final String key) {
		this.key = key;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	public List<RoleAuthorizationLink> getPersonRoleLinks() {
		return roleAuthorizationLinks;
	}

	public void setPersonRoleLinks(final List<RoleAuthorizationLink> roleAuthorizationLinks) {
		this.roleAuthorizationLinks = roleAuthorizationLinks;
	}

	@Override
	public boolean getInUse() {
		if (getId() != null) {
			final EntityManager entityManager = EntityManagerProvider.get();
			final TypedQuery<RoleAuthorizationLink> query = entityManager.createQuery(
					"SELECT link FROM RoleAuthorizationLink link WHERE link.authorization = :authorization",
					RoleAuthorizationLink.class);
			query.setParameter("authorization", this);
			query.setFirstResult(0).setMaxResults(1);
			return query.getResultList().size() > 0;
		}
		else {
			return false;
		}
	}
}
