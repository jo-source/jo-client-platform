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
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.sort.IPropertySort;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.util.event.IChangeListener;

abstract class AbstractBeanTableSortMenuModel extends MenuModel {

	private final IRadioItemModel noSortItem;
	private final IRadioItemModel ascendingItem;
	private final IRadioItemModel descendingItem;
	private final ISortModel sortModel;
	private final IAttribute<?> attribute;
	private final IChangeListener sortModelChangeListener;
	private final IItemStateListener noSortItemListener;
	private final IItemStateListener ascendingItemListener;
	private final IItemStateListener descendingItemListener;

	AbstractBeanTableSortMenuModel(final String label, final IBeanTableModel<?> model, final int columnIndex) {
		//TODO i18n
		super(label);

		this.sortModel = model.getSortModel();

		this.noSortItem = addRadioItem("No sort");
		this.ascendingItem = addRadioItem("Ascending");
		this.descendingItem = addRadioItem("Descending");

		this.attribute = model.getAttribute(columnIndex);

		this.sortModelChangeListener = new IChangeListener() {
			@Override
			public void changed() {
				setCurrentSort();
			}
		};

		noSortItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				if (noSortItem.isSelected()) {
					sortModel.removeChangeListener(sortModelChangeListener);
					removeProperty(attribute.getPropertyName());
					sortModel.addChangeListener(sortModelChangeListener);
				}
			}
		};

		ascendingItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				if (ascendingItem.isSelected()) {
					sortModel.removeChangeListener(sortModelChangeListener);
					addOrSetProperty(attribute.getPropertyName(), SortOrder.ASC);
					sortModel.addChangeListener(sortModelChangeListener);
				}
			}
		};

		descendingItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				if (descendingItem.isSelected()) {
					sortModel.removeChangeListener(sortModelChangeListener);
					addOrSetProperty(attribute.getPropertyName(), SortOrder.DESC);
					sortModel.addChangeListener(sortModelChangeListener);
				}
			}
		};

		setCurrentSort();
	}

	ISortModel getSortModel() {
		return sortModel;
	}

	public abstract void removeProperty(String propertyName);

	public abstract void addOrSetProperty(String propertyName, SortOrder sortOrder);

	public abstract IPropertySort getPropertySort(String propertyName);

	private void setCurrentSort() {
		sortModel.removeChangeListener(sortModelChangeListener);
		noSortItem.removeItemListener(noSortItemListener);
		ascendingItem.removeItemListener(ascendingItemListener);
		descendingItem.removeItemListener(descendingItemListener);
		final IPropertySort sort = getPropertySort(attribute.getPropertyName());

		if (sort.getSortOrder() == SortOrder.ASC) {
			noSortItem.setSelected(false);
			ascendingItem.setSelected(true);
			descendingItem.setSelected(false);
		}
		else if (sort.getSortOrder() == SortOrder.DESC) {
			noSortItem.setSelected(false);
			ascendingItem.setSelected(false);
			descendingItem.setSelected(true);
		}
		else {
			noSortItem.setSelected(true);
			ascendingItem.setSelected(false);
			descendingItem.setSelected(false);
		}

		sortModel.addChangeListener(sortModelChangeListener);
		noSortItem.addItemListener(noSortItemListener);
		ascendingItem.addItemListener(ascendingItemListener);
		descendingItem.addItemListener(descendingItemListener);
	}

}
