/*
 * Copyright (c) 2011, Nikolaus Moll
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

package org.jowidgets.cap.common.impl;

import org.jowidgets.cap.common.api.filter.ICustomFilter;
import org.jowidgets.cap.common.api.filter.ICustomFilterBuilder;
import org.jowidgets.cap.common.api.filter.IOperator;

final class CustomFilterBuilderImpl extends FilterBuilderImpl<ICustomFilterBuilder> implements ICustomFilterBuilder {

	private String filterType;
	private String propertyName;
	private IOperator operator;
	private Object value;

	@Override
	public ICustomFilterBuilder setFilterType(final String filterType) {
		this.filterType = filterType;
		return this;
	}

	@Override
	public ICustomFilterBuilder setPropertyName(final String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	@Override
	public ICustomFilterBuilder setOperator(final IOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public ICustomFilterBuilder setValue(final Object value) {
		this.value = value;
		return this;
	}

	@Override
	public ICustomFilter build() {
		return new CustomFilterImpl(filterType, propertyName, operator, value, isInverted());
	}

}
