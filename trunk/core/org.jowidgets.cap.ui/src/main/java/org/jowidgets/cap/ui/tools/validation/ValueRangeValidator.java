/*
 * Copyright (c) 2010, Michael Grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.jowidgets.cap.ui.tools.validation;

import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.lookup.ILookUp;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

public class ValueRangeValidator<VALIDATION_INPUT_TYPE> implements IValidator<VALIDATION_INPUT_TYPE> {

	private final IValidationResult notInRangeResult;
	private final IValidationResult lookUpNotInitialized;
	private final IValueRange valueRange;

	public ValueRangeValidator(final IValueRange valueRange) {
		this(valueRange, Messages.getString("ValueRangeValidator.the_input_is_not_in_the_defined_range")); //$NON-NLS-1$
	}

	public ValueRangeValidator(final IValueRange valueRange, final String messageText) {
		Assert.paramNotNull(valueRange, "valueRange"); //$NON-NLS-1$
		Assert.paramNotEmpty(messageText, "messageText"); //$NON-NLS-1$
		this.valueRange = valueRange;
		this.notInRangeResult = ValidationResult.error(messageText);
		this.lookUpNotInitialized = ValidationResult.error(Messages.getString("ValueRangeValidator.look_up_not_initialized"));
	}

	@Override
	public IValidationResult validate(final VALIDATION_INPUT_TYPE validationInput) {
		if (valueRange instanceof IStaticValueRange) {
			return validateStaticRange((IStaticValueRange) valueRange, validationInput);
		}
		else if (valueRange instanceof ILookUpValueRange) {
			return validateLookUpRange((ILookUpValueRange) valueRange, validationInput);
		}
		else {
			return ValidationResult.ok();
		}
	}

	private IValidationResult validateStaticRange(
		final IStaticValueRange staticValueRange,
		final VALIDATION_INPUT_TYPE validationInput) {
		if (!staticValueRange.isOpen() && !staticValueRange.getValues().contains(validationInput)) {
			return notInRangeResult;
		}
		else {
			return ValidationResult.ok();
		}
	}

	private IValidationResult validateLookUpRange(
		final ILookUpValueRange lookUpValueValueRange,
		final VALIDATION_INPUT_TYPE validationInput) {

		final ILookUpAccess lookUpAccess = CapUiToolkit.lookUpCache().getAccess(lookUpValueValueRange.getLookUpId());
		//assuming that look up is already initialized here, otherwise an input
		//to the control could not be possible
		final ILookUp lookUp = lookUpAccess.getCurrentLookUp();
		if (lookUp != null) {
			if (validationInput != null && !lookUp.getKeys().contains(validationInput)) {
				return notInRangeResult;
			}
			else {
				return ValidationResult.ok();
			}
		}
		else {
			return lookUpNotInitialized;
		}

	}
}
