/*
 * Copyright (c) 2016, MGrossmann
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

package org.jowidgets.cap.common.tools.execution;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtosInsertionUpdate;
import org.jowidgets.cap.common.api.bean.IBeanDtosUpdate;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.IUpdateCallback;
import org.jowidgets.util.Assert;

public class BeanDtoListUpdateCallbackAdapter implements IUpdateCallback<IBeanDtosUpdate> {

	private final IResultCallback<List<IBeanDto>> resultCallback;

	public BeanDtoListUpdateCallbackAdapter(final IResultCallback<List<IBeanDto>> resultCallback) {
		Assert.paramNotNull(resultCallback, "resultCallback");
		this.resultCallback = resultCallback;
	}

	@Override
	public void finished(final IBeanDtosUpdate result) {
		if (result instanceof IBeanDtosInsertionUpdate) {
			resultCallback.finished(((IBeanDtosInsertionUpdate) result).getInsertedBeans());
		}
		else if (result == null) {
			resultCallback.finished(null);
		}
		else {
			throw new IllegalArgumentException(
				"Only insertion updates are implemented for this callback. Feel free and implement it.");
		}
	}

	@Override
	public void exception(final Throwable exception) {
		resultCallback.exception(exception);
	}

	@Override
	public void update(final IBeanDtosUpdate result) {
		throw new UnsupportedOperationException("Update is not supported for this callback. Feel free and implement it.");
	}

}
