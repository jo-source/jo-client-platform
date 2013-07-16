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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.password.IPasswordChangeExecutor;
import org.jowidgets.api.password.IPasswordChangeResult;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IPasswordChangeDialog;
import org.jowidgets.api.widgets.blueprint.IPasswordChangeDialogBluePrint;
import org.jowidgets.api.widgets.descriptor.setup.IPasswordChangeDialogSetup;
import org.jowidgets.cap.common.api.exception.PasswordChangeServiceException;
import org.jowidgets.cap.common.api.exception.PasswordChangeServiceException.PasswordChangeExceptionDetail;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.IPasswordChangeService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;
import org.jowidgets.util.event.ICancelListener;
import org.jowidgets.util.event.ICancelObservable;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidatorComposite;

final class PasswordChangeDialog {

	private static final IMessage OLD_PASSWORD_INVALID = Messages.getMessage("PasswordChangeDialog.oldPasswordInvalid");
	private static final IMessage NEW_PASSWORD_INVALID = Messages.getMessage("PasswordChangeDialog.newPasswordInvalid");
	private static final IMessage USER_NOT_FOUND = Messages.getMessage("PasswordChangeDialog.userNotFound");
	private static final IMessage MISSING_SECURITY_CONTEXT = Messages.getMessage("PasswordChangeDialog.missingSecurityContext");
	private static final IMessage UNKOWN_ERROR = Messages.getMessage("PasswordChangeDialog.unkownError");

	private final IPasswordChangeExecutor executor;
	private final IValidator<String> passwordValidator;
	private final IPasswordChangeDialogSetup passwordChangeDialogSetup;

	private Rectangle bounds;

	PasswordChangeDialog(
		final IPasswordChangeService passwordChangeService,
		final IPasswordChangeDialogSetup passwordChangeDialogSetup,
		final IValidator<String> passwordValidator) {

		Assert.paramNotNull(passwordChangeService, "passwordChangeService");

		this.executor = new PasswordChangeExecutor(passwordChangeService);
		this.passwordChangeDialogSetup = passwordChangeDialogSetup;

		if (passwordChangeDialogSetup != null) {
			this.passwordValidator = ValidatorComposite.create(
					passwordChangeDialogSetup.getPasswordValidator(),
					passwordValidator);
		}
		else {
			this.passwordValidator = passwordValidator;
		}
	}

	void show(final IExecutionContext executionContext) {
		final IPasswordChangeDialogBluePrint dialogBp = BPF.passwordChangeDialog(executor);
		if (passwordChangeDialogSetup != null) {
			dialogBp.setSetup(passwordChangeDialogSetup);
		}
		dialogBp.setExecutionContext(executionContext).setAutoDispose(true);
		dialogBp.setPasswordValidator(passwordValidator);
		if (bounds != null) {
			dialogBp.setPosition(bounds.getPosition()).setSize(bounds.getSize());
		}

		final IPasswordChangeDialog dialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);
		dialog.addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				bounds = dialog.getBounds();
			}
		});

		dialog.setVisible(true);
	}

	private static final class PasswordChangeExecutor implements IPasswordChangeExecutor {

		private final IPasswordChangeService passwordChangeService;

		private PasswordChangeExecutor(final IPasswordChangeService passwordChangeService) {
			this.passwordChangeService = passwordChangeService;
		}

		@Override
		public void changePassword(
			final IPasswordChangeResult passwordChangeResult,
			final String oldPassword,
			final String newPassword,
			final ICancelObservable cancelObsersable) {

			final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();

			final ICancelListener cancelListener = new ICancelListener() {
				@Override
				public void canceled() {
					executionTask.cancel();
				}
			};

			final IResultCallback<Void> resultCallback = new AbstractUiResultCallback<Void>() {

				@Override
				protected void finishedUi(final Void result) {
					cancelObsersable.removeCancelListener(cancelListener);
					passwordChangeResult.success();
				}

				@Override
				protected void exceptionUi(final Throwable exception) {
					cancelObsersable.removeCancelListener(cancelListener);
					passwordChangeResult.error(createErrorString(exception));
				}

			};

			cancelObsersable.addCancelListener(cancelListener);

			passwordChangeService.changePassword(resultCallback, oldPassword, newPassword, executionTask);
		}

		private String createErrorString(final Throwable exception) {
			if (exception instanceof PasswordChangeServiceException) {
				return createErrorString((PasswordChangeServiceException) exception);
			}
			else {
				return UNKOWN_ERROR.get();
			}
		}

		private String createErrorString(final PasswordChangeServiceException exception) {
			final PasswordChangeExceptionDetail detail = exception.getDetail();
			if (PasswordChangeExceptionDetail.OLD_PASSWORD_INVALID == detail) {
				return OLD_PASSWORD_INVALID.get();
			}
			else if (PasswordChangeExceptionDetail.NEW_PASSWORD_INVALID == detail) {
				return NEW_PASSWORD_INVALID.get();
			}
			else if (PasswordChangeExceptionDetail.USER_NOT_FOUND == detail) {
				return USER_NOT_FOUND.get();
			}
			else if (PasswordChangeExceptionDetail.MISSING_SECURITY_CONTEXT == detail) {
				return MISSING_SECURITY_CONTEXT.get();
			}
			else {
				return UNKOWN_ERROR.get();
			}
		}
	}
}
