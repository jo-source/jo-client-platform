/*
 * Copyright (c) 2014, grossmann
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

package org.jowidgets.cap.ui.api.command;

import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionTransferableFactory;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;

public interface ICopyActionBuilder<BEAN_TYPE> extends ICapActionBuilder<ICopyActionBuilder<BEAN_TYPE>> {

	/**
	 * Sets the entity label singular.
	 * This will set a proper text with the entity label as a variable
	 * if the selection mode is single selection.
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ICopyActionBuilder<BEAN_TYPE> setEntityLabelSingular(String label);

	/**
	 * Sets the entity label plural.
	 * This will set a proper text with the entity label as a variable
	 * if the selection mode is multi selection
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ICopyActionBuilder<BEAN_TYPE> setEntityLabelPlural(String label);

	ICopyActionBuilder<BEAN_TYPE> setTransferableFactory(IBeanSelectionTransferableFactory<BEAN_TYPE> factory);

	ICopyActionBuilder<BEAN_TYPE> setMultiSelectionPolicy(boolean multiSelection);

	ICopyActionBuilder<BEAN_TYPE> setMessageStatePolicy(BeanMessageStatePolicy policy);

	ICopyActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	ICopyActionBuilder<BEAN_TYPE> addExecutableChecker(IExecutableChecker<BEAN_TYPE> executableChecker);

}
