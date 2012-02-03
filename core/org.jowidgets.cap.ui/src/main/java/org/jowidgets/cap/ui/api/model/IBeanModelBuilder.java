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

package org.jowidgets.cap.ui.api.model;

import java.util.List;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.IProvider;

public interface IBeanModelBuilder<BEAN_TYPE, INSTANCE_TYPE> {

	INSTANCE_TYPE setReaderService(IReaderService<Void> readerService);

	INSTANCE_TYPE setReaderService(IServiceId<IReaderService<Void>> readerServiceId);

	<PARAM_TYPE> INSTANCE_TYPE setReaderService(
		final IReaderService<PARAM_TYPE> readerService,
		IProvider<PARAM_TYPE> paramProvider);

	<PARAM_TYPE> INSTANCE_TYPE setReaderService(
		final IServiceId<IReaderService<PARAM_TYPE>> readerServiceId,
		IProvider<PARAM_TYPE> paramProvider);

	INSTANCE_TYPE setEntityServices(IBeanServicesProvider beanServicesProvider);

	INSTANCE_TYPE setEntityServices(final IServiceId<IBeanServicesProvider> entityServicesProviderId);

	INSTANCE_TYPE setCreatorService(final ICreatorService creatorService);

	INSTANCE_TYPE setCreatorService(final IServiceId<ICreatorService> creatorServiceId);

	INSTANCE_TYPE setRefreshService(final IRefreshService refreshService);

	INSTANCE_TYPE setRefreshService(final IServiceId<IRefreshService> refreshServiceId);

	INSTANCE_TYPE setUpdaterService(final IUpdaterService updaterService);

	INSTANCE_TYPE setUpdaterService(final IServiceId<IUpdaterService> updaterServiceId);

	INSTANCE_TYPE setDeleterService(final IDeleterService deleterService);

	INSTANCE_TYPE setDeleterService(final IServiceId<IDeleterService> deleterServiceId);

	INSTANCE_TYPE setParent(IBeanListModel<?> parent, LinkType linkType);

	INSTANCE_TYPE setAttributes(List<? extends IAttribute<?>> attributes);

	INSTANCE_TYPE setMetaAttributes(String... metaPropertyNames);

	INSTANCE_TYPE addBeanValidator(IBeanValidator<BEAN_TYPE> beanValidator);

}
