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

package org.jowidgets.cap.ui.impl.widgets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.api.controller.ITabFolderListener;
import org.jowidgets.api.controller.ITabSelectionEvent;
import org.jowidgets.api.widgets.ITabFolder;
import org.jowidgets.api.widgets.ITabItem;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.ICustomBeanPropertyListener;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTab;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFactory;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolder;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.TabFolderWrapper;
import org.jowidgets.util.ITypedKey;

@SuppressWarnings("rawtypes")
final class BeanTabFolderImpl<BEAN_TYPE> extends TabFolderWrapper implements IBeanTabFolder<BEAN_TYPE> {

	private final ITabFolder tabFolder;
	private final IBeanTabFolderModel<BEAN_TYPE> model;
	private final IBeanTabFactory<BEAN_TYPE> beanTabFactory;
	private final Map<Integer, IBeanTab<BEAN_TYPE>> beanTabs;
	private final RenderLabelListener renderLabelListener;

	private final Map<ITypedKey, RenderLabelCustomPropertiesListener> renderLabelCustomPropertiesListeners;
	private final TabFolderSelectionListener tabFolderSelectionListener;
	private final ModelSelectionListener modelSelectionListener;

	private final String dummyBeanLabel;
	private final String dummyBeanDecription;

	BeanTabFolderImpl(final ITabFolder tabFolder, final IBeanTabFolderBluePrint<BEAN_TYPE> bluePrint) {
		super(tabFolder);

		this.dummyBeanLabel = Messages.getString("BeanTabFolderImpl.dummy_bean_label");
		this.dummyBeanDecription = Messages.getString("BeanTabFolderImpl.dummy_bean_description");

		this.tabFolder = tabFolder;
		this.model = bluePrint.getModel();
		this.beanTabFactory = bluePrint.getTabFactory();

		this.beanTabs = new HashMap<Integer, IBeanTab<BEAN_TYPE>>();
		this.renderLabelListener = new RenderLabelListener();
		this.renderLabelCustomPropertiesListeners = new HashMap<ITypedKey, RenderLabelCustomPropertiesListener>();
		final IBeanProxyLabelRenderer<BEAN_TYPE> labelRenderer = model.getLabelRenderer();
		final Set<? extends ITypedKey<?>> customPropertyDependencies = labelRenderer.getCustomPropertyDependencies();
		if (customPropertyDependencies != null) {
			for (final ITypedKey<?> typedKey : customPropertyDependencies) {
				renderLabelCustomPropertiesListeners.put(typedKey, new RenderLabelCustomPropertiesListener());
			}
		}

		this.tabFolderSelectionListener = new TabFolderSelectionListener();
		this.modelSelectionListener = new ModelSelectionListener();

		model.addBeanListModelListener(new IBeanListModelListener() {
			@Override
			public void beansChanged() {
				updateFromModel();
			}
		});

		model.addBeanSelectionListener(modelSelectionListener);
		tabFolder.addTabFolderListener(tabFolderSelectionListener);

		updateFromModel();
	}

	@Override
	public IBeanTabFolderModel<BEAN_TYPE> getModel() {
		return model;
	}

	@SuppressWarnings("unchecked")
	private void updateFromModel() {
		model.removeBeanSelectionListener(modelSelectionListener);
		tabFolder.removeTabFolderListener(tabFolderSelectionListener);

		final int tabsToAdd = model.getSize() - tabFolder.getItems().size();
		if (tabsToAdd < 0) {
			for (int i = 0; i < -tabsToAdd; i++) {
				final int removeIndex = tabFolder.getItems().size() - 1;
				tabFolder.removeItem(removeIndex);
				beanTabs.remove(Integer.valueOf(removeIndex));
			}
		}
		else {
			for (int i = 0; i < tabsToAdd; i++) {
				addTab();
			}
		}

		for (int i = 0; i < model.getSize(); i++) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(i);

			final ITabItem item = tabFolder.getItem(i);

			bean.addPropertyChangeListener(renderLabelListener);
			for (final Entry<ITypedKey, RenderLabelCustomPropertiesListener> entry : renderLabelCustomPropertiesListeners.entrySet()) {
				bean.addCustomPropertyListener(entry.getKey(), entry.getValue());
			}

			final IBeanTab<BEAN_TYPE> beanTab = beanTabs.get(Integer.valueOf(i));
			beanTab.setBean(bean);
			renderLabel(item, bean);
		}

