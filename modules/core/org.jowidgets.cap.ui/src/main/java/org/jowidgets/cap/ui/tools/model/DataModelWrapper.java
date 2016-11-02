/*
 * Copyright (c) 2012, grossmann
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

import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.model.IDataSaveListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

public class DataModelWrapper implements IDataModel {

	private final IDataModel original;

	public DataModelWrapper(final IDataModel original) {
		Assert.paramNotNull(original, "original");
		this.original = original;
	}

	protected IDataModel getOriginal() {
		return original;
	}

	@Override
	public IValidationResult validate() {
		return original.validate();
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		original.addProcessStateListener(listener);
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		original.addModificationStateListener(listener);
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		original.addValidationConditionListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		original.removeProcessStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		original.removeModificationStateListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		original.removeValidationConditionListener(listener);
	}

	@Override
	public void load() {
		original.load();
	}

	@Override
	public void clear() {
		original.clear();
	}

	@Override
	public void clearCache() {
		original.clearCache();
	}

	@Override
	public boolean hasModificationsCached() {
		return original.hasModificationsCached();
	}

	@Override
	public void save() {
		original.save();
	}

	@Override
	public void addDataSaveListener(final IDataSaveListener dataSaveListener) {
		original.addDataSaveListener(dataSaveListener);
	}

	@Override
	public void removeDataSaveListener(final IDataSaveListener dataSaveListener) {
		original.removeDataSaveListener(dataSaveListener);
	}

	@Override
	public void undo() {
		original.undo();
	}

	@Override
	public boolean hasModifications() {
		return original.hasModifications();
	}

	@Override
	public boolean hasExecutions() {
		return original.hasExecutions();
	}

	@Override
	public void cancelExecutions() {
		original.cancelExecutions();
	}

}
