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

package org.jowidgets.cap.sample1.service.datastore;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.sample1.common.entity.EntityIds;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataFactory;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataStore;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityFactory;

public final class AuthorizationInitializer {

	public static final String KEY_PROPERTY = "key";
	public static final String DESCRIPTION_PROPERTY = "description";

	public static final List<String> ALL_PROPERTIES = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(KEY_PROPERTY);
			add(DESCRIPTION_PROPERTY);
			add(IBean.ID_PROPERTY);
			add(IBean.VERSION_PROPERTY);
		}
	};

	private AuthorizationInitializer() {}

	public static void initialize() {

		final IEntityData<IBeanPropertyMap> data = EntityDataFactory.create(new IEntityFactory<IBeanPropertyMap>() {

			@Override
			public IBeanPropertyMap createBean(final Long id) {
				return CapServiceToolkit.beanPropertyMap(EntityIds.AUTHORIZATION);
			}

			@Override
			public Class<? extends IBeanPropertyMap> getBeanType() {
				return IBeanPropertyMap.class;
			}
		});

		EntityDataStore.putEntityData(EntityIds.AUTHORIZATION, data);

		addAuthorization(data, "READ_ALL", "Read all content");
		addAuthorization(data, "EDIT_ALL", "Edit all content");
		addAuthorization(data, "DELETE_ALL", "Delete all content");
		addAuthorization(data, "EXECUTIONS_ALL", "All executions are allowed");
		addAuthorization(data, "READ_USERS", "Read the users data");
		addAuthorization(data, "EDIT_USERS", "Edit the users data");
		addAuthorization(data, "DELETE_USERS", "Delete the users data");

	}

	private static void addAuthorization(final IEntityData<IBeanPropertyMap> data, final String key, final String description) {
		final IBeanPropertyMap bean = CapServiceToolkit.beanPropertyMap(EntityIds.AUTHORIZATION);
		bean.setId(data.nextId());
		bean.setValue(KEY_PROPERTY, key);
		bean.setValue(DESCRIPTION_PROPERTY, description);
		data.add(bean);
	}

}
