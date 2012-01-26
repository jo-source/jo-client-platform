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
import java.util.Set;

import org.jowidgets.api.controller.ITabFolderListener;
import org.jowidgets.api.controller.ITabSelectionEvent;
import org.jowidgets.api.widgets.ITabFolder;
import org.jowidgets.api.widgets.ITabItem;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTab;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFactory;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolder;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.tools.model.BeanListModelAdapter;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.TabFolderWrapper;

final class BeanTabFolderImpl<BEAN_TYPE> extends TabFolderWrapper implements IBeanTabFolder<BEAN_TYPE> {

	private final ITabFolder tabFolder;
	private final IBeanTabFolderModel<BEAN_TYPE> model;
	private final IBeanTabFactory<BEAN_TYPE> beanTabFactory;
	private final Map<Integer, IBeanTab<BEAN_TYPE>> beanTabs;
	private final RenderLabelListener renderLabelListener;
	private final TabFolderSelectionListener tabFolderSelectionListener;
	private final ModelSelectionListener modelSelectionListener;

	BeanTabFolderImpl(final ITabFolder tabFolder, final IBeanTabFolderBluePrint<BEAN_TYPE> bluePrint) {
		super(tabFolder);

		this.tabFolder = tabFolder;
		this.model = bluePrint.getModel();
		this.beanTabFactory = bluePrint.getTabFactory();

		this.beanTabs = new HashMap<Integer, IBeanTab<BEAN_TYPE>>();
		this.renderLabelListener = new RenderLabelListener();
		this.tabFolderSelectionListener = new TabFolderSelectionListener();
		this.modelSelectionListener = new ModelSelectionListener();

		model.addBeanListModelListener(new BeanListModelAdapter() {
			@Override
			public void beansChanged() {
				updateFromModel();
			}
		});

		model.addBeanListModelListener(modelSelectionListener);
		tabFolder.addTabFolderListener(tabFolderSelectionListener);

		updateFromModel();
	}

	@Override
	public IBeanTabFolderModel<BEAN_TYPE> getModel() {
		return model;
	}

	private void updateFromModel() {
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

			final IBeanTab<BEAN_TYPE> beanTab = beanTabs.get(Integer.valueOf(i));
			beanTab.setBean(bean);
			renderLabel(item, bean);
		}
	}

	private void renderLabel(final ITabItem item, final IBeanProxy<BEAN_TYPE> bean) {
		final ILabelModel label = model.getLabelRenderer().getLabel(bean);

		item.setText(label.getText());
		item.setToolTipText(label.getDescription());
		item.setIcon(label.getIcon());
	}

	private void addTab() {
		final int newTabIndex = tabFolder.getItems().size();
		final ITabItem tabItem = tabFolder.addItem(BPF.tabItem());
		final IBeanTab<BEAN_TYPE> beanTab = beanTabFactory.createTab(tabItem);
		tabItem.layout();
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

	private class TabFolderSelectionListener implements ITabFolderListener {
		@Override
		public void onDeselection(final IVetoable vetoable, final ITabSelectionEvent item) {}

		@Override
		public void itemSelected(final ITabSelectionEvent selectionEvent) {
			model.removeBeanListModelListener(modelSelectionListener);
			final int selectedIndex = tabFolder.getIndex(selectionEvent.getNewSelected());
			model.setSelection(selectedIndex);
			model.addBeanListModelListener(modelSelectionListener);
		}
	}

	private class ModelSelectionListener extends BeanListModelAdapter {

		@Override
		public void selectionChanged() {
			final Integer selectionIndex = model.getSelectionIndex();
			if (selectionIndex != null && selectionIndex.intValue() != -1) {
				tabFolder.removeTabFolderListener(tabFolderSelectionListener);
				tabFolder.setSelectedItem(selectionIndex.intValue());
				tabFolder.addTabFolderListener(tabFolderSelectionListener);
			}
		}

	}
}
