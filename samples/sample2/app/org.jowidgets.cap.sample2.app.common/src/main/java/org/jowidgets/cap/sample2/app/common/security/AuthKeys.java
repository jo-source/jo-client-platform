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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.util.EmptyCheck;

public final class AuthKeys {

	//Executor services
	public static final String EXECUTOR_ACTIVATE_PERSON = "EXECUTOR_ACTIVATE_PERSON";
	public static final String EXECUTOR_DEACTIVATE_PERSON = "EXECUTOR_DEACTIVATE_PERSON";

	//CRUD services
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

	public static final String CREATE_AUTHORIZATION = "CREATE_AUTHORIZATION";
	public static final String READ_AUTHORIZATION = "READ_AUTHORIZATION";
	public static final String UPDATE_AUTHORIZATION = "UPDATE_AUTHORIZATION";
	public static final String DELETE_AUTHORIZATION = "DELETE_AUTHORIZATION";

	public static final String CREATE_PHONE = "CREATE_PHONE";
	public static final String READ_PHONE = "READ_PHONE";
	public static final String UPDATE_PHONE = "UPDATE_PHONE";
	public static final String DELETE_PHONE = "DELETE_PHONE";

	public static final String CREATE_PERSON_LINK_TYPE = "CREATE_PERSON_LINK_TYPE";
	public static final String READ_PERSON_LINK_TYPE = "READ_PERSON_LINK_TYPE";
	public static final String UPDATE_PERSON_LINK_TYPE = "UPDATE_PERSON_LINK_TYPE";
	public static final String DELETE_PERSON_LINK_TYPE = "DELETE_PERSON_LINK_TYPE";

	public static final String CREATE_PERSON_ROLE_LINK = "CREATE_PERSON_ROLE_LINK";
	public static final String READ_PERSON_ROLE_LINK = "READ_PERSON_ROLE_LINK";
	public static final String UPDATE_PERSON_ROLE_LINK = "UPDATE_PERSON_ROLE_LINK";
	public static final String DELETE_PERSON_ROLE_LINK = "DELETE_PERSON_ROLE_LINK";

	public static final String CREATE_PERSON_PERSON_LINK = "CREATE_PERSON_PERSON_LINK";
	public static final String READ_PERSON_PERSON_LINK = "READ_PERSON_PERSON_LINK";
	public static final String UPDATE_PERSON_PERSON_LINK = "UPDATE_PERSON_PERSON_LINK";
	public static final String DELETE_PERSON_PERSON_LINK = "DELETE_PERSON_PERSON_LINK";

	public static final String CREATE_ROLE_AUTHORIZATION_LINK = "CREATE_ROLE_AUTHORIZATION_LINK";
	public static final String READ_ROLE_AUTHORIZATION_LINK = "READ_ROLE_AUTHORIZATION_LINK";
	public static final String UPDATE_ROLE_AUTHORIZATION_LINK = "UPDATE_ROLE_AUTHORIZATION_LINK";
	public static final String DELETE_ROLE_AUTHORIZATION_LINK = "DELETE_ROLE_AUTHORIZATION_LINK";

	//View components
	public static final String VIEW_PERSON_COMPONENT = "VIEW_PERSON_COMPONENT";
	public static final String VIEW_ROLE_COMPONENT = "VIEW_ROLE_COMPONENT";
	public static final String VIEW_AUTHORIZATION_COMPONENT = "VIEW_AUTHORIZATION_COMPONENT";
	public static final String VIEW_PERSON_LINK_TYPE_COMPONENT = "VIEW_PERSON_RELATION_TYPE_COMPONENT";
	public static final String VIEW_COUNTRY_COMPONENT = "VIEW_COUNTRY_COMPONENT";
	public static final String VIEW_PHONE_COMPONENT = "VIEW_PHONE_COMPONENT";

	//Authorization collections
	public static final Collection<String> ALL_AUTHORIZATIONS = createAuthorizations();
	public static final Collection<String> GUEST_AUTHORIZATIONS = createAuthorizations(
			"READ_PERSON",
			"VIEW_PERSON_COMPONENT",
			"VIEW_COUNTRY_COMPONENT");

	private AuthKeys() {}

	private static List<String> createAuthorizations(final String... startsWith) {
		final List<String> result = new LinkedList<String>();
		for (final Field field : AuthKeys.class.getDeclaredFields()) {
			if (field.getType().equals(String.class)) {
				try {
					final String authorization = (String) field.get(AuthKeys.class);
					if (EmptyCheck.isEmpty(startsWith)) {
						result.add(authorization);
					}
					else {
						for (final String prefix : startsWith) {
							if (authorization.startsWith(prefix)) {
								result.add(authorization);
								break;
							}
						}
					}
				}
				catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return result;
	}

}
