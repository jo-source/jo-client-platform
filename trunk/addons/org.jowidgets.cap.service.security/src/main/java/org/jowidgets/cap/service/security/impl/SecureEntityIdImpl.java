/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.service.security.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.service.security.api.CrudServiceType;
import org.jowidgets.cap.service.security.api.ISecureEntityId;
import org.jowidgets.util.Assert;

final class SecureEntityIdImpl<AUTHORIZATION_TYPE> implements ISecureEntityId<AUTHORIZATION_TYPE>, Serializable {

	private static final long serialVersionUID = -1844266122522882979L;

	private final Object id;
	private final Map<CrudServiceType, AUTHORIZATION_TYPE> authorizations;

	SecureEntityIdImpl(
		final Object id,
		final AUTHORIZATION_TYPE create,
		final AUTHORIZATION_TYPE read,
		final AUTHORIZATION_TYPE update,
		final AUTHORIZATION_TYPE delete) {
		Assert.paramNotNull(id, "id");
		this.id = id;
		this.authorizations = new HashMap<CrudServiceType, AUTHORIZATION_TYPE>();
		authorizations.put(CrudServiceType.CREATE, create);
		authorizations.put(CrudServiceType.READ, read);
		authorizations.put(CrudServiceType.UPDATE, update);
		authorizations.put(CrudServiceType.DELETE, delete);
	}

	@Override
	public AUTHORIZATION_TYPE getAuthorization(final CrudServiceType serviceType) {
		return authorizations.get(serviceType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SecureEntityIdImpl)) {
			return false;
		}
		final SecureEntityIdImpl<AUTHORIZATION_TYPE> other = (SecureEntityIdImpl<AUTHORIZATION_TYPE>) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SecureEntityIdImpl [id=" + id + ", authorizations=" + authorizations + "]";
	}

}
