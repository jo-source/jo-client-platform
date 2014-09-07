/*
 * Copyright (c) 2012, grossmann
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

import java.util.ArrayList;

import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.types.AutoScrollPolicy;
import org.jowidgets.util.Assert;

final class BeanTableConfigImpl implements IBeanTableConfig {

	private final ArrayList<Integer> columnPermutation;
	private final boolean autoUpdate;
	private final int autoUpdateInterval;
	private final AutoScrollPolicy autoScrollPolicy;
	private final boolean filterToolbarVisible;
	private final boolean searchFilterToolbarVisible;
	private final boolean statusBarVisible;
	private final boolean validationLabelVisible;

	BeanTableConfigImpl(
		final ArrayList<Integer> columnPermutation,
		final boolean autoUpdate,
		final int autoUpdateInterval,
		final AutoScrollPolicy autoScrollPolicy,
		final boolean filterToolbarVisible,
		final boolean searchFilterToolbarVisible,
		final boolean statusBarVisible,
		final boolean validationLabelVisible) {

		Assert.paramNotNull(columnPermutation, "columnPermutation");
		Assert.paramNotNull(autoScrollPolicy, "autoScrollPolicy");

		this.columnPermutation = columnPermutation;
		this.autoUpdate = autoUpdate;
		this.autoUpdateInterval = autoUpdateInterval;
		this.autoScrollPolicy = autoScrollPolicy;
		this.filterToolbarVisible = filterToolbarVisible;
		this.searchFilterToolbarVisible = searchFilterToolbarVisible;
		this.statusBarVisible = statusBarVisible;
		this.validationLabelVisible = validationLabelVisible;
	}

	@Override
	public ArrayList<Integer> getColumnPermutation() {
		return columnPermutation;
	}

	@Override
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	@Override
	public int getAutoUpdateInterval() {
		return autoUpdateInterval;
	}

	@Override
	public AutoScrollPolicy getAutoScrollPolicy() {
		return autoScrollPolicy;
	}

	@Override
	public boolean isFilterToolbarVisible() {
		return filterToolbarVisible;
	}

	@Override
	public boolean isSearchFilterToolbarVisible() {
		return searchFilterToolbarVisible;
	}

	@Override
	public boolean isStatusBarVisible() {
		return statusBarVisible;
	}

	@Override
	public boolean isValidationLabelVisible() {
		return validationLabelVisible;
	}

}
