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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.model.item.IRadioItemModel;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.control.DisplayFormat;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.util.ValueHolder;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableHeaderFormatMenuModel extends MenuModel {

	BeanTableHeaderFormatMenuModel(final IBeanTableModel<?> model, final int columnIndex) {
		super(Messages.getString("BeanTableHeaderFormatMenuModel.header_format")); //$NON-NLS-1$
		final IRadioItemModel shortRadioItem = addRadioItem(DisplayFormat.SHORT.getName());
		final IRadioItemModel longRadioItem = addRadioItem(DisplayFormat.LONG.getName());
		final IAttribute<?> attribute = model.getAttribute(columnIndex);
		if (attribute.getLabelDisplayFormat() == DisplayFormat.SHORT) {
			shortRadioItem.setSelected(true);
		}
		else {
			longRadioItem.setSelected(true);
		}
		addRadioItemListener(shortRadioItem, attribute, DisplayFormat.SHORT);
		addRadioItemListener(longRadioItem, attribute, DisplayFormat.LONG);
	}

	private static void addRadioItemListener(
		final IRadioItemModel radioItemModel,
		final IAttribute<?> attribute,
		final DisplayFormat displayFormat) {

		final ValueHolder<IItemStateListener> itemStateListenerHolder = new ValueHolder<IItemStateListener>();
		final ValueHolder<IChangeListener> changeListenerHolder = new ValueHolder<IChangeListener>();

		itemStateListenerHolder.set(new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				if (!attribute.getLabelDisplayFormat().equals(displayFormat) && radioItemModel.isSelected()) {
					attribute.removeChangeListener(changeListenerHolder.get());
					attribute.setLabelDisplayFormat(displayFormat);
					attribute.addChangeListener(changeListenerHolder.get());
				}
			}
		});

		changeListenerHolder.set(new IChangeListener() {
			@Override
			public void changed() {
				if (attribute.getLabelDisplayFormat().equals(displayFormat) && !radioItemModel.isSelected()) {
					radioItemModel.removeItemListener(itemStateListenerHolder.get());
					radioItemModel.setSelected(true);
					radioItemModel.addItemListener(itemStateListenerHolder.get());
				}

			}
		});

		radioItemModel.addItemListener(itemStateListenerHolder.get());
		attribute.addChangeListener(changeListenerHolder.get());
	}
}
