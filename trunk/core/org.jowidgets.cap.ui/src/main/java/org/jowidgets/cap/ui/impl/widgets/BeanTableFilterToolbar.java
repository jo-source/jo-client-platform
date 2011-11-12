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

import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.ICheckedItemModelBuilder;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IToolBar;
import org.jowidgets.api.widgets.IToolBarButton;
import org.jowidgets.api.widgets.blueprint.IToolBarButtonBluePrint;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.types.Orientation;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableFilterToolbar<BEAN_TYPE> {

	private final IComposite composite;
	private final IComposite toolBarComposite;
	private final IBeanTableModel<BEAN_TYPE> model;

	private final ICheckedItemModel itemModel;
	private final IItemStateListener itemListener;
	private final IChangeListener filterChangeListener;
	private final String itemTooltip;

	BeanTableFilterToolbar(
		final IComposite composite,
		final BeanTableImpl<BEAN_TYPE> table,
		final IBeanTableMenuFactory<BEAN_TYPE> menuFactory) {
		this.composite = composite;
		this.model = table.getModel();

		this.toolBarComposite = composite.add(0, BPF.composite(), "growy, h 0::");
		toolBarComposite.setLayout(new MigLayoutDescriptor("0[]0[]0", "0[grow]0"));

		final IToolBar toolBar = toolBarComposite.add(0, BPF.toolBar().setVertical(), "growy, h 0::");
		toolBarComposite.add(1, BPF.separator().setOrientation(Orientation.VERTICAL), "growy, h 0::");

		final IToolBarButtonBluePrint closeButtonBp = BPF.toolBarButton().setIcon(IconsSmall.DELETE);
		final String closeButtonTooltip = Messages.getString("BeanTableFilterToolbar.hide_filter_toolbar");
		closeButtonBp.setToolTipText(closeButtonTooltip);
		final IToolBarButton closeButton = toolBar.addItem(closeButtonBp);

		toolBar.addAction(menuFactory.editFilterAction(model));
		toolBar.addAction(menuFactory.deleteFilterAction(model));

		toolBar.pack();

		closeButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				setVisible(false);
			}
		});

		final String disabledTooltip = Messages.getString("BeanTableFilterToolbar.hide_disabled");
		this.filterChangeListener = new IChangeListener() {
			@Override
			public void changed() {
				final IUiFilter filter = model.getFilter(IBeanTableModel.UI_FILTER_ID);
				if (filter == null) {
					itemModel.setEnabled(true);
					itemModel.setToolTipText(itemTooltip);
					closeButton.setEnabled(true);
					closeButton.setToolTipText(closeButtonTooltip);
				}
				else {
					itemModel.setEnabled(false);
					itemModel.setToolTipText(disabledTooltip);
					closeButton.setEnabled(false);
					closeButton.setToolTipText(disabledTooltip);
					setVisible(true);
				}
			}
		};
		model.addFilterChangeListener(filterChangeListener);

		this.itemTooltip = Messages.getString("BeanTableFilterToolbar.show_filter_toolbar_tooltip");
		this.itemModel = createItemModel();
		this.itemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				table.setFilterToolbarVisible(itemModel.isSelected());
			}
		};
		itemModel.addItemListener(itemListener);
		toolBarComposite.setVisible(false);
	}

	private ICheckedItemModel createItemModel() {
		final IItemModelFactory modelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();
		final ICheckedItemModelBuilder builder = modelFactory.checkedItemBuilder();
		final String text = Messages.getString("BeanTableFilterToolbar.show_filter_toolbar_text");
		builder.setText(text).setToolTipText(itemTooltip);
		return builder.build();
	}

	void setVisible(final boolean visible) {
		if (toolBarComposite.isVisible() != visible) {
			composite.layoutBegin();
			toolBarComposite.setVisible(visible);
			composite.layoutEnd();
			itemModel.removeItemListener(itemListener);
			itemModel.setSelected(visible);
			itemModel.addItemListener(itemListener);
		}
	}

	ICheckedItemModel getItemModel() {
		return itemModel;
	}

}
