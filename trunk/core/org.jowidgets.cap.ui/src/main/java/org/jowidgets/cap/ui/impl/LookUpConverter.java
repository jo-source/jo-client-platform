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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.lookup.ILookUp;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.cap.ui.api.lookup.ILookUpListener;
import org.jowidgets.common.mask.ITextMask;
import org.jowidgets.common.verify.IInputVerifier;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class LookUpConverter<KEY_TYPE> implements IConverter<KEY_TYPE> {

	private final Object lookUpId;
	private final String lookUpProperty;
	private final IConverter<Object> valueConverter;
	private final ILookUpListener lookUpListener;
	private final IValidator<String> stringValidator;
	private final IUiThreadAccess uiThreadAccess;
	private final ILookUpAccess lookUpAccess;

	private boolean onLoad;

	LookUpConverter(final Object lookUpId, final ILookUpProperty lookUpProperty) {
		this(lookUpId, lookUpProperty.getName(), Toolkit.getConverterProvider().getConverter(lookUpProperty.getValueType()));
	}

	@SuppressWarnings("unchecked")
	LookUpConverter(final Object lookUpId, final String lookUpPropertyName, final IConverter<?> valueConverter) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		Assert.paramNotNull(lookUpPropertyName, "lookUpPropertyName");
		Assert.paramNotNull(valueConverter, "valueConverter");

		this.lookUpId = lookUpId;
		this.lookUpProperty = lookUpPropertyName;
		this.valueConverter = (IConverter<Object>) valueConverter;
		this.onLoad = false;

		this.uiThreadAccess = Toolkit.getUiThreadAccess();

		this.lookUpAccess = CapUiToolkit.lookUpCache().getAccess(lookUpId);

		if (lookUpAccess != null) {
			this.lookUpListener = new ILookUpListener() {

				@Override
				public void taskCreated(final IExecutionTask task) {
					onLoad = true;
				}

				@Override
				public void afterLookUpChanged() {
					onLoad = false;
				}
			};
			lookUpAccess.addLookUpListener(lookUpListener, true);
		}
		else {
			throw new IllegalStateException("No look up access found for the id '" + lookUpId + "'");
		}

		this.stringValidator = new IValidator<String>() {

			@Override
			public IValidationResult validate(final String value) {
				final IValidator<String> original = valueConverter.getStringValidator();
				if (original != null) {
					final IValidationResult result = valueConverter.getStringValidator().validate(value);
					if (!result.isValid()) {
						return result;
					}
				}
				if (!EmptyCheck.isEmpty(value)) {
					final KEY_TYPE converted = convertToObject(value);
					if (converted == null) {
						final String msg = Messages.getString("LookUpConverter.the_input_is_not_in_the_defined_range");
						return ValidationResult.error(msg);
					}
				}
				return ValidationResult.ok();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public KEY_TYPE convertToObject(final String string) {
		final ILookUp lookUp = getLookUp();
		if (lookUp != null) {
			final Object value = valueConverter.convertToObject(string);
			if (value != null) {
				return (KEY_TYPE) lookUp.getKey(value, lookUpProperty);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	@Override
	public String convertToString(final KEY_TYPE key) {
		if (key == null) {
			return "";
		}
		final ILookUp lookUp = getLookUp();
		if (lookUp != null) {
			final ILookUpEntry lookUpEntry = lookUp.getEntry(key);
			if (lookUpEntry != null) {
				final Object value = lookUpEntry.getValue(lookUpProperty);
				return valueConverter.convertToString(value);
			}
			else if (!onLoad) {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						CapUiToolkit.lookUpCache().clearCache(lookUpId);
					}
				});

				return Messages.getString("LookUpConverter.unknown_look_up_key");
			}
			else {
				return Messages.getString("LookUpConverter.not_initialized_value");
			}
		}
		else {
			return Messages.getString("LookUpConverter.not_initialized_value");
		}
	}

	@Override
	public String getDescription(final KEY_TYPE key) {
		if (key == null) {
			return null;
		}
		final ILookUp lookUp = getLookUp();
		if (lookUp != null) {
			return lookUp.getDescription(key);
		}
		else {
			return Messages.getString("LookUpConverter.not_initialized_description");
		}
	}

	@Override
	public IValidator<String> getStringValidator() {
		return stringValidator;
	}

	@Override
	public IInputVerifier getInputVerifier() {
		return valueConverter.getInputVerifier();
	}

	@Override
	public String getAcceptingRegExp() {
		return valueConverter.getAcceptingRegExp();
	}

	@Override
	public ITextMask getMask() {
		return valueConverter.getMask();
	}

	private ILookUp getLookUp() {
		if (lookUpAccess.isInitialized()) {
			return lookUpAccess.getCurrentLookUp();
		}
		else {
			//trigger initialization to enhance probability that the 
			//next access may work
			lookUpAccess.initialize();
			return null;
		}
	}

}
