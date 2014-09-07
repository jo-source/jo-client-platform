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
package org.jowidgets.cap.ui.tools.bean;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionObservable;
import org.jowidgets.cap.ui.api.plugin.IBeanSelectionProviderPlugin;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.util.Assert;

/**
 * @author MGrossmann
 * 
 * @param <BEAN_TYPE> The type of the beans
 */
public class BeanSelectionObservable<BEAN_TYPE> implements IBeanSelectionObservable<BEAN_TYPE> {

	private final Set<IBeanSelectionListener<BEAN_TYPE>> listeners;

	public BeanSelectionObservable() {
		this.listeners = new LinkedHashSet<IBeanSelectionListener<BEAN_TYPE>>();
	}

	@Override
	public final void addBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		listeners.add(listener);
	}

	@Override
	public final void removeBeanSelectionListener(final IBeanSelectionListener<BEAN_TYPE> listener) {
		Assert.paramNotNull(listener, "listener");
		listeners.remove(listener);
	}

	public final void fireBeanSelectionEvent(final IBeanSelectionEvent<BEAN_TYPE> event) {
		Assert.paramNotNull(event, "event");
		for (final IBeanSelectionListener<BEAN_TYPE> listener : new LinkedList<IBeanSelectionListener<BEAN_TYPE>>(listeners)) {
			listener.selectionChanged(event);
		}
		fireSelectionChangedOnPlugins(event);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void fireSelectionChangedOnPlugins(final IBeanSelectionEvent event) {
		final Class<?> beanType = event.getBeanType();
		final Class<?> eventSourceType = event.getSource().getClass();
		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IBeanSelectionProviderPlugin.ENTITIY_ID_PROPERTY_KEY, event.getEntityId());
		propBuilder.add(IBeanSelectionProviderPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		propBuilder.add(IBeanSelectionProviderPlugin.SELECTION_SOURCE_TYPE_PROPERTY_KEY, eventSourceType);
		propBuilder.add(IBeanSelectionProviderPlugin.SELECTION_EMPTY_PROPERTY_KEY, event.getFirstSelected() == null);
		final IPluginProperties properties = propBuilder.build();
		for (final IBeanSelectionProviderPlugin<?> plugin : PluginProvider.getPlugins(IBeanSelectionProviderPlugin.ID, properties)) {
			plugin.selectionChanged(event, properties);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void fireBeanSelectionEvent(
		final IBeanSelectionObservable source,
		final Object beanTypeId,
		final Class beanType,
		final Object entityId,
		final Collection selection) {
		fireBeanSelectionEvent(new BeanSelectionEventImpl<BEAN_TYPE>(source, beanTypeId, beanType, entityId, selection));
	}

	public void dispose() {
		listeners.clear();
	}
}
