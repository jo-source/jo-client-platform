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

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterType;
import org.jowidgets.cap.ui.api.filter.IUiConfigurableFilter;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControl;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;

//TODO MG implement AttributeFilterControlImpl
final class AttributeFilterControlImpl extends ControlWrapper implements IAttributeFilterControl {

	AttributeFilterControlImpl(final IComposite composite, final IAttributeFilterControlBluePrint bluePrint) {
		super(composite);
		composite.setLayout(MigLayoutFactory.growingInnerCellLayout());
		composite.add(Toolkit.getBluePrintFactory().textLabel("TODO, must be implemented\ndfasdasd\nsfsdfdsf"));
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void setAttribute(final IAttribute<?> attribute, final IFilterType filterType) {

	}

	@Override
	public void setValue(final IUiConfigurableFilter<? extends Object> value) {

	}

	@Override
	public IUiConfigurableFilter<? extends Object> getValue() {
		return null;
	}

	@Override
	public boolean hasModifications() {
		return false;
	}

	@Override
	public void resetModificationState() {

	}

	@Override
	public IValidationResult validate() {
		return null;
	}

	@Override
	public void addValidator(final IValidator<IUiConfigurableFilter<? extends Object>> validator) {

	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {

	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {

	}

	@Override
	public void setEditable(final boolean editable) {

	}

	@Override
	public void addInputListener(final IInputListener listener) {

	}

	@Override
	public void removeInputListener(final IInputListener listener) {

	}

}
