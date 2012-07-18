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

package org.jowidgets.security.tools;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jowidgets.security.api.IDefaultPrincipal;

public final class DefaultPrincipal implements IDefaultPrincipal<String>, Serializable {

	private static final long serialVersionUID = 7568678347480658843L;

	private final String username;
	private final Set<String> grantedAuthorities = new HashSet<String>();

	public DefaultPrincipal(final String username) {
		this.username = username;
	}

	public DefaultPrincipal(final String username, final Collection<String> grantedAuthorities) {
		this(username);
		this.grantedAuthorities.addAll(grantedAuthorities);
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public Set<String> getGrantedAuthorities() {
		return Collections.unmodifiableSet(grantedAuthorities);
	}

	@Override
	public String toString() {
		return "DefaultPrincipal [username=" + username + ", grantedAuthorities=" + grantedAuthorities + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((grantedAuthorities == null) ? 0 : grantedAuthorities.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DefaultPrincipal)) {
			return false;
		}
		final DefaultPrincipal other = (DefaultPrincipal) obj;
		if (grantedAuthorities == null) {
			if (other.grantedAuthorities != null) {
				return false;
			}
		}
		else if (!grantedAuthorities.equals(other.grantedAuthorities)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		}
		else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

}
