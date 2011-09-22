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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilterBuilder;

final class ArithmeticPropertyFilterBuilderImpl extends FilterBuilderImpl<IArithmeticPropertyFilterBuilder> implements
		IArithmeticPropertyFilterBuilder {

	private String propertyName;
	private final List<String> rightHandPropertyNames;
	private ArithmeticOperator operator;

	ArithmeticPropertyFilterBuilderImpl() {
		rightHandPropertyNames = new LinkedList<String>();
	}

	@Override
	public IArithmeticPropertyFilterBuilder setPropertyName(final String propertyName) {
		this.propertyName = propertyName;
		return this;
	}

	@Override
	public IArithmeticPropertyFilterBuilder setRightHandPropertyNames(final String[] propertyNames) {
		rightHandPropertyNames.clear();
		for (final String propName : propertyNames) {
			rightHandPropertyNames.add(propName);
		}
		return this;
	}

	@Override
	public IArithmeticPropertyFilterBuilder setRightHandPropertyName(final String propertyName) {
		rightHandPropertyNames.clear();
		rightHandPropertyNames.add(propertyName);
		return this;
	}

	@Override
	public IArithmeticPropertyFilterBuilder addRightHandPropertyName(final String propertyName) {
		rightHandPropertyNames.add(propertyName);
		return this;
	}

	@Override
	public IArithmeticPropertyFilterBuilder setOperator(final ArithmeticOperator operator) {
		this.operator = operator;
		return this;
	}

	@Override
	public IArithmeticPropertyFilter build() {
		return new ArithmeticPropertyFilterImpl(
			propertyName,
			operator,
			rightHandPropertyNames.toArray(new String[rightHandPropertyNames.size()]),
			isInverted());
	}
}
