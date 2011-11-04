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
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;

public interface ILinkActionBuilder {

	ILinkActionBuilder setText(String text);

	ILinkActionBuilder setToolTipText(final String toolTipText);

	ILinkActionBuilder setIcon(IImageConstant icon);

	ILinkActionBuilder setMnemonic(final Character mnemonic);

	ILinkActionBuilder setMnemonic(final char mnemonic);

	ILinkActionBuilder setAccelerator(Accelerator accelerator);

	ILinkActionBuilder setAccelerator(final char key, final Modifier... modifier);

	ILinkActionBuilder setLinkCreatorService(ICreatorService creatorService);

	ILinkActionBuilder setLinkableTableAttributes(List<? extends IAttribute<?>> attributes);

	ILinkActionBuilder setLinkableTableLabel(String label);

	ILinkActionBuilder setLinkableTableReaderService(IReaderService<Void> readerService);

	ILinkActionBuilder setSourceProperties(IEntityLinkProperties properties);

	ILinkActionBuilder setSourceProperties(String keyPropertyName, String foreignKeyPropertyname);

	ILinkActionBuilder setDestinationProperties(IEntityLinkProperties properties);

	ILinkActionBuilder setDestinationProperties(String keyPropertyName, String foreignKeyPropertyname);

	ILinkActionBuilder setMultiSelection(boolean multiSelection);

	ILinkActionBuilder addEnabledChecker(IEnabledChecker enabledChecker);

	ILinkActionBuilder setExceptionConverter(IBeanExecptionConverter exceptionConverter);

	ILinkActionBuilder addExecutionInterceptor(IExecutionInterceptor interceptor);

	IAction build();

}
