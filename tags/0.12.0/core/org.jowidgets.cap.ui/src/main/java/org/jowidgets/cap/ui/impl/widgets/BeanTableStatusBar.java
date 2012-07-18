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

import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.ICheckedItemModelBuilder;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.ITextLabel;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;

final class BeanTableStatusBar<BEAN_TYPE> {

	private final IComposite composite;
	private final IBeanTableModel<BEAN_TYPE> model;
	private final IComposite statusbar;
	private final ICheckedItemModel statusBarItemModel;
	private final IItemStateListener statusBarItemListener;
	private final String messageSelection;
	private final String messageNoSelection;

	BeanTableStatusBar(final IComposite composite, final BeanTableImpl<BEAN_TYPE> table) {
		this.composite = composite;
		this.model = table.getModel();
		this.messageSelection = Messages.getString("BeanTableStatusBar.row_n_of_n");
		this.messageNoSelection = Messages.getString("BeanTableStatusBar.n_rows");

		this.statusbar = composite.add(2, BPF.composite(), "growx, w 0::");
		statusbar.setLayout(new MigLayoutDescriptor("3[]1", "3[]0"));
		statusbar.setVisible(false);

		final ITextLabel textLabel = statusbar.add(BPF.textLabel().setFontSize(7));

		model.addBeanSelectionListener(new IBeanSelectionListener<BEAN_TYPE>() {
			@Override
			public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
				statusbar.layoutBegin();
				textLabel.setText(getStatusBarText());
				statusbar.layoutEnd();
			}
		});

		model.addBeanListModelListener(new IBeanListModelListener() {
			@Override
			public void beansChanged() {
				statusbar.layoutBegin();
				textLabel.setText(getStatusBarText());
				statusbar.layoutEnd();
			}
		});

		this.statusBarItemModel = createStatusBarItemModel();
		this.statusBarItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				//TODO MG set status bar state
				table.setStatusBarVisible(statusBarItemModel.isSelected());
			}
		};
		statusBarItemModel.addItemListener(statusBarItemListener);
	}

	private String getStatusBarText() {
		final ArrayList<Integer> selection = model.getSelection();
		if (!EmptyCheck.isEmpty(selection)) {
			return MessageReplacer.replace(messageSelection, "" + (selection.get(0).intValue() + 1), "" + model.getSize());
		}
		else {
			return MessageReplacer.replace(messageNoSelection, "" + model.getSize());
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
