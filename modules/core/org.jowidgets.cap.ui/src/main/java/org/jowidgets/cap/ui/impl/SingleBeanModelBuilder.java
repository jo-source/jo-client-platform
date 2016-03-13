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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.model.ISingleBeanModelBuilder;
import org.jowidgets.cap.ui.api.plugin.IBeanModelBuilderPlugin;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;

public class SingleBeanModelBuilder<BEAN_TYPE> extends
		AbstractBeanModelBuilderImpl<BEAN_TYPE, ISingleBeanModelBuilder<BEAN_TYPE>> implements ISingleBeanModelBuilder<BEAN_TYPE> {

	SingleBeanModelBuilder(final Object entityId, final Object beanTypeId, final Class<BEAN_TYPE> beanType) {
		super(entityId, beanTypeId, beanType);
		setMetaAttributes(new String[0]);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void modifyFromBeanModelPlugins() {
		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanModelBuilderPlugin.ENTITIY_ID_PROPERTY_KEY, getEntityId());
		propBuilder.add(IBeanModelBuilderPlugin.BEAN_TYPE_PROPERTY_KEY, getBeanType());
		final IPluginProperties properties = propBuilder.build();
		for (final IBeanModelBuilderPlugin plugin : PluginProvider.getPlugins(IBeanModelBuilderPlugin.ID, properties)) {
			plugin.modify(this);
		}
	}

	@Override
	public ISingleBeanModel<BEAN_TYPE> build() {
		modifyFromBeanModelPlugins();
		return new SingleBeanModelImpl<BEAN_TYPE>(
			getBeanTypeId(),
			getBeanType(),
			getEntityId(),
			getReaderService(),
			getReaderParameterProvider(),
			getCreatorService(),
			getRefreshService(),
			getUpdaterService(),
			getDeleterService(),
			getExceptionConverter(),
			getBeanValidators(),
			isValidateUnmodifiedBeans(),
			getParent(),
			getLinkType(),
			getListenerDelay(),
			getAttributes(),
			getBeanProxyContext(),
			getDataModelContext());
	}

}
