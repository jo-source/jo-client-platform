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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.ui.api.sort.IPropertySort;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.util.Assert;
import org.jowidgets.util.event.ChangeObservable;

final class SortModelImpl extends ChangeObservable implements ISortModel {

	private final List<ISort> currentSorting;
	private final List<ISort> currentSortingView;
	private final List<ISort> defaultSorting;
	private final List<ISort> defaultSortingView;

	private boolean retentEvents;
	private boolean dirty;

	SortModelImpl() {
		this.currentSorting = new LinkedList<ISort>();
		this.currentSortingView = Collections.unmodifiableList(currentSorting);
		this.defaultSorting = new LinkedList<ISort>();
		this.defaultSortingView = Collections.unmodifiableList(defaultSorting);
		this.retentEvents = false;
	}

	@Override
	public List<ISort> getCurrentSorting() {
		return currentSortingView;
	}

	@Override
	public List<ISort> getDefaultSorting() {
		return defaultSortingView;
	}

	@Override
	public void modelModificationStart() {
		retentEvents = true;
	}

	@Override
	public void modelModificationEnd() {
		retentEvents = false;
		if (dirty) {
			fireChangedEvent();
			dirty = false;
		}
	}

	@Override
	public List<ISort> getSorting() {
		if (currentSorting.size() > 0) {
			return currentSortingView;
		}
		else {
			return defaultSortingView;
		}
	}

	@Override
	public void setConfig(final ISortModelConfig config) {
		Assert.paramNotNull(config, "config");
		setSorting(config.getDefaultSorting(), config.getCurrentSorting());
	}

	@Override
	public ISortModelConfig getConfig() {
		return new SortModelConfigImpl(defaultSorting, currentSorting);
	}

	@Override
	public void setSorting(final List<ISort> defaultSorting, final List<ISort> currentSorting) {
		Assert.paramNotNull(defaultSorting, "defaultSorting");
		Assert.paramNotNull(currentSorting, "currentSorting");
		this.defaultSorting.clear();
		this.currentSorting.clear();
		addToDefaultSorting(defaultSorting);
		addToCurrentSorting(currentSorting);
		modelChanged();
	}

	@Override
	public void setCurrentSorting(final List<ISort> sorting) {
		Assert.paramNotNull(sorting, "sorting");
		this.currentSorting.clear();
		addToCurrentSorting(sorting);
		modelChanged();
	}

	@Override
	public void setDefaultSorting(final List<ISort> sorting) {
		Assert.paramNotNull(sorting, "sorting");
		this.defaultSorting.clear();
		addToDefaultSorting(sorting);
		modelChanged();
	}

	@Override
	public void clearSorting() {
		this.defaultSorting.clear();
		this.currentSorting.clear();
		modelChanged();
	}

	@Override
	public void clearCurrentSorting() {
		this.defaultSorting.clear();
		this.currentSorting.clear();
		modelChanged();
	}

	@Override
	public void clearDefaultSorting() {
		this.defaultSorting.clear();
		this.currentSorting.clear();
		modelChanged();
	}

