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

package org.jowidgets.cap.ui.api.table;

import java.util.List;

import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.types.Null;


public interface IBeanTableModelBuilder<BEAN_TYPE> {

	IBeanTableModelBuilder<BEAN_TYPE> setReaderService(IReaderService<Null> readerService);

	IBeanTableModelBuilder<BEAN_TYPE> setReaderService(IServiceId<IReaderService<Null>> readerServiceId);

	<PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		final IReaderService<PARAM_TYPE> readerService,
		IReaderParameterProvider<PARAM_TYPE> paramProvider);

	<PARAM_TYPE> IBeanTableModelBuilder<BEAN_TYPE> setReaderService(
		final IServiceId<IReaderService<PARAM_TYPE>> readerServiceId,
		IReaderParameterProvider<PARAM_TYPE> paramProvider);

	IBeanTableModelBuilder<BEAN_TYPE> setEntityServices(IBeanServicesProvider<BEAN_TYPE> entityServicesProvider);

	IBeanTableModelBuilder<BEAN_TYPE> setEntityServices(
		final IServiceId<IBeanServicesProvider<BEAN_TYPE>> entityServicesProviderId);

	IBeanTableModelBuilder<BEAN_TYPE> setCreatorService(final ICreatorService creatorService);

	IBeanTableModelBuilder<BEAN_TYPE> setCreatorService(final IServiceId<ICreatorService> creatorServiceId);

	IBeanTableModelBuilder<BEAN_TYPE> setRefreshService(final IRefreshService refreshService);

	IBeanTableModelBuilder<BEAN_TYPE> setRefreshService(final IServiceId<IRefreshService> refreshServiceId);

	IBeanTableModelBuilder<BEAN_TYPE> setUpdaterService(final IUpdaterService updaterService);

	IBeanTableModelBuilder<BEAN_TYPE> setUpdaterService(final IServiceId<IUpdaterService> updaterServiceId);

	IBeanTableModelBuilder<BEAN_TYPE> setDeleterService(final IDeleterService deleterService);

	IBeanTableModelBuilder<BEAN_TYPE> setDeleterService(final IServiceId<IDeleterService> deleterServiceId);

	IBeanTableModelBuilder<BEAN_TYPE> setParent(IBeanListModel<?> parent, LinkType linkType);

	IBeanTableModelBuilder<BEAN_TYPE> setAttributes(List<? extends IAttribute<?>> attributes);

	IBeanTableModel<BEAN_TYPE> build();

}
