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

package org.jowidgets.cap.common.api.filter;

public enum ArithmeticOperator implements IOperator {

	EMPTY(Messages.getString("ArithmeticOperator.empty_short"), //$NON-NLS-1$ 
		Messages.getString("ArithmeticOperator.empty_long"), //$NON-NLS-1$ 
		null),
	EQUAL(Messages.getString("ArithmeticOperator.equal_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.equal_long"), //$NON-NLS-1$
		null),
	LESS(Messages.getString("ArithmeticOperator.less_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.less_long"), //$NON-NLS-1$
		null),
	LESS_EQUAL(Messages.getString("ArithmeticOperator.less_equal_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.less_equal_long"), //$NON-NLS-1$
		null),
	GREATER(Messages.getString("ArithmeticOperator.greater_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.greater_long"), //$NON-NLS-1$
		null),
	GREATER_EQUAL(Messages.getString("ArithmeticOperator.greaer_equal_short"), //$NON-NLS-1$ 
		Messages.getString("ArithmeticOperator.greater_equal_long"), //$NON-NLS-1$
		null),
	BETWEEN(Messages.getString("ArithmeticOperator.between_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.between_long"), //$NON-NLS-1$
		null),
	CONTAINS_ANY(Messages.getString("ArithmeticOperator.contains_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.contains_long"), //$NON-NLS-1$
		null),
	CONTAINS_ALL(Messages.getString("ArithmeticOperator.contains_all_short"), //$NON-NLS-1$
		Messages.getString("ArithmeticOperator.contains_all_long"), //$NON-NLS-1$
		null);

	private final String label;
	private final String labelLong;
	private final String description;

	private ArithmeticOperator(final String label, final String labelLong, final String description) {
		this.label = label;
		this.labelLong = labelLong;
		this.description = description;
	}

	@Override
	public Object getId() {
		return this;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getLabelLong() {
		return labelLong;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
