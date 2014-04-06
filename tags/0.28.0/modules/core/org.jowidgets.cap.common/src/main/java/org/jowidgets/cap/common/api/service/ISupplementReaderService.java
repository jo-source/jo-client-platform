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

public interface ISupplementReaderService<PARAM_TYPE> {

	/**
	 * Gets a initial (result) reference
	 * 
	 * @param result a initial reference
	 */
	void getInitialReference(final IResultCallback<Object> result);

	/**
	 * Reads a supplement of a result
	 * (what has been changed since the last call of this method or of the getInitialReference() method).
	 * 
	 * 
	 * REMARK: The supplement result may contain some data of the last result call but must not omit data.
	 * In other words: To avoid gaps in the supplement data make your implementation robust by using a small
	 * overlap for continuous results
	 * 
	 * @param result
	 * @param parentBeanKeys
	 * @param filter
	 * @param sorting
	 * @param parameter
	 * @param lastResultReference The reference of the last result or the initial reference
	 * @param executionCallback
	 */
	void readSupplement(
		IResultCallback<ISupplementReaderResult> result,
		List<? extends IBeanKey> parentBeanKeys,
		IFilter filter,
		List<? extends ISort> sorting,
		PARAM_TYPE parameter,
		Object lastResultReference,
		IExecutionCallback executionCallback);

	public interface ISupplementReaderResult {

		List<IBeanDto> getBeans();

		/**
		 * @return A free chooseable reference (e.g. a timestamp)
		 */
		Object getResultReference();

	}

}
