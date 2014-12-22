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

package org.jowidgets.cap.ui.tools.command;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.model.DataModelChangeType;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IDataModelContextProvider;
import org.jowidgets.cap.ui.tools.model.DataModelContextExecutor;
import org.jowidgets.util.Assert;

public final class DataModelContextCommandWrapper implements ICommand {

	private final ICommandExecutor executor;
	private final IEnabledChecker enabledChecker;
	private final IExceptionHandler exceptionHandler;

	public DataModelContextCommandWrapper(
		final IDataModelContextProvider dataModelContextProvider,
		final DataModelChangeType changeType,
		final ICommand command) {

		this(
			dataModelContextProvider,
			changeType,
			command.getCommandExecutor(),
			command.getEnabledChecker(),
			command.getExceptionHandler());
	}

	public DataModelContextCommandWrapper(
		final IDataModelContextProvider dataModelContextProvider,
		final DataModelChangeType changeType,
		final ICommandExecutor executor,
		final IEnabledChecker enabledChecker,
		final IExceptionHandler exceptionHandler) {

		Assert.paramNotNull(dataModelContextProvider, "dataModelContextProvider");
		Assert.paramNotNull(executor, "executor");

		this.executor = new DataModelContextCommandExecutor(dataModelContextProvider, changeType, executor);
		this.enabledChecker = enabledChecker;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return executor;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return enabledChecker;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	private final class DataModelContextCommandExecutor implements ICommandExecutor {

		private final IDataModelContext context;
		private final DataModelChangeType changeType;
		private final ICommandExecutor original;

		private DataModelContextCommandExecutor(
			final IDataModelContextProvider dataModelContextProvider,
			final DataModelChangeType changeType,
			final ICommandExecutor original) {

			this.context = dataModelContextProvider.getDataModelContext();
			this.changeType = changeType;
			this.original = original;
		}

		@Override
		public void execute(final IExecutionContext executionContext) throws Exception {
			if (isEnabled()) {
				DataModelContextExecutor.execute(context, changeType, new Runnable() {
					@Override
					public void run() {
						if (isEnabled()) {
							try {
								original.execute(executionContext);
							}
							catch (final Exception e) {
								if (exceptionHandler != null) {
									try {
										exceptionHandler.handleException(executionContext, e);
									}
									catch (final Exception e1) {
										throw new RuntimeException(e1);
									}
								}
								else {
									throw new RuntimeException(e);
								}
							}
						}
					}
				});
			}
		}

		private boolean isEnabled() {
			if (enabledChecker != null) {
				return enabledChecker.getEnabledState().isEnabled();
			}
			else {
				return true;
			}
		}

	}

}
