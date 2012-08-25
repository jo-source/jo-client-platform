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

package org.jowidgets.cap.security.ui.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.ui.api.ISecureControlCreator;
import org.jowidgets.cap.security.ui.api.ISecureControlMapper;
import org.jowidgets.cap.security.ui.api.plugin.ISecureControlCreatorDecoratorPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.widgets.IWidgetCommon;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.common.widgets.factory.IWidgetFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.reflection.ReflectionCache;

final class SecureControlFactoryDecoratorImpl<WIDGET_TYPE extends IControl, DESCRIPTOR_TYPE extends IWidgetDescriptor<? extends WIDGET_TYPE>, AUTHORIZATION_TYPE> implements
		IDecorator<IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE>> {

	private static final IMessage DEFAULT_FAILED_MESSAGE = Messages.getMessage("SecureControlFactoryDecoratorImpl.defaultFailedText");

	private final ISecureControlMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> controlAuthorizationMapper;
	private final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;
	private final ISecureControlCreator<? extends IControl> controlCreator;

	SecureControlFactoryDecoratorImpl(
		final ISecureControlMapper<WIDGET_TYPE, DESCRIPTOR_TYPE, AUTHORIZATION_TYPE> controlAuthorizationMapper,
		final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker,
		final ISecureControlCreator<? extends IControl> controlCreator) {

		this.controlAuthorizationMapper = controlAuthorizationMapper;
		this.authorizationChecker = authorizationChecker;
		this.controlCreator = controlCreator;
	}

	@Override
	public IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE> decorate(final IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE> original) {
		return new SecureControlFactory(original);
	}

	private final class SecureControlFactory implements IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE> {

		private final IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE> originalFactory;

		private SecureControlFactory(final IWidgetFactory<WIDGET_TYPE, DESCRIPTOR_TYPE> original) {
			Assert.paramNotNull(original, "original");
			this.originalFactory = original;
		}

		@Override
		public WIDGET_TYPE create(final Object parentUiReference, final DESCRIPTOR_TYPE bluePrint) {
			if (authorizationChecker.hasAuthorization(controlAuthorizationMapper.getAuthorization(bluePrint))) {
				return originalFactory.create(parentUiReference, bluePrint);
			}
			else {
				return createDummyWidget(parentUiReference, bluePrint);
			}
		}

		@SuppressWarnings("unchecked")
		private WIDGET_TYPE createDummyWidget(final Object parentUiReference, final DESCRIPTOR_TYPE bluePrint) {

			final IComposite composite = Toolkit.getWidgetFactory().create(parentUiReference, BPF.composite());
			composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));
			final WIDGET_TYPE originalWidget = originalFactory.create(composite.getUiReference(), bluePrint);

			composite.add(getControlCreator(bluePrint, originalWidget), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
			originalWidget.setVisible(false);

			final InvocationHandler invocationHandler = new SecureControlInvocationHandler(
				composite,
				controlAuthorizationMapper.getWidgetType(),
				originalWidget);

			return (WIDGET_TYPE) Proxy.newProxyInstance(
					IBeanTable.class.getClassLoader(),
					new Class[] {IBeanTable.class},
					invocationHandler);
		}

		private ICustomWidgetCreator<? extends IControl> getControlCreator(
			final DESCRIPTOR_TYPE descriptor,
			final WIDGET_TYPE widget) {

			String failedText = controlAuthorizationMapper.getAuthorizationFailedText(descriptor, widget);
			if (failedText == null) {
				failedText = DEFAULT_FAILED_MESSAGE.get();
			}
			final String label = failedText;
			final IImageConstant icon = controlAuthorizationMapper.getAuthorizationFailedIcon(descriptor, widget);

			return new ICustomWidgetCreator<IControl>() {
				@Override
				public IControl create(final ICustomWidgetFactory widgetFactory) {
					return getDecoratedControlCreator(descriptor, widget).create(widgetFactory, label, icon);
				}
			};
		}

		@SuppressWarnings("unchecked")
		private ISecureControlCreator<? extends IControl> getDecoratedControlCreator(
			final DESCRIPTOR_TYPE descriptor,
			final WIDGET_TYPE widget) {
			ISecureControlCreator<? extends IControl> result = controlCreator;
			final IPluginPropertiesBuilder propertiesBuilder = PluginProperties.builder();
			propertiesBuilder.add(
					ISecureControlCreatorDecoratorPlugin.DESCRIPTOR_INTERFACE_PROPERTY_KEY,
					descriptor.getDescriptorInterface());
			final IPluginProperties pluginProperties = propertiesBuilder.build();
			List<ISecureControlCreatorDecoratorPlugin> plugins;
			plugins = PluginProvider.getPlugins(ISecureControlCreatorDecoratorPlugin.ID, pluginProperties);
			for (final ISecureControlCreatorDecoratorPlugin plugin : plugins) {
				result = plugin.decorate((ISecureControlCreator<IControl>) result, descriptor, widget, pluginProperties);
			}
			return result;
		}
	}

	private final class SecureControlInvocationHandler implements InvocationHandler {

		private final Set<Method> controlMethods;
		private final Set<Method> decoratedControlMethods;

		private final IControl dummyControl;
		private final IWidgetCommon decoratedControl;

		private SecureControlInvocationHandler(
			final IControl dummyControl,
			final Class<?> decoratedType,
			final IControl decoratedControl) {

			this.dummyControl = dummyControl;
			this.decoratedControl = decoratedControl;

			controlMethods = ReflectionCache.getMethods(IControl.class);
			decoratedControlMethods = ReflectionCache.getMethods(decoratedType);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (controlMethods.contains(method)) {
				return method.invoke(dummyControl, args);
			}
			else if (decoratedControlMethods.contains(method)) {
				return method.invoke(decoratedControl, args);
			}
			else {
				return method.invoke(dummyControl, args);
			}
		}
	}

}
