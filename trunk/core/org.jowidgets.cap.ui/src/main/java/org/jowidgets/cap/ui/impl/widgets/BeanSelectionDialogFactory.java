/*
 * Copyright (c) 2010, grossmann
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

import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IInputDialog;
import org.jowidgets.api.widgets.blueprint.IInputDialogBluePrint;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionDialogBluePrint;
import org.jowidgets.common.widgets.factory.IWidgetFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;

public final class BeanSelectionDialogFactory implements
		IWidgetFactory<IInputDialog<List<IBeanProxy<Object>>>, IBeanSelectionDialogBluePrint<Object>> {

	@Override
	public IInputDialog<List<IBeanProxy<Object>>> create(
		final Object parentUiReference,
		final IBeanSelectionDialogBluePrint<Object> bluePrint) {
		final IInputDialogBluePrint<List<IBeanProxy<Object>>> inputDialogBp;
		final BeanSelectionDialogContentCreator<Object> contentCreator = new BeanSelectionDialogContentCreator<Object>(
			bluePrint.getBeanSelectionTable());
		inputDialogBp = BPF.inputDialog(contentCreator);
		inputDialogBp.setSetup(bluePrint);
		inputDialogBp.setContentCreator(contentCreator);
		return Toolkit.getWidgetFactory().create(inputDialogBp);
	}

}
