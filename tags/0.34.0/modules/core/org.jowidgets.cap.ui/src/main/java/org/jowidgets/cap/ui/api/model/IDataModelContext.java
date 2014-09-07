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

package org.jowidgets.cap.ui.api.model;

import org.jowidgets.util.ICallback;

public interface IDataModelContext {

	/**
	 * Adds a dependency to the context with the default DataModelChangeType.SELECTION
	 * 
	 * @param model the model to add
	 */
	void addDependency(IDataModel model);

	/**
	 * Adds a dependency to the context
	 * 
	 * @param model The model to add
	 * @param changeType The change type
	 */
	void addDependency(IDataModel model, DataModelChangeType changeType);

	/**
	 * Removes a dependency from the context
	 * 
	 * @param model The model to remove
	 */
	void removeDependency(IDataModel model);

	/**
	 * Ask for permission of a change.
	 * In some cases this can not be done with synchronous signature, so the result my be ResponseType.ASYNC.
	 * 
	 * In this case, the permitChangeAsync method can be used, to get the permission with async signature
	 * 
	 * @param changeType The change type
	 * @return The ChangeResponse
	 */
	IChangeResponse permitChange(DataModelChangeType changeType);

	/**
	 * Ask for permission with async signature. The answer will be provided on the given callback.
	 * 
	 * Invokers may not assume than the callback will ever be invoked.
	 * 
	 * @param changeRespose The response of the sync request
	 * @param callback The callback to get the result
	 */
	void permitChangeAsync(IChangeResponse changeRespose, ICallback<Boolean> callback);

	/**
	 * Dispose the context.
	 */
	void dispose();

}
