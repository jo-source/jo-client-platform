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

package org.jowidgets.cap.service.impl.dummy.service;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.refresh.IRefreshServiceBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.service.api.IServiceRegistry;

final class BeanServicesProviderBuilderCreator<BEAN_TYPE extends IBean> {

	private final IBeanServicesProviderBuilder builder;

	BeanServicesProviderBuilderCreator(
		final IServiceRegistry registry,
		final Object entityTypeId,
		final IEntityData<? extends BEAN_TYPE> data,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {

		this.builder = CapServiceToolkit.beanServicesProviderBuilder(registry, IEntityService.ID, IBean.class, entityTypeId);

		//reader service
		builder.setReaderService(new SyncReaderService<BEAN_TYPE>(data, beanDtoFactory));

		//creator service
		builder.setCreatorService(new SyncCreatorService<BEAN_TYPE>(data, beanDtoFactory, beanInitializer));

		//deleter service
		builder.setDeleterService(new SyncDeleterService(data, true, false));

		//updater service
		final IUpdaterServiceBuilder<BEAN_TYPE> updaterBuilder = CapServiceToolkit.updaterServiceBuilder(data);
		updaterBuilder.setBeanDtoFactory(beanDtoFactory);
		updaterBuilder.setBeanModifier(beanModifier);
		builder.setUpdaterService(updaterBuilder.build());

		//refresh service
		final IRefreshServiceBuilder<BEAN_TYPE> refreshBuilder = CapServiceToolkit.refreshServiceBuilder(data);
		refreshBuilder.setBeanDtoFactory(beanDtoFactory);
		builder.setRefreshService(refreshBuilder.build());

	}

	public IBeanServicesProviderBuilder getBuilder() {
		return builder;
	}

}
