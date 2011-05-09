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

package org.jowidgets.cap.common.api.filter;

public enum ArithmeticOperator {

	EMPTY(0, false),
	EQUAL(1, false),
	LESS(1, false),
	LESS_EQUAL(1, false),
	GREATER(1, false),
	GREATER_EQUAL(1, false),
	BETWEEN(2, false),
	CONTAINS_ANY(1, true),
	CONTAINS_ALL(1, true);

	private final int parameterCount;
	private final boolean isCollectionOperator;

	private ArithmeticOperator(final int parameterCount, final boolean isCollectionOperator) {
		this.parameterCount = parameterCount;
		this.isCollectionOperator = isCollectionOperator;
	}

	/**
	 * @return the number of parameters this operator must have
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	/**
	 * @return true, if the right hand of the operator is a collection
	 */
	public boolean isCollectionOperator() {
		return isCollectionOperator;
	}

}
