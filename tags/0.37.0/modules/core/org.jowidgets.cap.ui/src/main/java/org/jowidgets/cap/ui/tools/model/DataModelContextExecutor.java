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

package org.jowidgets.cap.ui.tools.model;

import org.jowidgets.cap.ui.api.model.DataModelChangeType;
import org.jowidgets.cap.ui.api.model.IChangeResponse;
import org.jowidgets.cap.ui.api.model.IChangeResponse.ResponseType;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IDataModelContextProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ICallback;

public final class DataModelContextExecutor {

	private DataModelContextExecutor() {}

	public static void executeDataChange(final IDataModelContextProvider provider, final Runnable runnable) {
		Assert.paramNotNull(provider, "provider");
		executeDataChange(provider.getDataModelContext(), runnable);
	}

	public static void executeDataChange(final IDataModelContext context, final Runnable runnable) {
		Assert.paramNotNull(context, "context");
		execute(context, DataModelChangeType.DATA_CHANGE, runnable);
	}

	public static void executeSelectionChange(final IDataModelContextProvider provider, final Runnable runnable) {
		Assert.paramNotNull(provider, "provider");
		executeDataChange(provider.getDataModelContext(), runnable);
	}

	public static void executeSelectionChange(final IDataModelContext context, final Runnable runnable) {
		Assert.paramNotNull(context, "context");
		execute(context, DataModelChangeType.SELECTION_CHANGE, runnable);
	}

	public static void execute(final IDataModelContext context, final DataModelChangeType changeType, final Runnable runnable) {
		Assert.paramNotNull(context, "context");
		final IChangeResponse response = context.permitChange(changeType);
		if (ResponseType.YES.equals(response.getType())) {
			runnable.run();
		}
		else if (ResponseType.ASYNC.equals(response.getType())) {
			context.permitChangeAsync(response, new ICallback<Boolean>() {
				@Override
				public void call(final Boolean parameter) {
					if (Boolean.TRUE.equals(parameter)) {
						runnable.run();
					}
				}
			});
		}
	}

}
