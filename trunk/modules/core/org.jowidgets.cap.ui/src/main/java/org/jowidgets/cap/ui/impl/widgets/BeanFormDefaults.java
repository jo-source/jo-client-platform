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

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.widgets.blueprint.IInputComponentValidationLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.defaults.IDefaultInitializer;
import org.jowidgets.cap.ui.api.color.CapColors;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.IDecorator;
import org.jowidgets.validation.IValidationMessage;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.MessageType;
import org.jowidgets.validation.tools.MandatoryValidator;

final class BeanFormDefaults implements IDefaultInitializer<IBeanFormBluePrint<?>> {

	@Override
	public void initialize(final IBeanFormBluePrint<?> bluePrint) {
		bluePrint.setAutoResetValidation(true);
		bluePrint.setCreateModeForegroundColor(Colors.GREEN);
		bluePrint.setModifiedForegroundColor(Colors.STRONG);
		bluePrint.setScrollbarsAllowed(true);
		final IInputComponentValidationLabelBluePrint editModeValidationLabel = BPF.inputComponentValidationLabel();
		final IDecorator<IValidationResult> editModeValidationDecorator = new IDecorator<IValidationResult>() {
			@Override
			public IValidationResult decorate(final IValidationResult original) {
				if (original != null) {
					final IValidationMessage worstFirst = original.getWorstFirst();
					if (worstFirst != null && worstFirst.getType().equalOrWorse(MessageType.WARNING)) {
						return original;
					}
				}
				return null;
			}
		};
		editModeValidationLabel.setInitialValidationDecorator(editModeValidationDecorator);
		editModeValidationLabel.setUnmodifiedValidationDecorator(editModeValidationDecorator);
		editModeValidationLabel.setEmptyIcon(IconsSmall.OK_GREYED);
		bluePrint.setEditModeValidationLabel(editModeValidationLabel);

		final IInputComponentValidationLabelBluePrint createModeValidationLabel = BPF.inputComponentValidationLabel();
		final IDecorator<IValidationResult> createModeValidationDecorator = new IDecorator<IValidationResult>() {
			@Override
			public IValidationResult decorate(final IValidationResult original) {
				if (original != null) {
					final IValidationMessage worstFirst = original.getWorstFirst();
					if (worstFirst != null && worstFirst.getType().equalOrWorse(MessageType.WARNING)) {
						return original;
					}
				}
				return null;
			}
		};
		createModeValidationLabel.setInitialValidationDecorator(createModeValidationDecorator);
		createModeValidationLabel.setUnmodifiedValidationDecorator(createModeValidationDecorator);
		bluePrint.setCreateModeValidationLabel(createModeValidationLabel);
		bluePrint.setCreateModeInputHint(Messages.getString("BeanFormDefaults.fill_out_mandatory_fields"));

		bluePrint.setMandatoryBackgroundColor(CapColors.MANDATORY_BACKGROUND);
		bluePrint.setMandatoryLabelDecorator(new IDecorator<String>() {
			@Override
			public String decorate(final String original) {
				if (original != null) {
					return original + "*";
				}
				return null;
			}
		});
		bluePrint.setMandatoryValidator(new MandatoryValidator<Object>());
		bluePrint.setPropertyValidationLabel(BPF.validationResultLabel().setShowValidationMessage(false));
	}
}
