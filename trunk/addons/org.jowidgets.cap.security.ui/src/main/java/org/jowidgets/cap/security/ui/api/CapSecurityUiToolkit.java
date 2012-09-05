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

package org.jowidgets.cap.security.ui.api;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.api.model.item.IActionItemVisibilityAspectPlugin;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.security.ui.impl.CapSecurityUiToolkitImpl;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.cap.ui.api.plugin.IBeanRelationTreePlugin;
import org.jowidgets.cap.ui.api.plugin.IServiceActionDecoratorPlugin;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.factory.IWidgetFactory;
import org.jowidgets.service.api.IServiceProviderDecoratorHolder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

public final class CapSecurityUiToolkit {

	private static ICapSecurityUiToolkit instance;

	private CapSecurityUiToolkit() {}

	public static synchronized void initialize(final ICapSecurityUiToolkit instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			CapSecurityUiToolkit.instance = instance;
		}
		else {
			throw new IllegalStateException("The CapSecurityUiToolkit is already initialized");
		}
	}

	public static <AUTHORIZATION_TYPE> ISecureServiceProviderDecoratorBuilder<AUTHORIZATION_TYPE> secureServiceProviderDecoratorBuilder() {
		return getInstance().secureServiceProviderDecoratorBuilder();
	}

	public static IServiceProviderDecoratorHolder secureServiceProviderDecorator() {
		return getInstance().secureServiceProviderDecorator();
	}

	public static <AUTHORIZATION_TYPE> ISecureServiceActionDecoratorPluginBuilder<AUTHORIZATION_TYPE> secureServiceActionDecoratorPluginBuilder() {
		return getInstance().secureServiceActionDecoratorPluginBuilder();
	}

	public static IServiceActionDecoratorPlugin secureServiceActionDecoratorPlugin() {
		return getInstance().secureServiceActionDecoratorPlugin();
	}

	public static IActionItemVisibilityAspectPlugin secureActionItemVisibilityAspectPlugin() {
		return getInstance().secureActionItemVisibilityAspectPlugin();
	}

	public static <AUTHORIZATION_TYPE> ISecureActionItemVisibilityAspectPluginBuilder<AUTHORIZATION_TYPE> secureActionItemVisibilityAspectPluginBuilder() {
		return getInstance().secureActionItemVisibilityAspectPluginBuilder();
	}

	public static <AUTHORIZATION_TYPE> ISecureBeanFormPluginBuilder<AUTHORIZATION_TYPE> secureBeanFormPluginBuilder() {
		return getInstance().secureBeanFormPluginBuilder();
	}

	public static IBeanFormPlugin secureBeanFormPlugin() {
		return getInstance().secureBeanFormPlugin();
	}

	public static <AUTHORIZATION_TYPE> ISecureBeanRelationTreePluginBuilder<AUTHORIZATION_TYPE> secureBeanRelationTreePluginBuilder() {
		return getInstance().secureBeanRelationTreePluginBuilder();
	}

	public static IBeanRelationTreePlugin<Object> secureBeanRelationTreePlugin() {
		return getInstance().secureBeanRelationTreePlugin();
	}

	public static <WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<WIDGET_TYPE>, AUTHORIZATION_TYPE> ISecureControlFactoryDecoratorBuilder<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> secureControlFactoryDecoratorBuilder(
		final ISecureControlMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> mapper) {
		return getInstance().secureControlFactoryDecoratorBuilder(mapper);
	}

	public static <WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<WIDGET_TYPE>, AUTHORIZATION_TYPE> IDecorator<IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE>> secureControlFactoryDecorator(
		final ISecureControlMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> mapper) {
		return getInstance().secureControlFactoryDecorator(mapper);
	}

	public static <AUTHORIZATION_TYPE> ISecureControlMapperFactory<AUTHORIZATION_TYPE> secureControlMapperFactory() {
		return getInstance().secureControlMapperFactory();
	}

	public static ICapSecurityUiToolkit getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<ICapSecurityUiToolkit> serviceLoader = ServiceLoader.load(ICapSecurityUiToolkit.class);
			final Iterator<ICapSecurityUiToolkit> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				instance = new CapSecurityUiToolkitImpl();
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ ICapSecurityUiToolkit.class.getName()
						+ "'");
				}
			}
		}
	}
}
