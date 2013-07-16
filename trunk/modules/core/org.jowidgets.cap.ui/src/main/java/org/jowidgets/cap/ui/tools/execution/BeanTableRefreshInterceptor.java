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

package org.jowidgets.cap.ui.tools.execution;

import java.util.List;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.util.EmptyCheck;

public final class BeanTableRefreshInterceptor<BEAN_TYPE, RESULT_TYPE> extends ExecutionInterceptorAdapter<RESULT_TYPE> {

	private final IBeanTableModel<BEAN_TYPE> tableModel;

	private List<IBeanProxy<BEAN_TYPE>> lastSelection;

	public BeanTableRefreshInterceptor(final IBeanTableModel<BEAN_TYPE> tableModel) {
		this.tableModel = tableModel;
	}

	@Override
	public void beforeExecution(final IExecutionContext executionContext, final IVetoable continueExecution) {
		lastSelection = tableModel.getSelectedBeans();
	}

	@Override
	public void onExecutionVeto(final IExecutionContext executionContext) {
		lastSelection = null;
	}

	@Override
	public void afterExecutionSuccess(final IExecutionContext executionContext, final RESULT_TYPE result) {
		if (!EmptyCheck.isEmpty(lastSelection)) {
			tableModel.refreshBeans(lastSelection);
		}
		lastSelection = null;
	}

}
