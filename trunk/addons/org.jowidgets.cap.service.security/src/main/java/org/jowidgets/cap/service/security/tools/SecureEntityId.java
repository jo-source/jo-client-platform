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

package org.jowidgets.cap.service.security.tools;

import java.io.Serializable;

import org.jowidgets.cap.service.security.api.CrudServiceType;
import org.jowidgets.cap.service.security.api.ISecureEntityId;
import org.jowidgets.cap.service.security.api.SecureServiceToolkit;

public class SecureEntityId<AUTHORIZATION_TYPE> implements ISecureEntityId<AUTHORIZATION_TYPE>, Serializable {

	private static final long serialVersionUID = 163271039311211943L;

	private final ISecureEntityId<AUTHORIZATION_TYPE> original;

	public SecureEntityId(
		final Object id,
		final AUTHORIZATION_TYPE create,
		final AUTHORIZATION_TYPE read,
		final AUTHORIZATION_TYPE update,
		final AUTHORIZATION_TYPE delete) {
		this(SecureServiceToolkit.entityId(id, create, read, update, delete));
	}

	private SecureEntityId(final ISecureEntityId<AUTHORIZATION_TYPE> original) {
		this.original = original;
	}

	@Override
	public AUTHORIZATION_TYPE getAuthorization(final CrudServiceType serviceType) {
		return original.getAuthorization(serviceType);
	}

	@Override
	public final int hashCode() {
		return original.hashCode();
	}

	@Override
	public final boolean equals(final Object obj) {
		return original.equals(obj);
	}

	@Override
	public final String toString() {
		return original.toString();
	}

}
