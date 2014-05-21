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

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.api.widgets.IQuestionDialog;
import org.jowidgets.api.widgets.blueprint.IQuestionDialogBluePrint;
import org.jowidgets.cap.ui.api.model.DataModelChangeType;
import org.jowidgets.cap.ui.api.model.IChangeResponse;
import org.jowidgets.cap.ui.api.model.IChangeResponse.ResponseType;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.model.IDataModelContext;
import org.jowidgets.cap.ui.api.model.IDataModelSaveDelegate;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ICallback;

final class DataModelContextImpl implements IDataModelContext {

	private static final IMessage UNSAVED_INVALID_DATA_TITLE = Messages.getMessage("DataModelContextImpl.unsaved_invalid_data_title");
	private static final IMessage UNSAVED_INVALID_DATA_TEXT = Messages.getMessage("DataModelContextImpl.unsaved_invalid_data_text");
	private static final IMessage UNSAVED_INVALID_DATA_YES_BTN = Messages.getMessage("DataModelContextImpl.unsaved_invalid_data_yes_btn");
	private static final IMessage UNSAVED_INVALID_DATA_NO_BTN = Messages.getMessage("DataModelContextImpl.unsaved_invalid_data_no_btn");
	private static final IMessage UNSAVED_DATA_TITLE = Messages.getMessage("DataModelContextImpl.unsaved_data_title");
	private static final IMessage UNSAVED_DATA_TEXT = Messages.getMessage("DataModelContextImpl.unsaved_data_text");
	private static final IMessage UNSAVED_DATA_YES_BTN = Messages.getMessage("DataModelContextImpl.unsaved_data_yes_btn");
	private static final IMessage UNSAVED_DATA_NO_BTN = Messages.getMessage("DataModelContextImpl.unsaved_data_no_btn");
	private static final IMessage UNSAVED_DATA_CANCEL_BTN = Messages.getMessage("DataModelContextImpl.unsaved_data_cancel_btn");

	private final Set<IDataModel> selectionChangeModels;
	private final Set<IDataModel> dataChangeModels;
	private final IDataModelSaveDelegate saveDelegate;

	DataModelContextImpl(
		final IDataModel rootModel,
		final DataModelChangeType rootModelDepenency,
		final IDataModelSaveDelegate saveDelegate) {
		Assert.paramNotNull(rootModel, "rootModel");
		Assert.paramNotNull(rootModelDepenency, "rootModelDepenency");

		this.selectionChangeModels = new LinkedHashSet<IDataModel>();
		this.dataChangeModels = new LinkedHashSet<IDataModel>();
		this.saveDelegate = saveDelegate;

		addDependency(rootModel, rootModelDepenency);
	}

	@Override
	public void addDependency(final IDataModel model) {
		addDependency(model, DataModelChangeType.SELECTION_CHANGE);
	}

	@Override
	public void addDependency(final IDataModel model, final DataModelChangeType changeType) {
		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(changeType, "changeType");

		if (DataModelChangeType.DATA_CHANGE.equals(changeType)) {
			dataChangeModels.add(model);
		}
		else if (DataModelChangeType.SELECTION_CHANGE.equals(changeType)) {
			selectionChangeModels.add(model);
		}
	}

	@Override
	public void removeDependency(final IDataModel model) {
		Assert.paramNotNull(model, "model");

		selectionChangeModels.remove(model);
		dataChangeModels.remove(model);
	}

	@Override
	public IChangeResponse permitChange(final DataModelChangeType changeType) {
		if (hasChanged(getRelevantModels(changeType))) {
			return new ChangeResponseImpl(ResponseType.ASYNC, changeType);
		}
		else {
			return new ChangeResponseImpl(ResponseType.YES, changeType);
		}
	}

	@Override
	public void permitChangeAsync(final IChangeResponse changeRespose, final ICallback<Boolean> callback) {
		Assert.paramHasType(changeRespose, ChangeResponseImpl.class, "changeRespose");

		final ChangeResponseImpl changeResponseImpl = (ChangeResponseImpl) changeRespose;
		final DataModelChangeType changeType = changeResponseImpl.getChangeType();
		final Set<IDataModel> relevantModels = getRelevantModels(changeType);

		if (hasInvalidChanged(relevantModels)) {
			if (shouldDataBeRefused()) {
				callback.call(Boolean.TRUE);
				return;
			}
			else {
				callback.call(Boolean.FALSE);
				return;
			}
		}
		else if (hasChanged(relevantModels)) {
			final QuestionResult questionResult = shouldDataBeSaved();
			if (QuestionResult.YES == questionResult) {//do the save
				doSave(changeResponseImpl, callback);
				return;
			}
			if (QuestionResult.CANCEL == questionResult) {//to not permit in case of cancel
				callback.call(Boolean.FALSE);
				return;
			}
			else {//do not save, do refuse, in this case permit the change
				callback.call(Boolean.TRUE);
				return;
			}
		}

	}

