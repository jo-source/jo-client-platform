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

package org.jowidgets.cap.ui.impl;

import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.command.ILinkActionBuilder;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

final class LinkActionBuilderImpl extends AbstractSingleUseBuilder<IAction> implements ILinkActionBuilder {

	private final IActionBuilder builder;

	LinkActionBuilderImpl(final IBeanListModel<?> model) {
		this.builder = Toolkit.getActionBuilderFactory().create();
	}

	@Override
	public ILinkActionBuilder setText(final String text) {
		checkExhausted();
		builder.setText(text);
		return this;
	}

	@Override
	public ILinkActionBuilder setToolTipText(final String toolTipText) {
		checkExhausted();
		builder.setToolTipText(toolTipText);
		return this;
	}

	@Override
	public ILinkActionBuilder setIcon(final IImageConstant icon) {
		checkExhausted();
		builder.setIcon(icon);
		return this;
	}

	@Override
	public ILinkActionBuilder setMnemonic(final Character mnemonic) {
		checkExhausted();
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public ILinkActionBuilder setMnemonic(final char mnemonic) {
		checkExhausted();
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public ILinkActionBuilder setAccelerator(final Accelerator accelerator) {
		checkExhausted();
		builder.setAccelerator(accelerator);
		return this;
	}

	@Override
	public ILinkActionBuilder setAccelerator(final char key, final Modifier... modifier) {
		checkExhausted();
		builder.setAccelerator(key, modifier);
		return this;
	}

	@Override
	public ILinkActionBuilder setLinkCreatorService(final ICreatorService creatorService) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setLinkableTableAttributes(final List<? extends IAttribute<?>> attributes) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setLinkableTableLabel(final String label) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setLinkableTableReaderService(final IReaderService<Void> readerService) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setMultiSelection(final boolean multiSelection) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setExceptionConverter(final IBeanExecptionConverter exceptionConverter) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder addExecutionInterceptor(final IExecutionInterceptor interceptor) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setSourceProperties(final IEntityLinkProperties properties) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setSourceProperties(final String keyPropertyName, final String foreignKeyPropertyname) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setDestinationProperties(final IEntityLinkProperties properties) {
		checkExhausted();

		return this;
	}

	@Override
	public ILinkActionBuilder setDestinationProperties(final String keyPropertyName, final String foreignKeyPropertyname) {
		checkExhausted();

		return this;
	}

	@Override
	protected IAction doBuild() {
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				Toolkit.getMessagePane().showInfo(executionContext, "Must be implemented");
			}
		});
		return builder.build();
	}
}
