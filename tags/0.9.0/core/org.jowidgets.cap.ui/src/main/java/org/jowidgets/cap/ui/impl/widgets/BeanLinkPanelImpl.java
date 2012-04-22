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

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel.IBeanLink;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanelBluePrint;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

final class BeanLinkPanelImpl<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		AbstractInputControl<IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> implements
		IBeanLinkPanel<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> {

	public BeanLinkPanelImpl(
		final IComposite composite,
		final IBeanLinkPanelBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> bluePrint) {
		super(composite);
		composite.add(BPF.textLabel("TODO"));
	}

	@Override
	public boolean hasModifications() {
		return false;
	}

	@Override
	public void resetModificationState() {

	}

	@Override
	public void setValue(final IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> value) {

	}

	@Override
	public IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> getValue() {

		return null;
	}

	@Override
	public void setEditable(final boolean editable) {

	}

	@Override
	protected IValidationResult createValidationResult() {
		return ValidationResult.ok();
	}

}
