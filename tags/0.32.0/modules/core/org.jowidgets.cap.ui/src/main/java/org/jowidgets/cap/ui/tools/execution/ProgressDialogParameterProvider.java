/*
 * Copyright (c) 2012, grossmann, Florian Schaefer
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

package org.jowidgets.cap.ui.tools.execution;

import java.util.List;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IWindow;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IParameterProvider;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialog;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialogBluePrint;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Some;

/**
 * This class implements an experimental workaround that shows a (non modal) progress dialog for executions.
 * 
 * To use is, an instance of this class must be added as parameter provider (it should be the last) and
 * the same instance must be added as execution interceptor.
 * 
 * REMARK: At the moment this works only for SingleSelection actions
 * REMARK: This class will be removed later, if the progress dialog issue was integrated to the api
 * 
 * @param <BEAN_TYPE>
 * @param <PARAMETER_TYPE>
 */
public class ProgressDialogParameterProvider<BEAN_TYPE, PARAMETER_TYPE> extends ExecutionInterceptorAdapter<List<IBeanDto>> implements
		IParameterProvider<BEAN_TYPE, PARAMETER_TYPE>,
		IExecutionInterceptor<List<IBeanDto>> {

	private IExecutionTaskDialog executionTaskDialog;

	@Override
	public void afterExecutionSuccess(final IExecutionContext executionContext, final List<IBeanDto> result) {
		executionTaskDialog.executionFinished(executionContext.getAction().getText() + " - finished");
	}

	@Override
	public void afterExecutionError(final IExecutionContext executionContext, final Throwable error) {
		executionTaskDialog.executionError("Error while - " + executionContext.getAction().getText());
	}

	@Override
	public IMaybe<PARAMETER_TYPE> getParameter(
		final IExecutionContext executionContext,
		final List<IBeanProxy<BEAN_TYPE>> beans,
		final PARAMETER_TYPE defaultParameter) throws Exception {
		showProgress(executionContext, beans);
		return new Some<PARAMETER_TYPE>(defaultParameter);
	}

	private void showProgress(final IExecutionContext executionContext, final List<IBeanProxy<BEAN_TYPE>> beans) {
		if (beans.size() > 0) {
			final IExecutionTask executionTask = beans.iterator().next().getExecutionTask();
			if (executionTask != null) {
				final IWindow activeWindow = Toolkit.getActiveWindow();
				final IExecutionTaskDialogBluePrint dialogBp = CapUiToolkit.bluePrintFactory().executionTaskDialog(executionTask);
				dialogBp.setModal(false);
				dialogBp.setExecutionContext(executionContext);
				executionTaskDialog = activeWindow.createChildWindow(dialogBp);
				executionTaskDialog.setVisible(true);
			}
		}
	}

}
