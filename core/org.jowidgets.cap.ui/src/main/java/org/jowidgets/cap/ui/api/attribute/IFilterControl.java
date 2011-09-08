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

package org.jowidgets.cap.ui.api.attribute;

import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.common.api.filter.IOperator;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;

public interface IFilterControl<OPERATOR_TYPE extends IOperator, CONFIG_TYPE> extends
		IInputControl<IUiConfigurableFilter<CONFIG_TYPE>> {

	/**
	 * Try to set a operand (this may be one from another control).
	 * If the operand is type compatible, the operand will be set in the control.
	 * If the operand is not compatible, nothing happens
	 * (particularly NO exception must be thrown)
	 * 
	 * @param value The new (potential) value to set
	 */
	void trySetOperand(Object value);

	/**
	 * Gets the current operand value. Maybe this will be set later on this
	 * control or another control.
	 * 
	 * @return the current operands value
	 */
	Object getOperand();

	/**
	 * The the operator for this control
	 * 
	 * @param operator The operator to set
	 */
	void setOperator(OPERATOR_TYPE operator);

	void setConfig(CONFIG_TYPE config);

}
