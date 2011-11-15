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
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.service.api.IServiceId;

public interface ICreatorActionBuilder<BEAN_TYPE> {

	ICreatorActionBuilder<BEAN_TYPE> setText(String text);

	/**
	 * Sets the entity label (singular).
	 * This will set a proper text with the entity label as a variable.
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	ICreatorActionBuilder<BEAN_TYPE> setEntityLabelSingular(String label);

	ICreatorActionBuilder<BEAN_TYPE> setToolTipText(final String toolTipText);

	ICreatorActionBuilder<BEAN_TYPE> setIcon(IImageConstant icon);

	ICreatorActionBuilder<BEAN_TYPE> setMnemonic(final Character mnemonic);

	ICreatorActionBuilder<BEAN_TYPE> setMnemonic(final char mnemonic);

	ICreatorActionBuilder<BEAN_TYPE> setAccelerator(Accelerator accelerator);

	ICreatorActionBuilder<BEAN_TYPE> setAccelerator(final char key, final Modifier... modifier);

	ICreatorActionBuilder<BEAN_TYPE> setBeanForm(IBeanFormBluePrint<BEAN_TYPE> beanForm);

	ICreatorActionBuilder<BEAN_TYPE> setBeanForm(List<? extends IAttribute<?>> attributes);

	ICreatorActionBuilder<BEAN_TYPE> setBeanPropertyValidator(IBeanPropertyValidator<BEAN_TYPE> beanValidator);

	ICreatorActionBuilder<BEAN_TYPE> setCreatorService(ICreatorService creatorService);

	ICreatorActionBuilder<BEAN_TYPE> setCreatorService(IServiceId<ICreatorService> creatorServiceId);

	ICreatorActionBuilder<BEAN_TYPE> setCreatorService(String creatorServiceId);

	ICreatorActionBuilder<BEAN_TYPE> setAnySelection(boolean anySelection);

	ICreatorActionBuilder<BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	ICreatorActionBuilder<BEAN_TYPE> setExceptionConverter(IBeanExecptionConverter exceptionConverter);

	ICreatorActionBuilder<BEAN_TYPE> addExecutionInterceptor(IExecutionInterceptor interceptor);

	IAction build();

}
