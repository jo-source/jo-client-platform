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

package org.jowidgets.cap.sample2.app.common.entity;

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.sample2.app.common.security.AuthorizationKeys;
import org.jowidgets.cap.service.security.api.CrudServiceType;
import org.jowidgets.cap.service.security.api.ISecureEntityId;

public enum EntityIds implements ISecureEntityId<String> {

	PERSON(
		AuthorizationKeys.CREATE_PERSON,
		AuthorizationKeys.READ_PERSON,
		AuthorizationKeys.UPDATE_PERSON,
		AuthorizationKeys.DELETE_PERSON),

	ROLE(AuthorizationKeys.CREATE_ROLE, AuthorizationKeys.READ_ROLE, AuthorizationKeys.UPDATE_ROLE, AuthorizationKeys.DELETE_ROLE),

	COUNTRY(
		AuthorizationKeys.CREATE_COUNTRY,
		AuthorizationKeys.READ_COUNTRY,
		AuthorizationKeys.UPDATE_COUNTRY,
		AuthorizationKeys.DELETE_COUNTRY),

	PERSON_LINK_TYPE(
		AuthorizationKeys.CREATE_PERSON_LINK_TYPE,
		AuthorizationKeys.READ_PERSON_LINK_TYPE,
		AuthorizationKeys.UPDATE_PERSON_LINK_TYPE,
		AuthorizationKeys.DELETE_PERSON_LINK_TYPE),

	AUTHORIZATION(
		AuthorizationKeys.CREATE_AUTHORIZATION,
		AuthorizationKeys.READ_AUTHORIZATION,
		AuthorizationKeys.UPDATE_AUTHORIZATION,
		AuthorizationKeys.DELETE_AUTHORIZATION),

	ROLE_AUTHORIZATION_LINK(AuthorizationKeys.ADMIN_MISC),
	PERSON_ROLE_LINK(AuthorizationKeys.ADMIN_MISC),
	PERSONS_OF_SOURCE_PERSONS_LINK(AuthorizationKeys.ADMIN_MISC),
	SOURCE_PERSONS_OF_PERSONS_LINK(AuthorizationKeys.ADMIN_MISC),
	LINKED_PERSONS_OF_SOURCE_PERSONS(AuthorizationKeys.ADMIN_MISC),
	LINKED_SOURCE_PERSONS_OF_PERSONS(AuthorizationKeys.ADMIN_MISC),
	LINKABLE_PERSONS_OF_PERSONS(AuthorizationKeys.ADMIN_MISC),
	LINKED_ROLES_OF_PERSONS(AuthorizationKeys.ADMIN_MISC),
	LINKABLE_ROLES_OF_PERSONS(AuthorizationKeys.ADMIN_MISC),
	LINKED_PERSONS_OF_ROLES(AuthorizationKeys.ADMIN_MISC),
	LINKABLE_PERSONS_OF_ROLES(AuthorizationKeys.ADMIN_MISC),
	LINKED_AUTHORIZATION_OF_ROLES(AuthorizationKeys.ADMIN_MISC),
	LINKABLE_AUTHORIZATIONS_OF_ROLES(AuthorizationKeys.ADMIN_MISC),
	LINKED_ROLES_OF_AUTHORIZATIONS(AuthorizationKeys.ADMIN_MISC),
	LINKABLE_ROLES_OF_AUTHORIZATIONS(AuthorizationKeys.ADMIN_MISC);

	private final Map<CrudServiceType, String> authorization;

	private EntityIds(final String all) {
		this(all, all, all, all);
	}

	private EntityIds(final String create, final String read, final String update, final String delete) {
		this.authorization = new HashMap<CrudServiceType, String>();
		authorization.put(CrudServiceType.CREATE, create);
		authorization.put(CrudServiceType.READ, read);
		authorization.put(CrudServiceType.UPDATE, update);
		authorization.put(CrudServiceType.DELETE, delete);
	}

	@Override
	public String getAuthorization(final CrudServiceType serviceType) {
		return authorization.get(serviceType);
	}

}
