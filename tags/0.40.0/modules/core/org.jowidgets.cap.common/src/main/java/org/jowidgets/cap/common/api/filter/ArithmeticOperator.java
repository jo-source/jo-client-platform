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

import org.jowidgets.i18n.api.IMessage;

public enum ArithmeticOperator implements IOperator {

	EMPTY(Messages.getMessage("ArithmeticOperator.empty_short"), Messages.getMessage("ArithmeticOperator.empty_long")),

	EQUAL(Messages.getMessage("ArithmeticOperator.equal_short"), Messages.getMessage("ArithmeticOperator.equal_long")),

	LESS(Messages.getMessage("ArithmeticOperator.less_short"), Messages.getMessage("ArithmeticOperator.less_long")),

	LESS_EQUAL(
		Messages.getMessage("ArithmeticOperator.less_equal_short"),
		Messages.getMessage("ArithmeticOperator.less_equal_long")),

	GREATER(Messages.getMessage("ArithmeticOperator.greater_short"), Messages.getMessage("ArithmeticOperator.greater_long")),

	GREATER_EQUAL(
		Messages.getMessage("ArithmeticOperator.greaer_equal_short"),
		Messages.getMessage("ArithmeticOperator.greater_equal_long")),

	BETWEEN(Messages.getMessage("ArithmeticOperator.between_short"), Messages.getMessage("ArithmeticOperator.between_long")),

	CONTAINS_ANY(
		Messages.getMessage("ArithmeticOperator.contains_short"),
		Messages.getMessage("ArithmeticOperator.contains_long")),

	CONTAINS_ALL(
		Messages.getMessage("ArithmeticOperator.contains_all_short"),
		Messages.getMessage("ArithmeticOperator.contains_all_long"));

	private final IMessage label;
	private final IMessage labelLong;

	private ArithmeticOperator(final IMessage label, final IMessage labelLong) {
		this.label = label;
		this.labelLong = labelLong;
	}

	@Override
	public Object getId() {
		return this;
	}

	@Override
	public String getLabel() {
		return label.get();
	}

	@Override
	public String getLabelLong() {
		return labelLong.get();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isInvertible() {
		return true;
	}

}
