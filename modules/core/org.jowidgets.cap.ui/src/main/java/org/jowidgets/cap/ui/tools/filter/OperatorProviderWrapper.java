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

package org.jowidgets.cap.ui.tools.filter;

import java.util.List;

import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.util.Assert;

public class OperatorProviderWrapper<OPERATOR_TYPE extends IOperator> implements IOperatorProvider<OPERATOR_TYPE> {

	private final IOperatorProvider<OPERATOR_TYPE> original;

	public OperatorProviderWrapper(final IOperatorProvider<OPERATOR_TYPE> original) {
		Assert.paramNotNull(original, "original");
		this.original = original;
	}

	@Override
	public List<OPERATOR_TYPE> getOperators() {
		return original.getOperators();
	}

	@Override
	public OPERATOR_TYPE getDefaultOperator() {
		return original.getDefaultOperator();
	}

	@Override
	public boolean isInvertible(final OPERATOR_TYPE operator) {
		return original.isInvertible(operator);
	}

}
