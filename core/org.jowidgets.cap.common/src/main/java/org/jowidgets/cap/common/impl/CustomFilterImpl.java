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

import java.io.Serializable;

import org.jowidgets.cap.common.api.filter.ICustomFilter;
import org.jowidgets.cap.common.api.filter.IOperator;

final class CustomFilterImpl implements ICustomFilter, Serializable {

	private static final long serialVersionUID = 8945938513423021180L;
	private final String filterType;
	private final String propertyName;
	private final IOperator operator;
	private final Object value;
	private final boolean inverted;

	CustomFilterImpl(
		final String filterType,
		final String propertyName,
		final IOperator operator,
		final Object value,
		final boolean inverted) {
		this.filterType = filterType;
		this.propertyName = propertyName;
		this.operator = operator;
		this.value = value;
		this.inverted = inverted;
	}

	@Override
	public String getFilterType() {
		return filterType;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public IOperator getOperator() {
		return operator;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean isInverted() {
		return inverted;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		if (inverted) {
			result.append("not (");
		}
		result.append("custom filter: ");
		result.append(filterType);
		result.append(' ');
		result.append(propertyName);
		result.append(' ');
		result.append(operator.getLabel());
		result.append(' ');
		result.append(value);
		if (inverted) {
			result.append(")");
		}
		return result.toString();
	}

}
