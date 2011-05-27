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

package org.jowidgets.cap.ui.impl.command;

import java.util.HashSet;
import java.util.Set;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.controler.IChangeListener;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.tools.command.EnabledChecker;
import org.jowidgets.util.Assert;

abstract class AbstractDataModelCommand implements ICommand, ICommandExecutor, IEnabledChecker {

	private final Set<IDataModel> dataModels;
	private final EnabledChecker enabledChecker;
	private final IModificationStateListener modificationStateListener;

	private boolean lastModifications;

	AbstractDataModelCommand() {
		this.dataModels = new HashSet<IDataModel>();
		this.enabledChecker = new EnabledChecker();
		enabledChecker.setEnabledState(EnabledState.DISABLED);

		this.lastModifications = false;
		this.modificationStateListener = new IModificationStateListener() {
			@Override
			public void modificationStateChanged() {
				boolean modifications = false;
				for (final IDataModel dataModel : dataModels) {
					modifications = modifications || dataModel.hasModifications();
				}
				if (lastModifications != modifications) {
					lastModifications = modifications;
					if (modifications) {
						enabledChecker.setEnabledState(EnabledState.ENABLED);
					}
					else {
						//TODO MG i18n
						enabledChecker.setEnabledState(EnabledState.disabled("There is no data modified"));
					}
				}
			}
		};
	}

	abstract void execute(IDataModel dataModel, final IExecutionContext executionContext);

	final void addDataModel(final IDataModel dataModel) {
		Assert.paramNotNull(dataModel, "dataModel");
		if (!dataModels.contains(dataModel)) {
			dataModels.add(dataModel);
			dataModel.addModificationStateListener(modificationStateListener);
		}
	}

	final void removeDataModel(final IDataModel dataModel) {
		Assert.paramNotNull(dataModel, "dataModel");
		if (dataModels.contains(dataModel)) {
			dataModels.remove(dataModel);
			dataModel.removeModificationStateListener(modificationStateListener);
		}
	}

	@Override
	public final ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public final IEnabledChecker getEnabledChecker() {
		return this;
	}

	@Override
	public final IEnabledState getEnabledState() {
		return enabledChecker.getEnabledState();
	}

	@Override
	public final void addChangeListener(final IChangeListener listener) {
		enabledChecker.addChangeListener(listener);
	}

	@Override
	public final void removeChangeListener(final IChangeListener listener) {
		enabledChecker.removeChangeListener(listener);
	}

	@Override
	public final void execute(final IExecutionContext executionContext) throws Exception {
		for (final IDataModel dataModel : dataModels) {
			execute(dataModel, executionContext);
		}
	}

	@Override
	public final IExceptionHandler getExceptionHandler() {
		return null;
	}

}
