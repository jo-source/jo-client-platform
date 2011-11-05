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

import java.util.List;

import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTable;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.tools.layout.MigLayoutFactory;

final class BeanSelectionDialogContentCreator<BEAN_TYPE> implements IInputContentCreator<List<IBeanProxy<BEAN_TYPE>>> {

	private final IBeanSelectionTableBluePrint<BEAN_TYPE> beanSelectionTable;

	private IBeanSelectionTable<BEAN_TYPE> table;

	BeanSelectionDialogContentCreator(final IBeanSelectionTableBluePrint<BEAN_TYPE> beanSelectionTable) {
		this.beanSelectionTable = beanSelectionTable;
	}

	@Override
	public void createContent(final IInputContentContainer contentContainer) {
		contentContainer.setLayout(MigLayoutFactory.growingInnerCellLayout());
		table = contentContainer.add(beanSelectionTable, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
	}

	@Override
	public void setValue(final List<IBeanProxy<BEAN_TYPE>> value) {
		if (table != null) {
			table.setValue(value);
		}
	}

	@Override
	public List<IBeanProxy<BEAN_TYPE>> getValue() {
		if (table != null) {
			return table.getValue();
		}
		return null;
	}

}