		tabFolder.addTabFolderListener(tabFolderSelectionListener);
		model.addBeanSelectionListener(modelSelectionListener);

		if (tabFolder.getItems().size() > 0) {
			final Integer selectionIndex = model.getSelectionIndex();
			if (selectionIndex != null) {
				if (selectionIndex.intValue() < tabFolder.getItems().size()) {
					tabFolder.setSelectedItem(selectionIndex.intValue());
				}
				else {
					model.setSelection(tabFolder.getSelectedIndex());
				}
			}
			else {
				model.setSelection(tabFolder.getSelectedIndex());
			}
		}

	}

	private void renderLabel(final ITabItem item, final IBeanProxy<BEAN_TYPE> bean) {
		if (bean.isDummy()) {
			item.setText(dummyBeanLabel);
			item.setToolTipText(dummyBeanDecription);
			item.setIcon(null);
		}
		else {
			final ILabelModel label = model.getLabelRenderer().getLabel(bean);
			item.setText(label.getText());
			item.setToolTipText(label.getDescription());
			item.setIcon(label.getIcon());
		}

	}

	private void addTab() {
		final int newTabIndex = tabFolder.getItems().size();
		final ITabItem tabItem = tabFolder.addItem(BPF.tabItem());
		final IBeanTab<BEAN_TYPE> beanTab = beanTabFactory.createTab(tabItem);
		beanTabs.put(Integer.valueOf(newTabIndex), beanTab);
	}

	private class RenderLabelListener implements PropertyChangeListener {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			@SuppressWarnings("unchecked")
			final IBeanProxy<BEAN_TYPE> source = (IBeanProxy<BEAN_TYPE>) evt.getSource();
			final IBeanProxyLabelRenderer<BEAN_TYPE> labelRenderer = model.getLabelRenderer();
			final Set<String> propertyDependencies = labelRenderer.getPropertyDependencies();
			if (propertyDependencies == null || propertyDependencies.contains(evt.getPropertyName())) {
				final int beanIndex = model.getBeanIndex(source);
				if (beanIndex != -1) {
					renderLabel(tabFolder.getItem(beanIndex), source);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private class RenderLabelCustomPropertiesListener implements ICustomBeanPropertyListener {
		@Override
		public void propertyChanged(final IBeanProxy bean, final Object oldValue, final Object newValue) {
			final int beanIndex = model.getBeanIndex(bean);
			if (beanIndex != -1) {
				renderLabel(tabFolder.getItem(beanIndex), bean);
			}
		}
	}

	private class TabFolderSelectionListener implements ITabFolderListener {
		@Override
		public void onDeselection(final IVetoable vetoable, final ITabSelectionEvent item) {}

		@Override
		public void itemSelected(final ITabSelectionEvent selectionEvent) {
			model.removeBeanSelectionListener(modelSelectionListener);
			final int selectedIndex = tabFolder.getIndex(selectionEvent.getNewSelected());
			model.setSelection(selectedIndex);
			model.addBeanSelectionListener(modelSelectionListener);
		}
	}

	private class ModelSelectionListener implements IBeanSelectionListener<BEAN_TYPE> {

		@Override
		public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
			final Integer selectionIndex = model.getSelectionIndex();
			if (selectionIndex != null && selectionIndex.intValue() != -1) {
				tabFolder.removeTabFolderListener(tabFolderSelectionListener);
				tabFolder.setSelectedItem(selectionIndex.intValue());
				tabFolder.addTabFolderListener(tabFolderSelectionListener);
			}
		}

	}
}
