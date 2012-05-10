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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.tools.types.VetoHolder;

final class ExecutionObservable<RESULT_TYPE> {

	private final Set<IExecutionInterceptor<RESULT_TYPE>> interceptors;

	ExecutionObservable(final Collection<IExecutionInterceptor<RESULT_TYPE>> interceptors) {
		if (interceptors != null) {
			this.interceptors = new LinkedHashSet<IExecutionInterceptor<RESULT_TYPE>>(interceptors);
		}
		else {
			this.interceptors = Collections.emptySet();
		}
	}

	/**
	 * Fires the before execution method on the interceptors
	 * 
	 * @param executionContext
	 * @return True if execution should be continued
	 */
	boolean fireBeforeExecution(final IExecutionContext executionContext) {
		final VetoHolder vetoHolder = new VetoHolder();
		for (final IExecutionInterceptor<RESULT_TYPE> interceptor : interceptors) {
			interceptor.beforeExecution(executionContext, vetoHolder);
			if (vetoHolder.hasVeto()) {
				break;
			}
		}
		if (vetoHolder.hasVeto()) {
			for (final IExecutionInterceptor<RESULT_TYPE> interceptor : interceptors) {
				interceptor.onExecutionVeto(executionContext);
			}
		}
		return !vetoHolder.hasVeto();
	}

	void fireAfterExecutionPrepared(final IExecutionContext executionContext) {
		for (final IExecutionInterceptor<RESULT_TYPE> interceptor : interceptors) {
			interceptor.afterExecutionPrepared(executionContext);
		}
	}

	void fireAfterExecutionSuccess(final IExecutionContext executionContext, final RESULT_TYPE result) {
		for (final IExecutionInterceptor<RESULT_TYPE> interceptor : interceptors) {
			interceptor.afterExecutionSuccess(executionContext, result);
		}
	}

	void fireAfterExecutionError(final IExecutionContext executionContext, final Throwable error) {
		for (final IExecutionInterceptor<RESULT_TYPE> interceptor : interceptors) {
			interceptor.afterExecutionError(executionContext, error);
		}
	}

	void fireAfterExecutionCanceled(final IExecutionContext executionContext) {
		for (final IExecutionInterceptor<RESULT_TYPE> interceptor : interceptors) {
			interceptor.afterExecutionUserCanceled(executionContext);
		}
	}
}
