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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.widgets.descriptor.setup.IPasswordChangeDialogSetup;
import org.jowidgets.cap.common.api.service.IPasswordChangeService;
import org.jowidgets.cap.ui.api.command.IPasswordChangeActionBuilder;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.IValidatorCompositeBuilder;
import org.jowidgets.validation.ValidatorComposite;

final class PasswordChangeActionBuilderImpl extends AbstractCapActionBuilderImpl<IPasswordChangeActionBuilder> implements
		IPasswordChangeActionBuilder {

	private final IValidatorCompositeBuilder<String> passwordValidatorBuilder;

	private IPasswordChangeService passwordChangeService;
	private IPasswordChangeDialogSetup passwordChangeDialogSetup;

	PasswordChangeActionBuilderImpl() {
		setText(Messages.getString("PasswordChangeActionBuilderImpl.text"));
		setToolTipText(Messages.getString("PasswordChangeActionBuilderImpl.tooltip"));

		this.passwordValidatorBuilder = ValidatorComposite.builder();
	}

	@Override
	protected IAction doBuild() {
		final PasswordChangeCommand command = new PasswordChangeCommand(
			getPasswordChangeService(),
			passwordChangeDialogSetup,
			passwordValidatorBuilder.build());

		final IActionBuilder builder = getBuilder();
		builder.setCommand(command);
		return builder.build();
	}

	private IPasswordChangeService getPasswordChangeService() {
		if (passwordChangeService == null) {
			final IPasswordChangeService result = ServiceProvider.getService(IPasswordChangeService.ID);
			if (result == null) {
				throw new IllegalStateException("No 'PasswordChangeService' found");
			}
			return result;
		}
		else {
			return passwordChangeService;
		}
	}

	@Override
	public IPasswordChangeActionBuilder setPasswordChangeService(final IPasswordChangeService service) {
		Assert.paramNotNull(service, "service");
		this.passwordChangeService = service;
		return this;
	}

	@Override
	public IPasswordChangeActionBuilder setPasswordChangeService(final IServiceId<IPasswordChangeService> serviceId) {
		Assert.paramNotNull(serviceId, "serviceId");
		this.passwordChangeService = ServiceProvider.getService(serviceId);
		if (passwordChangeService == null) {
			throw new IllegalArgumentException("No service found for the id '" + serviceId + "'.");
		}
		return this;
	}

	@Override
	public IPasswordChangeActionBuilder setPasswordChangeDialog(final IPasswordChangeDialogSetup passwordChangeDialogSetup) {
		Assert.paramNotNull(passwordChangeDialogSetup, "passwordChangeDialogSetup");
		this.passwordChangeDialogSetup = passwordChangeDialogSetup;
		return this;
	}

	@Override
	public IPasswordChangeActionBuilder addPasswordValidator(final IValidator<String> passwordValidator) {
		Assert.paramNotNull(passwordValidator, "passwordValidator");
		passwordValidatorBuilder.add(passwordValidator);
		return this;
	}

}
