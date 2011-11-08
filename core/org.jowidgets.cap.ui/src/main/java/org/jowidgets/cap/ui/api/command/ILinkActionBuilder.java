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

import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;

public interface ILinkActionBuilder<BEAN_TYPE> {

	ILinkActionBuilder<BEAN_TYPE> setText(String text);

	/**
	 * Sets the entity label plural.
	 * This will set a proper text with the entity label as a variable
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ILinkActionBuilder<BEAN_TYPE> setDestinationEntityLabelPlural(String label);

	ILinkActionBuilder<BEAN_TYPE> setToolTipText(final String toolTipText);

	ILinkActionBuilder<BEAN_TYPE> setIcon(IImageConstant icon);

	ILinkActionBuilder<BEAN_TYPE> setMnemonic(final Character mnemonic);

	ILinkActionBuilder<BEAN_TYPE> setMnemonic(final char mnemonic);

	ILinkActionBuilder<BEAN_TYPE> setAccelerator(Accelerator accelerator);

	ILinkActionBuilder<BEAN_TYPE> setAccelerator(final char key, final Modifier... modifier);

	ILinkActionBuilder<BEAN_TYPE> setLinkCreatorService(ICreatorService creatorService);

	ILinkActionBuilder<BEAN_TYPE> setLinkableTableAttributes(List<? extends IAttribute<?>> attributes);

	ILinkActionBuilder<BEAN_TYPE> setLinkableTableEntityId(Object id);

	ILinkActionBuilder<BEAN_TYPE> setLinkableTableLabel(String label);

	ILinkActionBuilder<BEAN_TYPE> setLinkableTableReaderService(IReaderService<Void> readerService);

	ILinkActionBuilder<BEAN_TYPE> setSourceProperties(IEntityLinkProperties properties);

	ILinkActionBuilder<BEAN_TYPE> setSourceProperties(String keyPropertyName, String foreignKeyPropertyName);

	ILinkActionBuilder<BEAN_TYPE> setDestinationProperties(IEntityLinkProperties properties);

	ILinkActionBuilder<BEAN_TYPE> setDestinationProperties(String keyPropertyName, String foreignKeyPropertyName);

	ILinkActionBuilder<BEAN_TYPE> setMultiSelection(boolean multiSelection);

	ILinkActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	ILinkActionBuilder<BEAN_TYPE> setModificationPolicy(BeanModificationStatePolicy policy);

	ILinkActionBuilder<BEAN_TYPE> setMessageStatePolicy(BeanMessageStatePolicy policy);

	ILinkActionBuilder<BEAN_TYPE> addExecutableChecker(IExecutableChecker<BEAN_TYPE> executableChecker);

	ILinkActionBuilder<BEAN_TYPE> setExceptionConverter(IBeanExecptionConverter exceptionConverter);

	ILinkActionBuilder<BEAN_TYPE> addExecutionInterceptor(IExecutionInterceptor interceptor);

	IAction build();

}
