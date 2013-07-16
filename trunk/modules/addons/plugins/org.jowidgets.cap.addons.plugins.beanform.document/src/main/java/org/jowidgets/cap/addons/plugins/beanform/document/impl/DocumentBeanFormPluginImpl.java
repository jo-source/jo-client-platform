/*
 * Copyright (c) 2013, grossmann
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

package org.jowidgets.cap.addons.plugins.beanform.document.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.addons.widgets.browser.api.BrowserBPF;
import org.jowidgets.addons.widgets.browser.api.IBrowser;
import org.jowidgets.addons.widgets.browser.api.IBrowserBluePrint;
import org.jowidgets.api.controller.IShowingStateListener;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.ITabFolder;
import org.jowidgets.api.widgets.ITabItem;
import org.jowidgets.api.widgets.blueprint.ITabFolderBluePrint;
import org.jowidgets.cap.common.api.dto.IDocument;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.types.TabPlacement;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;

final class DocumentBeanFormPluginImpl implements IBeanFormPlugin {

	@Override
	public void modifySetup(final IPluginProperties properties, final IBeanFormBluePrint<?> bluePrint) {
		if (!hasBrowser()) {
			return;
		}

		final Collection<IAttribute<?>> editModeAttributes = bluePrint.getEditModeAttributes();
		final List<IAttribute<?>> documentAttributes = new LinkedList<IAttribute<?>>();
		final List<IAttribute<?>> additionalAttributes = new LinkedList<IAttribute<?>>();
		for (final IAttribute<?> attribute : editModeAttributes) {
			if (IDocument.class.isAssignableFrom(attribute.getElementValueType())) {
				documentAttributes.add(attribute);
			}
			else if (attribute.isVisible() && !IBeanProxy.ALL_META_ATTRIBUTES.contains(attribute.getPropertyName())) {
				additionalAttributes.add(attribute);
			}
		}
		if (documentAttributes.size() > 0) {
			modifyEditModelLayouter(bluePrint, documentAttributes, additionalAttributes);
		}
	}

	private static boolean hasBrowser() {
		return Toolkit.getWidgetFactory().getFactory(IBrowserBluePrint.class) != null;
	}

	private void modifyEditModelLayouter(
		final IBeanFormBluePrint<?> bluePrint,
		final List<IAttribute<?>> documentAttributes,
		final List<IAttribute<?>> additionalAttributes) {
		bluePrint.setLayouter(new IBeanFormLayouter() {
			@Override
			public void layout(final IContainer container, final IBeanFormControlFactory controlFactory) {
				container.setLayout(MigLayoutFactory.growingInnerCellLayout());

				if (documentAttributes.size() == 1 && additionalAttributes.size() == 0) {
					addBrowserAttribute(container, documentAttributes.iterator().next(), controlFactory);
				}
				else {
					final ITabFolderBluePrint tabFolderBp = BPF.tabFolder().setTabPlacement(TabPlacement.BOTTOM);
					final ITabFolder tabFolder = container.add(tabFolderBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
					if (!additionalAttributes.isEmpty()) {
						final ITabItem item = tabFolder.addItem(BPF.tabItem().setText("Form"));
						addAdditionalAttributes(item, additionalAttributes, controlFactory);
					}
					for (final IAttribute<?> attribute : documentAttributes) {
						final ITabItem item = tabFolder.addItem(BPF.tabItem().setText(attribute.getCurrentLabel()));
						addBrowserAttribute(item, attribute, controlFactory);
					}
				}
			}
		});
	}

	private void addAdditionalAttributes(
		final IContainer container,
		final List<IAttribute<?>> additionalAttributes,
		final IBeanFormControlFactory controlFactory) {
		final IBeanFormToolkit beanFormToolkit = CapUiToolkit.beanFormToolkit();
		final IBeanFormLayout layout = beanFormToolkit.layoutBuilder().addGroups(additionalAttributes).build();
		final IBeanFormLayouter layouter = beanFormToolkit.layouter(layout);
		layouter.layout(container, controlFactory);
	}

	private void addBrowserAttribute(
		final IContainer container,
		final IAttribute<?> attribute,
		final IBeanFormControlFactory controlFactory) {
		container.setLayout(new MigLayoutDescriptor("wrap", "0[grow, 0::]0", "0[]0[grow, 0::]0"));
		final String propertyName = attribute.getPropertyName();
		@SuppressWarnings("unchecked")
		final IInputControl<IDocument> inputField = (IInputControl<IDocument>) container.add(
				controlFactory.createControl(propertyName),
				"growx, w 0::");

		final IBrowser browser = container.add(BrowserBPF.browser(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		container.addShowingStateListener(new IShowingStateListener() {
			@Override
			public void showingStateChanged(final boolean isShowing) {
				if (isShowing) {
					setUrl(container, browser, inputField);
				}
			}
		});

		inputField.addInputListener(new IInputListener() {
			@Override
			public void inputChanged() {
				setUrl(container, browser, inputField);
			}
		});
	}

	private void setUrl(final IContainer container, final IBrowser browser, final IInputControl<IDocument> inputField) {
		final IDocument document = inputField.getValue();
		if (container.isShowing() && document != null && !EmptyCheck.isEmpty(document.getUrl())) {
			browser.setUrl(document.getUrl());
		}
		else {
			browser.setUrl("about:blank");
		}
	}
}