	@Override
	public void setCurrentProperty(final String propertyName, final SortOrder order) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(order, "order");
		this.currentSorting.clear();
		currentSorting.add(CapCommonToolkit.sortFactory().create(propertyName, order));
		modelChanged();
	}

	@Override
	public void addCurrentProperty(final String propertyName, final SortOrder order) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(order, "order");
		currentSorting.add(CapCommonToolkit.sortFactory().create(propertyName, order));
		modelChanged();
	}

	@Override
	public void addOrSetCurrentProperty(final String propertyName, final SortOrder order) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(order, "order");
		addOrSetCurrentProperty(propertyName, order, currentSorting);
		modelChanged();
	}

	@Override
	public void removeCurrentProperty(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		removeProperty(propertyName, currentSorting);
		modelChanged();
	}

	@Override
	public void setDefaultProperty(final String propertyName, final SortOrder order) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(order, "order");
		this.defaultSorting.clear();
		defaultSorting.add(CapCommonToolkit.sortFactory().create(propertyName, order));
		modelChanged();
	}

	@Override
	public void addDefaultProperty(final String propertyName, final SortOrder order) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(order, "order");
		defaultSorting.add(CapCommonToolkit.sortFactory().create(propertyName, order));
		modelChanged();
	}

	@Override
	public void addOrSetDefaultProperty(final String propertyName, final SortOrder order) {
		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(order, "order");
		addOrSetCurrentProperty(propertyName, order, defaultSorting);
		modelChanged();
	}

	@Override
	public void removeDefaultProperty(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		removeProperty(propertyName, defaultSorting);
		modelChanged();
	}

	@Override
	public void setOrToggleCurrentProperty(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		final IPropertySort propertySort = findPropertySort(propertyName, currentSorting);
		currentSorting.clear();
		if (!propertySort.isSorted()) {
			currentSorting.add(CapCommonToolkit.sortFactory().create(propertyName, SortOrder.ASC));
		}
		else {
			if (propertySort.getSortOrder() == SortOrder.ASC) {
				currentSorting.add(CapCommonToolkit.sortFactory().create(propertyName, SortOrder.DESC));
			}
		}
		modelChanged();
	}

	@Override
	public void addOrToggleCurrentProperty(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		final IPropertySort propertySort = findPropertySort(propertyName, currentSorting);
		if (!propertySort.isSorted()) {
			currentSorting.add(CapCommonToolkit.sortFactory().create(propertyName, SortOrder.ASC));
		}
		else {
			if (propertySort.getSortOrder() == SortOrder.DESC) {
				currentSorting.remove(propertySort.getSortIndex());
			}
			else {
				currentSorting.remove(propertySort.getSortIndex());
				currentSorting.add(
						propertySort.getSortIndex(),
						CapCommonToolkit.sortFactory().create(propertyName, SortOrder.DESC));
			}
		}
		modelChanged();
	}

	@Override
	public IPropertySort getCurrentPropertySort(final String propertyName) {
		return findPropertySort(propertyName, currentSorting);
	}

	@Override
	public IPropertySort getDefaultPropertySort(final String propertyName) {
		return findPropertySort(propertyName, defaultSorting);
	}

	@Override
	public IPropertySort getPropertySort(final String propertyName) {
		if (!currentSorting.isEmpty()) {
			return getCurrentPropertySort(propertyName);
		}
		else {
			return getDefaultPropertySort(propertyName);
		}
	}

	@Override
	public String toString() {
		return "SortModelImpl [currentSorting=" + currentSorting + ", defaultSorting=" + defaultSorting + "]";
	}

	private void addToCurrentSorting(final List<ISort> sorting) {
		for (final ISort sort : sorting) {
			currentSorting.add(CapCommonToolkit.sortFactory().create(sort.getPropertyName(), sort.getSortOrder()));
		}
	}

	private void addToDefaultSorting(final List<ISort> sorting) {
		for (final ISort sort : sorting) {
			defaultSorting.add(CapCommonToolkit.sortFactory().create(sort.getPropertyName(), sort.getSortOrder()));
		}
	}

	private void removeProperty(final String propertyName, final List<ISort> sorting) {
		for (final ISort sort : new LinkedList<ISort>(sorting)) {
			if (propertyName.equals(sort.getPropertyName())) {
				sorting.remove(sort);
				break;
			}
		}
	}

	private void addOrSetCurrentProperty(final String propertyName, final SortOrder order, final List<ISort> sorting) {
		final int index = findSort(propertyName, sorting);
		if (index == -1) {
			sorting.add(CapCommonToolkit.sortFactory().create(propertyName, order));
		}
		else {
			sorting.remove(index);
			sorting.add(index, CapCommonToolkit.sortFactory().create(propertyName, order));
		}
	}

	private int findSort(final String propertyName, final List<ISort> sorting) {
		int index = 0;
		for (final ISort sort : sorting) {
			if (sort.getPropertyName().equals(propertyName)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private IPropertySort findPropertySort(final String propertyName, final List<ISort> sorting) {
		int index = 0;
		for (final ISort sort : sorting) {
			if (sort.getPropertyName().equals(propertyName)) {
				return new PropertySortImpl(index, sort.getSortOrder());
			}
			index++;
		}
		return new PropertySortImpl();
	}

	private void modelChanged() {
		if (retentEvents) {
			this.dirty = true;
		}
		else {
			fireChangedEvent();
		}
	}

}
