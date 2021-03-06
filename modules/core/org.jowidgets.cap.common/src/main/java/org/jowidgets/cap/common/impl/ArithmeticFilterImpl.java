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
import java.util.Arrays;

import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;

final class ArithmeticFilterImpl implements IArithmeticFilter, Serializable {

	private static final long serialVersionUID = -1426438845445419132L;
	private final String propertyName;
	private final ArithmeticOperator operator;
	private final Object[] parameters;
	private final boolean inverted;

	ArithmeticFilterImpl(
		final String propertyName,
		final ArithmeticOperator operator,
		final Object[] parameters,
		final boolean inverted) {
		this.propertyName = propertyName;
		this.operator = operator;
		this.parameters = parameters;
		this.inverted = inverted;
	}

	@Override
	public boolean isInverted() {
		return inverted;
	}

	@Override
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public ArithmeticOperator getOperator() {
		return operator;
	}

	@Override
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		if (inverted) {
			result.append("not (");
		}
		result.append(propertyName);
		result.append(' ');
		result.append(operator.getLabel());
		result.append(' ');
		result.append('[');
		int effectiveSize = result.length();
		for (final Object o : parameters) {
			if (o == null) {
				result.append("<null>");
			}
			else {
				result.append(o.toString());
			}
			effectiveSize = result.length();
			result.append(", ");
		}
		result.setLength(effectiveSize);
		result.append(']');
		if (inverted) {
			result.append(")");
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (inverted ? 1231 : 1237);
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IArithmeticFilter)) {
			return false;
		}
		final IArithmeticFilter other = (IArithmeticFilter) obj;
		if (inverted != other.isInverted()) {
			return false;
		}
		if (operator != other.getOperator()) {
			return false;
		}
		if (!Arrays.equals(parameters, other.getParameters())) {
			return false;
		}
		if (propertyName == null) {
			if (other.getPropertyName() != null) {
				return false;
			}
		}
		else if (!propertyName.equals(other.getPropertyName())) {
			return false;
		}
		return true;
	}

}