	private boolean shouldDataBeRefused() {

		final IQuestionDialogBluePrint dialogBp = BPF.questionDialog();

		dialogBp.setTitle(UNSAVED_INVALID_DATA_TITLE.get());
		dialogBp.setText(UNSAVED_INVALID_DATA_TEXT.get());

		dialogBp.setYesButton(BPF.button(UNSAVED_INVALID_DATA_YES_BTN.get()));
		dialogBp.setNoButton(BPF.button(UNSAVED_INVALID_DATA_NO_BTN.get()));

		final IQuestionDialog dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);

		final QuestionResult questionResult = dialog.question();

		if (QuestionResult.YES == questionResult) {
			return true;
		}
		else {
			return false;
		}
	}

	private QuestionResult shouldDataBeSaved() {
		final IQuestionDialogBluePrint dialogBp = BPF.questionDialog();

		dialogBp.setTitle(UNSAVED_DATA_TITLE.get());
		dialogBp.setText(UNSAVED_DATA_TEXT.get());

		dialogBp.setYesButton(BPF.button(UNSAVED_DATA_YES_BTN.get()));
		dialogBp.setNoButton(BPF.button(UNSAVED_DATA_NO_BTN.get()));
		dialogBp.setCancelButton(BPF.button(UNSAVED_DATA_CANCEL_BTN.get()));

		dialogBp.setDefaultResult(QuestionResult.YES);

		final IQuestionDialog dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);

		return dialog.question();
	}

	private void doSave(final ChangeResponseImpl changeRespose, final ICallback<Boolean> callback) {
		final Set<IDataModel> modifiedModels = getModelsToSave(getRelevantModels(changeRespose.getChangeType()));
		for (final IDataModel model : modifiedModels) {
			model.addModificationStateListener(new ModificationStateListener(modifiedModels, model, callback));
		}
		saveDelegate.save();
	}

	private boolean hasInvalidChanged(final Collection<IDataModel> models) {
		for (final IDataModel model : models) {
			if (model.hasModifications() && !model.validate().isValid()) {
				return true;
			}
		}
		return false;
	}

	private boolean hasChanged(final Collection<IDataModel> models) {
		for (final IDataModel model : models) {
			if (model.hasModifications()) {
				return true;
			}
		}
		return false;
	}

	private Set<IDataModel> getModelsToSave(final Collection<IDataModel> models) {
		final Set<IDataModel> result = new LinkedHashSet<IDataModel>();
		for (final IDataModel model : models) {
			if (model.hasModifications()) {
				result.add(model);
			}
		}
		return result;
	}

	private Set<IDataModel> getRelevantModels(final DataModelChangeType changeType) {
		final Set<IDataModel> result = new LinkedHashSet<IDataModel>();
		if (DataModelChangeType.DATA_CHANGE.equals(changeType)) {
			result.addAll(dataChangeModels);
			result.addAll(selectionChangeModels);
		}
		else if (DataModelChangeType.SELECTION_CHANGE.equals(changeType)) {
			result.addAll(selectionChangeModels);
		}
		return result;
	}

	private final class ModificationStateListener implements IModificationStateListener {

		private static final long MAX_TIME_TO_REFUSE = 1000;

		private final long timestamp;

		private final Set<IDataModel> modifiedModels;
		private final IDataModel model;
		private final ICallback<Boolean> callback;

		private ModificationStateListener(
			final Set<IDataModel> modifiedModels,
			final IDataModel model,
			final ICallback<Boolean> callback) {

			this.timestamp = System.currentTimeMillis();
			this.modifiedModels = modifiedModels;
			this.model = model;
			this.callback = callback;
		}

		@Override
		public void modificationStateChanged() {
			if ((System.currentTimeMillis() - timestamp) > MAX_TIME_TO_REFUSE) {
				model.removeModificationStateListener(this);
			}

			if (!model.hasModifications()) {
				modifiedModels.remove(model);
				if (modifiedModels.isEmpty()) {
					callback.call(Boolean.TRUE);
				}
			}
		}

	}

	private final class ChangeResponseImpl implements IChangeResponse {

		private final ResponseType responseType;
		private final DataModelChangeType changeType;

		private ChangeResponseImpl(final ResponseType responseType, final DataModelChangeType changeType) {
			this.responseType = responseType;
			this.changeType = changeType;
		}

		@Override
		public ResponseType getType() {
			return responseType;
		}

		private DataModelChangeType getChangeType() {
			return changeType;
		}

	}

	@Override
	public void dispose() {
		selectionChangeModels.clear();
		dataChangeModels.clear();
	}

}
