/*
 * Copyright (c) 2011, H.Westphal
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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.service.api.IServiceId;

public interface IDeleterActionBuilder<BEAN_TYPE> {

	IDeleterActionBuilder<BEAN_TYPE> setText(String text);

	/**
	 * Sets the entity label singular.
	 * This will set a proper text with the entity label as a variable
	 * if the selection mode is single selection.
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	IDeleterActionBuilder<BEAN_TYPE> setEntityLabelSingular(String label);

	/**
	 * Sets the entity label plural.
	 * This will set a proper text with the entity label as a variable
	 * if the selection mode is multi selection
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	IDeleterActionBuilder<BEAN_TYPE> setEntityLabelPlural(String label);

	IDeleterActionBuilder<BEAN_TYPE> setToolTipText(final String toolTipText);

	IDeleterActionBuilder<BEAN_TYPE> setIcon(IImageConstant icon);

	IDeleterActionBuilder<BEAN_TYPE> setMnemonic(final Character mnemonic);

	IDeleterActionBuilder<BEAN_TYPE> setMnemonic(final char mnemonic);

	IDeleterActionBuilder<BEAN_TYPE> setAccelerator(Accelerator accelerator);

	IDeleterActionBuilder<BEAN_TYPE> setAccelerator(final char key, final Modifier... modifier);

	IDeleterActionBuilder<BEAN_TYPE> setDeleterService(IDeleterService deleterService);

	IDeleterActionBuilder<BEAN_TYPE> setDeleterService(IServiceId<IDeleterService> deleterServiceId);

	IDeleterActionBuilder<BEAN_TYPE> setDeleterService(String deleterServiceId);

	IDeleterActionBuilder<BEAN_TYPE> setMultiSelectionPolicy(boolean multiSelection);

	IDeleterActionBuilder<BEAN_TYPE> setMessageStatePolicy(BeanMessageStatePolicy policy);

	IDeleterActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	IDeleterActionBuilder<BEAN_TYPE> addExecutableChecker(IExecutableChecker<BEAN_TYPE> executableChecker);

	IDeleterActionBuilder<BEAN_TYPE> addExecutionInterceptor(IExecutionInterceptor interceptor);

	IDeleterActionBuilder<BEAN_TYPE> setExceptionConverter(IBeanExceptionConverter exceptionConverter);

	/**
	 * If auto selection is set, after bean deletion was delegated to the deleter service, the
	 * next bean (the bean after the last selected) will be selected automatically
	 * 
	 * @param autoSelection
	 * 
	 * @return This builder
	 */
	IDeleterActionBuilder<BEAN_TYPE> setAutoSelection(boolean autoSelection);

	IDeleterActionBuilder<BEAN_TYPE> setDeletionConfirmDialog(boolean deletionConfirmDialog);

	IAction build();

}
