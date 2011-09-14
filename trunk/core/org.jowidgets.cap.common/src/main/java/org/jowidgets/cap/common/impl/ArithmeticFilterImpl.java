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

}
