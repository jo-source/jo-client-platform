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

package org.jowidgets.cap.ui.tools.sort;

import java.util.List;

import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;
import org.jowidgets.cap.ui.api.sort.ISortModelConfigBuilder;

public final class SortModelConfigBuilder implements ISortModelConfigBuilder {

	private final ISortModelConfigBuilder builder;

	public SortModelConfigBuilder() {
		this.builder = CapUiToolkit.sortModelConfigBuilder();
	}

	@Override
	public ISortModelConfigBuilder setCurrentSorting(final List<ISort> sorting) {
		builder.setCurrentSorting(sorting);
		return this;
	}

	@Override
	public ISortModelConfigBuilder setDefaultSorting(final List<ISort> sorting) {
		builder.setDefaultSorting(sorting);
		return this;
	}

	@Override
	public ISortModelConfigBuilder addCurrentProperty(final String propertyName, final SortOrder order) {
		builder.addCurrentProperty(propertyName, order);
		return this;
	}

	@Override
	public ISortModelConfigBuilder addDefaultProperty(final String propertyName, final SortOrder order) {
		builder.addDefaultProperty(propertyName, order);
		return this;
	}

	@Override
	public ISortModelConfig build() {
		return builder.build();
	}

}
