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

package org.jowidgets.cap.sample1.service.reader;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.sample1.common.entity.EntityIds;
import org.jowidgets.cap.sample1.service.datastore.RoleInitializer;
import org.jowidgets.cap.sample1.service.datastore.UserRoleLinkInitializer;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataStore;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;

public class LinkedRolesOfUsersReaderService extends AbstractSyncReaderService {

	@Override
	IBeanDtoFactory<?> getDtoFactory() {
		return CapServiceToolkit.beanPropertyMapDtoFactory(RoleInitializer.ALL_PROPERTIES);
	}

	@Override
	@SuppressWarnings("unchecked")
	List<IBean> getData(final List<? extends IBeanKey> parentBeanKeys, final IExecutionCallback executionCallback) {
		final List<IBean> result = new LinkedList<IBean>();

		final Set<Object> parentIds = new HashSet<Object>();
		if (parentBeanKeys != null) {
			for (final IBeanKey beanKey : parentBeanKeys) {
				parentIds.add(beanKey.getId());
			}

			final Set<Long> roleIds = new HashSet<Long>();
			final IEntityData<IBeanPropertyMap> userRoleLinkData = (IEntityData<IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.USER_ROLE_LINK);
			for (final IBeanPropertyMap userRoleLink : userRoleLinkData.getAllData()) {
				CapServiceToolkit.checkCanceled(executionCallback);
				if (parentIds.contains(userRoleLink.getValue(UserRoleLinkInitializer.USER_ID))) {
					roleIds.add((Long) userRoleLink.getValue(UserRoleLinkInitializer.ROLE_ID));
				}
			}

			final IEntityData<IBeanPropertyMap> roleData = (IEntityData<IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.ROLE);

			for (final Long roleId : roleIds) {
				CapServiceToolkit.checkCanceled(executionCallback);
				final IBeanPropertyMap role = roleData.getData(roleId);
				if (role != null) {
					result.add(role);
				}
			}
		}

		return result;
	}
}
