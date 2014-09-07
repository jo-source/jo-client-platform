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
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.types.AutoScrollPolicy;
import org.jowidgets.util.Assert;

final class BeanTableConfigBuilderImpl implements IBeanTableConfigBuilder {

	private ArrayList<Integer> columnPermutation;
	private boolean autoUpdate;
	private int autoUpdateInterval;
	private AutoScrollPolicy autoScrollPolicy;
	private boolean filterToolbarVisible;
	private boolean searchFilterToolbarVisible;
	private boolean statusBarVisible;
	private boolean validationLabelVisible;

	BeanTableConfigBuilderImpl() {
		this.autoUpdate = false;
		this.autoUpdateInterval = 1000;
		this.autoScrollPolicy = AutoScrollPolicy.OFF;
	}

	@Override
	public IBeanTableConfigBuilder setConfig(final IBeanTableConfig config) {
		Assert.paramNotNull(config, "config");
		setColumnPermutation(config.getColumnPermutation());
		setAutoUpdate(config.isAutoUpdate());
		setAutoUpdateInterval(config.getAutoUpdateInterval());
		setAutoScrollPolicy(config.getAutoScrollPolicy());
		setFilterToolbarVisible(config.isFilterToolbarVisible());
		setSearchFilterToolbarVisible(config.isSearchFilterToolbarVisible());
		setStatusBarVisible(config.isStatusBarVisible());
		setStatusBarVisible(config.isStatusBarVisible());
		setValidationLabelVisible(config.isValidationLabelVisible());
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setAutoUpdate(final boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setAutoUpdateInterval(final int autoUpdateInterval) {
		this.autoUpdateInterval = autoUpdateInterval;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setAutoScrollPolicy(final AutoScrollPolicy autoScrollPolicy) {
		Assert.paramNotNull(autoScrollPolicy, "autoScrollPolicy");
		this.autoScrollPolicy = autoScrollPolicy;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setColumnPermutation(final ArrayList<Integer> columnPermutation) {
		Assert.paramNotNull(columnPermutation, "columnPermutation");
		this.columnPermutation = columnPermutation;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setFilterToolbarVisible(final boolean visible) {
		this.filterToolbarVisible = visible;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setSearchFilterToolbarVisible(final boolean visible) {
		this.searchFilterToolbarVisible = visible;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setStatusBarVisible(final boolean visible) {
		this.statusBarVisible = visible;
		return this;
	}

	@Override
	public IBeanTableConfigBuilder setValidationLabelVisible(final boolean visible) {
		this.validationLabelVisible = visible;
		return this;
	}

	@Override
	public IBeanTableConfig build() {
		return new BeanTableConfigImpl(
			columnPermutation,
			autoUpdate,
			autoUpdateInterval,
			autoScrollPolicy,
			filterToolbarVisible,
			searchFilterToolbarVisible,
			statusBarVisible,
			validationLabelVisible);
	}
}
