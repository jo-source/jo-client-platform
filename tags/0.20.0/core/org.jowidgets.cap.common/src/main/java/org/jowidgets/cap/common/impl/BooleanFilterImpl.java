/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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
import java.util.List;

import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.IFilter;

final class BooleanFilterImpl implements IBooleanFilter, Serializable {

	private static final long serialVersionUID = 595551125332442715L;
	private final BooleanOperator operator;
	private final List<IFilter> filters;
	private final boolean inverted;

	BooleanFilterImpl(final BooleanOperator operator, final List<IFilter> filters, final boolean inverted) {
		this.operator = operator;
		this.filters = filters;
		this.inverted = inverted;
	}

	@Override
	public boolean isInverted() {
		return inverted;
	}

	@Override
	public List<IFilter> getFilters() {
		return filters;
	}

	@Override
	public BooleanOperator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		if (filters.size() > 0) {
			int effectiveSize = 0;
			if (inverted) {
				result.append("not (");
			}
			for (final IFilter filter : filters) {
				result.append('(');
				result.append(filter.toString());
				result.append(')');
				effectiveSize = result.length();
				result.append(' ');
				result.append(operator.getLabel());
				result.append(' ');
			}
			result.setLength(effectiveSize);
			if (inverted) {
				result.append(')');
			}
		}
		return result.toString();
	}
}