/*
 * Copyright (c) 2011, nimoll
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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.impl.FilterBuilderImpl;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilter;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilter;

final class UiBooleanFilterBuilderImpl extends FilterBuilderImpl<IUiBooleanFilterBuilder> implements IUiBooleanFilterBuilder {

	private final List<IUiFilter> filters;
	private BooleanOperator operator;

	UiBooleanFilterBuilderImpl() {
		filters = new LinkedList<IUiFilter>();
	}

	@Override
	public IUiBooleanFilterBuilder setOperator(final BooleanOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public IUiBooleanFilterBuilder setFilters(final List<? extends IUiFilter> filters) {
		this.filters.clear();
		for (final IUiFilter filter : filters) {
			this.filters.add(filter);
		}
		return this;
	}

	@Override
	public IUiBooleanFilterBuilder addFilter(final IUiFilter filter) {
		filters.add(filter);
		return this;
	}

	@Override
	public boolean hasEntries() {
		return !filters.isEmpty();
	}

	@Override
	public IUiBooleanFilter build() {
		return new UiBooleanFilterImpl(operator, filters, isInverted());
	}

}
