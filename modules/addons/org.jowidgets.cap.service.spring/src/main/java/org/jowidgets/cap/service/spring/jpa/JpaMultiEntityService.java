/*
 * Copyright (c) 2016, SStoehrmann
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

package org.jowidgets.cap.service.spring.jpa;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityInfo;
import org.jowidgets.cap.common.api.service.IEntityService;

public class JpaMultiEntityService implements IEntityService {

	List<IEntityService> services = new LinkedList<IEntityService>();

	@Override
	public Collection<IEntityInfo> getEntityInfos() {
		final Collection<IEntityInfo> result = new LinkedList<IEntityInfo>();
		for (final Iterator<IEntityService> iterator = services.iterator(); iterator.hasNext();) {
			final Collection<IEntityInfo> entityInfos = iterator.next().getEntityInfos();
			if (entityInfos != null) {
				result.addAll(entityInfos);
			}
		}
		return result;
	}

	@Override
	public IEntityInfo getEntityInfo(final Object entityId) {
		for (final Iterator<IEntityService> iterator = services.iterator(); iterator.hasNext();) {
			final IEntityInfo entityInfo = iterator.next().getEntityInfo(entityId);
			if (entityInfo != null) {
				return entityInfo;
			}
		}
		return null;
	}

	@Override
	public IBeanDtoDescriptor getDescriptor(final Object entityId) {
		for (final Iterator<IEntityService> iterator = services.iterator(); iterator.hasNext();) {
			final IBeanDtoDescriptor dtoDesc = iterator.next().getDescriptor(entityId);
			if (dtoDesc != null) {
				return dtoDesc;
			}
		}
		return null;
	}

	@Override
	public IBeanServicesProvider getBeanServices(final Object entityId) {
		for (final Iterator<IEntityService> iterator = services.iterator(); iterator.hasNext();) {
			final IBeanServicesProvider serviceProvider = iterator.next().getBeanServices(entityId);
			if (serviceProvider != null) {
				return serviceProvider;
			}
		}
		return null;
	}

	@Override
	public List<IEntityLinkDescriptor> getEntityLinks(final Object entityId) {
		final List<IEntityLinkDescriptor> result = new LinkedList<IEntityLinkDescriptor>();
		for (final Iterator<IEntityService> iterator = services.iterator(); iterator.hasNext();) {
			final List<IEntityLinkDescriptor> entityLinks = iterator.next().getEntityLinks(entityId);
			if (entityLinks != null) {
				result.addAll(entityLinks);
			}
		}
		return result;
	}

	@Override
	public void clearCache() {
		for (final Iterator<IEntityService> iterator = services.iterator(); iterator.hasNext();) {
			iterator.next().clearCache();
		}
	}

	public void addJpaEntityService(final IEntityService service) {
		services.add(service);
	}

}
