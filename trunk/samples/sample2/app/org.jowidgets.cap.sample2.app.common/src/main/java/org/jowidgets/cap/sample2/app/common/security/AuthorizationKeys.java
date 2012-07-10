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

package org.jowidgets.cap.sample2.app.common.security;

import java.util.Collection;
import java.util.LinkedList;

public final class AuthorizationKeys {

	public static final String ADMIN_MISC = "ADMIN_MISC";

	public static final String EXECUTOR_ACTIVATE_PERSON = "EXECUTOR_ACTIVATE_PERSON";
	public static final String EXECUTOR_DEACTIVATE_PERSON = "EXECUTOR_DEACTIVATE_PERSON";

	public static final String CREATE_PERSON = "CREATE_PERSON";
	public static final String READ_PERSON = "READ_PERSON";
	public static final String UPDATE_PERSON = "UPDATE_PERSON";
	public static final String DELETE_PERSON = "DELETE_PERSON";

	public static final String CREATE_ROLE = "CREATE_ROLE";
	public static final String READ_ROLE = "READ_ROLE";
	public static final String UPDATE_ROLE = "UPDATE_ROLE";
	public static final String DELETE_ROLE = "DELETE_ROLE";

	public static final String CREATE_COUNTRY = "CREATE_COUNTRY";
	public static final String READ_COUNTRY = "READ_COUNTRY";
	public static final String UPDATE_COUNTRY = "UPDATE_COUNTRY";
	public static final String DELETE_COUNTRY = "DELETE_COUNTRY";

	public static final String CREATE_PERSON_LINK_TYPE = "CREATE_PERSON_LINK_TYPE";
	public static final String READ_PERSON_LINK_TYPE = "READ_PERSON_LINK_TYPE";
	public static final String UPDATE_PERSON_LINK_TYPE = "UPDATE_PERSON_LINK_TYPE";
	public static final String DELETE_PERSON_LINK_TYPE = "DELETE_PERSON_LINK_TYPE";

	public static final String CREATE_AUTHORIZATION = "CREATE_AUTHORIZATION";
	public static final String READ_AUTHORIZATION = "READ_AUTHORIZATION";
	public static final String UPDATE_AUTHORIZATION = "UPDATE_AUTHORIZATION";
	public static final String DELETE_AUTHORIZATION = "DELETE_AUTHORIZATION";

	public static final Collection<String> ALL_AUTHORIZATIONS = new LinkedList<String>() {
		private static final long serialVersionUID = 1404650177379798436L;
		{
			add(ADMIN_MISC);

			add(EXECUTOR_ACTIVATE_PERSON);
			add(EXECUTOR_DEACTIVATE_PERSON);

			add(CREATE_PERSON);
			add(READ_PERSON);
			add(UPDATE_PERSON);
			add(DELETE_PERSON);

			add(CREATE_ROLE);
			add(READ_ROLE);
			add(UPDATE_ROLE);
			add(DELETE_ROLE);

			add(CREATE_COUNTRY);
			add(READ_COUNTRY);
			add(UPDATE_COUNTRY);
			add(DELETE_COUNTRY);

			add(CREATE_PERSON_LINK_TYPE);
			add(READ_PERSON_LINK_TYPE);
			add(UPDATE_PERSON_LINK_TYPE);
			add(DELETE_PERSON_LINK_TYPE);

			add(CREATE_AUTHORIZATION);
			add(READ_AUTHORIZATION);
			add(UPDATE_AUTHORIZATION);
			add(DELETE_AUTHORIZATION);
		}
	};

	public static final Collection<String> GUEST_AUTHORIZATIONS = new LinkedList<String>() {
		private static final long serialVersionUID = 1404650177379798436L;
		{
			add(ADMIN_MISC);

			add(READ_PERSON);
			add(READ_ROLE);
			add(READ_COUNTRY);
			add(READ_PERSON_LINK_TYPE);
			add(READ_AUTHORIZATION);

		}
	};

	private AuthorizationKeys() {}

}
