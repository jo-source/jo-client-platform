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

package org.jowidgets.cap.common.api.service;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;

public interface IReaderService<PARAM_TYPE> {

	/**
	 * Reads the beans asynchronous.
	 * 
	 * This method returns directly and provides the result with help of the given
	 * result callback. In case of success, a list of IBeanDto objects will be
	 * returned to the callback with the {@link IResultCallback#finished(Object)} method,
	 * in case of an exception, the {@link IResultCallback#exception(Throwable)} will be
	 * invoked with the given exception.
	 * 
	 * @param result The result callback that will be invoked on success or error
	 * @param parentBeanKeys The parent keys for master - detail readers
	 * @param filter The filter, may be null
	 * @param sorting The sorting, may be empty but never null
	 * @param firstRow The first row index for paging
	 * @param maxRows The maximal number of row to load
	 * @param parameter A generic parameter object for further use
	 * @param executionCallback Callback to send progress or get informed when user canceled
	 */
	void read(
		IResultCallback<List<IBeanDto>> result,
		List<? extends IBeanKey> parentBeanKeys,
		IFilter filter,
		List<? extends ISort> sorting,
		int firstRow,
		int maxRows,
		PARAM_TYPE parameter,
		IExecutionCallback executionCallback);

	/**
	 * Counts the total number of currently available beans by this service asynchronous.
	 * 
	 * This method returns directly and provides the result with help of the given
	 * result callback. In case of success, a Integer will be returned to the callback
	 * with the {@link IResultCallback#finished(Object)} method. The value represents the
	 * total number of currently available beans by this service, or is null, if count is
	 * not supported.
	 * In case of an exception, the {@link IResultCallback#exception(Throwable)} will be
	 * invoked with the given exception.
	 * 
	 * @param result The result callback that will be invoked on success or error
	 * @param parentBeanKeys The parent keys for master - detail readers
	 * @param filter The filter, may be null
	 * @param parameter A generic parameter object for further use
	 * @param executionCallback Callback to send progress or get informed when user canceled
	 */
	void count(
		IResultCallback<Integer> result,
		List<? extends IBeanKey> parentBeanKeys,
		IFilter filter,
		PARAM_TYPE parameter,
		IExecutionCallback executionCallback);

}
