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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.filter.IFilterPanelProvider;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterSupportBuilder;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.util.Assert;

@SuppressWarnings({"unchecked", "rawtypes"})
final class FilterSupportBuilderImpl<ELEMENT_VALUE_TYPE> implements IFilterSupportBuilder<ELEMENT_VALUE_TYPE> {

	private final List providers;

	private IIncludingFilterFactory<ELEMENT_VALUE_TYPE> includingFilterFactory;

	FilterSupportBuilderImpl() {
		this.providers = new LinkedList();
	}

	@Override
	public IFilterSupportBuilder<ELEMENT_VALUE_TYPE> addFilterPanelProvider(
		final IFilterPanelProvider<? extends IOperator> provider) {
		Assert.paramNotNull(provider, "provider");
		providers.add(provider);
		return this;
	}

	@Override
	public IFilterSupportBuilder<ELEMENT_VALUE_TYPE> setIncludingFilterFactory(
		final IIncludingFilterFactory<ELEMENT_VALUE_TYPE> includingFilterFactory) {
		Assert.paramNotNull(includingFilterFactory, "includingFilterFactory");
		this.includingFilterFactory = includingFilterFactory;
		return null;
	}

	@Override
	public IFilterSupport<ELEMENT_VALUE_TYPE> build() {
		return new FilterSupportImpl<ELEMENT_VALUE_TYPE>(providers, includingFilterFactory);
	}

}
