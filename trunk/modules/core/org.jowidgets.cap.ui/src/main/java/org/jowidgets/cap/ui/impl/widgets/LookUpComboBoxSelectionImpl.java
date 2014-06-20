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

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.lookup.ILookUp;
import org.jowidgets.cap.ui.api.lookup.ILookUpAccess;
import org.jowidgets.cap.ui.api.lookup.ILookUpCallback;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.tools.controller.InputObservable;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.tools.widgets.wrapper.ComboBoxWrapper;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.tools.CompoundValidator;

final class LookUpComboBoxSelectionImpl extends ComboBoxWrapper<Object> implements ILookUpCallback {

	private static final Object DUMMY_OBJECT = new Object();
	private final LookUpControlInitializationDelegate initializationDelegate;
	private final ValidationCache validationCache;
	private final CompoundValidator<Object> compoundValidator;
	private final InputObservable inputObservable;

	private Object lastValue;

	LookUpComboBoxSelectionImpl(final IComboBox<Object> comboBox, final ILookUpComboBoxSelectionBluePrint<Object> setup) {
		super(comboBox);

		this.initializationDelegate = new LookUpControlInitializationDelegate(comboBox);
		this.inputObservable = new InputObservable();
		this.compoundValidator = new CompoundValidator<Object>();

		final IValidator<Object> validator = setup.getValidator();
		if (validator != null) {
			compoundValidator.addValidator(validator);
		}

		this.validationCache = new ValidationCache(new IValidationResultCreator() {
			@Override
			public IValidationResult createValidationResult() {
				return compoundValidator.validate(getValue());
			}
		});

		this.lastValue = comboBox.getValue();
		super.setValue(null);

		final ILookUpAccess lookUpAccess = CapUiToolkit.lookUpCache().getAccess(setup.getLookUpId());
		if (!lookUpAccess.isInitialized()) {
			setElements(DUMMY_OBJECT);
		}
		lookUpAccess.addCallback(this, false);

		addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				lookUpAccess.removeCallback(LookUpComboBoxSelectionImpl.this);
			}
		});

		comboBox.addInputListener(new IInputListener() {
			@Override
			public void inputChanged() {
				inputObservable.fireInputChanged();
			}
		});

		comboBox.addValidationConditionListener(new IValidationConditionListener() {
			@Override
			public void validationConditionsChanged() {
				validationCache.setDirty();
			}
		});
	}

	@Override
	public void beforeChange() {
		//DO NOTHING
	}

	@Override
	public void onChange(final ILookUp lookUp) {

		final boolean firstInitialize = !initializationDelegate.isInitialized();

		if (firstInitialize) {
			initializationDelegate.initialize();
			setElements(lookUp.getValidKeys());
			super.setValue(lastValue);
			resetModificationState();
		}
		else {
			setElements(lookUp.getValidKeys());
		}

		validationCache.setDirty();
	}

	@Override
	public void onException(final Throwable exception) {
		//TODO MG implement on exception
	}

	@Override
	public void setValue(final Object value) {
		this.lastValue = value;
		if (initializationDelegate.isInitialized()) {
			super.setValue(value);
		}
		else {
			super.setValue(DUMMY_OBJECT);
		}
	}

	@Override
	public Object getValue() {
		if (initializationDelegate.isInitialized()) {
			return super.getValue();
		}
		else {
			return lastValue;
		}
	}

	@Override
	public void setEditable(final boolean editable) {
		initializationDelegate.setEditable(editable);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		initializationDelegate.setEnabled(enabled);
	}

	@Override
	public void addValidator(final IValidator<Object> validator) {
		compoundValidator.addValidator(validator);
	}

	@Override
	public IValidationResult validate() {
		return validationCache.validate();
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		validationCache.addValidationConditionListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		validationCache.removeValidationConditionListener(listener);
	}

	@Override
	public void addInputListener(final IInputListener listener) {
		inputObservable.addInputListener(listener);
	}

	@Override
	public void removeInputListener(final IInputListener listener) {
		inputObservable.removeInputListener(listener);
	}

}
