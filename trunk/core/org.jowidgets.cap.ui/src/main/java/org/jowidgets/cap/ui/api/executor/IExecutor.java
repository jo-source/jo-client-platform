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

package org.jowidgets.cap.ui.api.executor;

import java.util.List;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;


public interface IExecutor<BEAN_TYPE, PARAMETER_TYPE> {

	/**
	 * Do some execution for the given beans and parameter in the ui thread.
	 * 
	 * REMARK: The executor will be invoked in the ui thread, so do not make long lasting
	 * things here to avoid that the ui freezes.
	 * For long lasting executions use the IExecutorJob or IExecutorService instead
	 * 
	 * @param executionContext The execution context of the action
	 * @param beans the beans to get the parameter for
	 * @param defaultParameter The default parameter
	 * 
	 * @return The result of the execution
	 * 
	 * @see IExecutorJob, IExecutorService
	 */
	List<IBeanProxy<BEAN_TYPE>> execute(
		IExecutionContext executionContext,
		List<IBeanProxy<BEAN_TYPE>> beans,
		PARAMETER_TYPE defaultParameter) throws Exception;

}
