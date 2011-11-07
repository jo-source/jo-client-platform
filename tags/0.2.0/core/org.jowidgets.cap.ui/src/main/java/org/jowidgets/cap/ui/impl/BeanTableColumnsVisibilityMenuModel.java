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

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.model.item.ISelectableItemModel;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.event.IChangeListener;

final class BeanTableColumnsVisibilityMenuModel extends MenuModel {

	BeanTableColumnsVisibilityMenuModel(final IBeanTableModel<?> model) {
		super(Messages.getString("BeanTableColumnsVisibilityMenuModel.visible_columns")); //$NON-NLS-1$

		final Map<String, IMenuModel> groupModels = new HashMap<String, IMenuModel>();
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			final IAttribute<?> attribute = model.getAttribute(columnIndex);

			final ICheckedItemModel checkedItem;
			if (attribute.getGroup() != null) {
				final IAttributeGroup attributeGroup = attribute.getGroup();
				IMenuModel groupMenuModel = groupModels.get(attributeGroup.getId());
				if (groupMenuModel == null) {
					groupMenuModel = addMenu(attributeGroup.getLabel());
					if (!EmptyCheck.isEmpty(attributeGroup.getDescription())) {
						groupMenuModel.setToolTipText(attributeGroup.getDescription());
					}
					groupModels.put(attributeGroup.getId(), groupMenuModel);
				}
				checkedItem = groupMenuModel.addCheckedItem(attribute.getCurrentLabel());
			}
			else {
				checkedItem = addCheckedItem(attribute.getCurrentLabel());
			}

			if (EmptyCheck.isEmpty(attribute.getDescription())) {
				checkedItem.setToolTipText(attribute.getDescription());
			}
			checkedItem.setSelected(attribute.isVisible());

			final AttributeBindingListener bindingListener = new AttributeBindingListener(checkedItem, attribute);
			checkedItem.addItemListener(bindingListener);
			attribute.addChangeListener(bindingListener);
		}

	}

	private final class AttributeBindingListener implements IItemStateListener, IChangeListener {

		private final ISelectableItemModel itemModel;
		private final IAttribute<?> attribute;

		private boolean onEvent;

		private AttributeBindingListener(final ISelectableItemModel itemModel, final IAttribute<?> attribute) {
			super();
			this.itemModel = itemModel;
			this.attribute = attribute;
			this.onEvent = false;
		}

		@Override
		public void changed() {
			if (!onEvent) {
				onEvent = true;
				itemModel.setText(attribute.getCurrentLabel());
				itemModel.setSelected(attribute.isVisible());
				onEvent = false;
			}
		}

		@Override
		public void itemStateChanged() {
			if (!onEvent) {
				onEvent = true;
				attribute.setVisible(itemModel.isSelected());
				onEvent = false;
			}
		}

	}
}
