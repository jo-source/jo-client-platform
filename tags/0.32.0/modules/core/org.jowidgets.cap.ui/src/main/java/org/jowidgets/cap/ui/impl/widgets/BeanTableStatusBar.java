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

import java.util.ArrayList;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.ICheckedItemModelBuilder;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.ILabel;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.model.ILabelModelBuilder;
import org.jowidgets.cap.ui.api.model.ILabelRenderer;
import org.jowidgets.cap.ui.api.model.LabelModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.tools.model.BeanListModelListenerAdapter;
import org.jowidgets.cap.ui.tools.model.DefaultLabelRenderer;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;

final class BeanTableStatusBar<BEAN_TYPE> {

	private static final int DEFAULT_FONT_SIZE = 7;

	private static final IMessage MESSAGE_SELECTION = Messages.getMessage("BeanTableStatusBar.row_n_of_n");
	private static final IMessage MESSAGE_MULTI_SELECTION = Messages.getMessage("BeanTableStatusBar.n_rows_of_n");
	private static final IMessage MESSAGE_NO_SELECTION = Messages.getMessage("BeanTableStatusBar.n_rows");
	private static final IMessage MESSAGE_NO_SELECTION_ONE_ROW = Messages.getMessage("BeanTableStatusBar.1_row");

	private final IComposite composite;
	private final BeanTableImpl<BEAN_TYPE> table;
	private final IBeanTableModel<BEAN_TYPE> model;
	private final IComposite statusbar;
	private final ICheckedItemModel statusBarItemModel;
	private final IItemStateListener statusBarItemListener;
	private final ILabelRenderer<IBeanTable<BEAN_TYPE>> renderer;

	private final ILabel label;

	BeanTableStatusBar(
		final IComposite composite,
		final ILabelRenderer<IBeanTable<BEAN_TYPE>> renderer,
		final BeanTableImpl<BEAN_TYPE> table) {

		this.composite = composite;
		this.table = table;
		this.model = table.getModel();
		this.statusbar = composite.add(3, BPF.composite(), "growx, w 0::");
		this.renderer = renderer != null ? renderer : new DefaultLabelRenderer<IBeanTable<BEAN_TYPE>>();
		statusbar.setLayout(new MigLayoutDescriptor("3[]1", "3[]0"));
		statusbar.setVisible(false);

		this.label = statusbar.add(BPF.label().setFontSize(DEFAULT_FONT_SIZE));

		final IBeanSelectionListener<BEAN_TYPE> beanSelectionListener = new IBeanSelectionListener<BEAN_TYPE>() {
			@Override
			public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
				renderLabel();
			}
		};
		model.addBeanSelectionListener(beanSelectionListener);

		final IBeanListModelListener<BEAN_TYPE> listModelListener = new BeanListModelListenerAdapter<BEAN_TYPE>() {
			@Override
			public void beansChanged() {
				renderLabel();
			}
		};
		model.addBeanListModelListener(listModelListener);

		this.statusBarItemModel = createStatusBarItemModel();
		this.statusBarItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				//TODO MG set status bar state
				table.setStatusBarVisible(statusBarItemModel.isSelected());
			}
		};
		statusBarItemModel.addItemListener(statusBarItemListener);

		composite.addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				model.removeBeanSelectionListener(beanSelectionListener);
				model.removeBeanListModelListener(listModelListener);
			}
		});
	}

	private void renderLabel() {
		statusbar.layoutBegin();

		final ILabelModel labelModel = getModelLabel();

		label.setText(labelModel.getText());
		label.setToolTipText(labelModel.getDescription());
		label.setIcon(labelModel.getIcon());
		label.setForegroundColor(labelModel.getForegroundColor());

		if (!EmptyCheck.isEmpty(labelModel.getFontName())) {
			label.setFontName(labelModel.getFontName());
		}
		if (labelModel.getFontSize() != null) {
			label.setFontSize(labelModel.getFontSize().intValue());
		}

		if (labelModel.getMarkup() != null) {
			label.setMarkup(labelModel.getMarkup());
		}
		else {
			label.setMarkup(Markup.DEFAULT);
		}

		statusbar.layoutEnd();
	}

	private ILabelModel getModelLabel() {
		final ILabelModelBuilder builder = LabelModel.builder().setText(getStatusBarText()).setFontSize(DEFAULT_FONT_SIZE);
		renderer.render(table, builder);
		return builder.build();
	}

	private String getStatusBarText() {
		final int selectionSize = getSelectionSize();
		final int modelSize = getModelSize();
		final ArrayList<Integer> selection = model.getSelection();
		if (selectionSize == 1) {
			final String selected = "" + (selection.get(0).intValue() + 1);
			return MessageReplacer.replace(MESSAGE_SELECTION.get(), model.getEntityLabelSingular(), selected, "" + getModelSize());
		}
		else if (selectionSize > 1) {
			return MessageReplacer.replace(MESSAGE_MULTI_SELECTION.get(), "" + selectionSize, model.getEntityLabelPlural(), ""
				+ modelSize);
		}
		else if (modelSize == 1) {
			return MessageReplacer.replace(MESSAGE_NO_SELECTION_ONE_ROW.get(), model.getEntityLabelSingular());
		}
		else {
			return MessageReplacer.replace(MESSAGE_NO_SELECTION.get(), "" + modelSize, model.getEntityLabelPlural());
		}
	}

	/**
	 * @return The selection size ignoring the lastBean if enabled
	 */
	private int getSelectionSize() {
		final int size = model.getSelection().size();
		if (size == 0) {
			return 0;
		}
		else if (!model.hasLastBean()) {
			return size;
		}
		else {//lastBeanEnabled && > 0
			final Integer lastSelected = model.getSelection().get(size - 1);
			if (lastSelected.intValue() == model.getSize() - 1) {
				return size - 1;
			}
			else {
				return size;
			}
		}
	}

	/**
	 * @return The model size ignoring the lastBean if enabled
	 */
	private int getModelSize() {
		final int size = model.getSize();
		if (model.hasLastBean() && size > 0) {
			return size - 1;
		}
		else {
			return size;
		}
	}

	private ICheckedItemModel createStatusBarItemModel() {
		final IItemModelFactory modelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();
		final ICheckedItemModelBuilder builder = modelFactory.checkedItemBuilder();
		final String text = Messages.getString("BeanTableStatusBar.show_statusbar_text");
		final String tooltip = Messages.getString("BeanTableStatusBar.show_statusbar_tooltip");
		builder.setText(text).setToolTipText(tooltip);
		return builder.build();
	}

	void setVisible(final boolean visible) {
		if (statusbar.isVisible() != visible) {
			composite.layoutBegin();
			statusbar.setVisible(visible);
			renderLabel();
			composite.layoutEnd();
			statusBarItemModel.removeItemListener(statusBarItemListener);
			statusBarItemModel.setSelected(visible);
			statusBarItemModel.addItemListener(statusBarItemListener);
		}
	}

	ICheckedItemModel getStatusBarItemModel() {
		return statusBarItemModel;
	}

}
