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
import org.jowidgets.tools.widgets.wrapper.ComboBoxWrapper;

final class LookUpComboBoxSelectionImpl extends ComboBoxWrapper<Object> implements ILookUpCallback {

	private static final Object DUMMY_OBJECT = new Object();

	private boolean initialized;

	private Object lastValue;
	private boolean enabled;
	private boolean editable;

	LookUpComboBoxSelectionImpl(final IComboBox<Object> comboBox, final ILookUpComboBoxSelectionBluePrint<Object> setup) {
		super(comboBox);
		setValue(null);

		lastValue = getValue();
		initialized = false;
		setElements(DUMMY_OBJECT);
		super.setEditable(false);

		final ILookUpAccess lookUpAccess = CapUiToolkit.lookUpCache().getAccess(setup.getLookUpId());
		lookUpAccess.addCallback(this);

		addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				lookUpAccess.removeCallback(LookUpComboBoxSelectionImpl.this);
			}
		});
	}

	@Override
	public void beforeChange() {
		//DO NOTHING
	}

	@Override
	public void onChange(final ILookUp lookUp) {
		setElements(lookUp.getValidKeys());
		super.setValue(lastValue);
		super.setEditable(editable);
		super.setEnabled(enabled);
		initialized = true;
	}

	@Override
	public void onException(final Throwable exception) {
		//TODO MG implement on exception
	}

	@Override
	public void setValue(final Object value) {
		this.lastValue = value;
		if (initialized) {
			super.setValue(value);
		}
		else if (value != null) {
			super.setValue(DUMMY_OBJECT);
		}
		else {
			super.setValue(null);
		}
	}

	@Override
	public Object getValue() {
		if (initialized) {
			return super.getValue();
		}
		else {
			return lastValue;
		}
	}

	@Override
	public void setEditable(final boolean editable) {
		this.editable = editable;
		if (initialized || !editable) {
			super.setEditable(editable);
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		if (initialized || !enabled) {
			super.setEnabled(enabled);
		}
	}

}
