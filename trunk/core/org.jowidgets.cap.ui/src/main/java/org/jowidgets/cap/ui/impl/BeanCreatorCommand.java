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

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.Assert;
import org.jowidgets.util.event.IChangeListener;

final class BeanCreatorCommand implements ICommand, ICommandExecutor, IEnabledChecker {

	@SuppressWarnings("unused")
	private final IBeanListModel<?> model;
	@SuppressWarnings("unused")
	private final List<IAttribute<?>> attributes;
	@SuppressWarnings("unused")
	private final List<IEnabledChecker> enabledCheckers;
	@SuppressWarnings("unused")
	private final ICreatorService creatorService;
	@SuppressWarnings("unused")
	private final boolean anySelection;
	@SuppressWarnings("unused")
	private final IBeanExecptionConverter exceptionConverter;

	BeanCreatorCommand(
		final IBeanListModel<?> model,
		final List<IAttribute<?>> attributes,
		final List<IEnabledChecker> enabledCheckers,
		final boolean anySelection,
		final ICreatorService creatorService,
		final IBeanExecptionConverter exceptionConverter) {

		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(anySelection, "anySelection");
		Assert.paramNotNull(creatorService, "creatorService");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.model = model;
		this.attributes = new LinkedList<IAttribute<?>>(attributes);
		this.enabledCheckers = new LinkedList<IEnabledChecker>(enabledCheckers);
		this.creatorService = creatorService;
		this.anySelection = anySelection;
		this.exceptionConverter = exceptionConverter;
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return this;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public IEnabledState getEnabledState() {
		return EnabledState.ENABLED;
	}

	@Override
	public void addChangeListener(final IChangeListener listener) {

	}

	@Override
	public void removeChangeListener(final IChangeListener listener) {

	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		Toolkit.getMessagePane().showInfo(executionContext, "Not yet implemented");
	}

}
