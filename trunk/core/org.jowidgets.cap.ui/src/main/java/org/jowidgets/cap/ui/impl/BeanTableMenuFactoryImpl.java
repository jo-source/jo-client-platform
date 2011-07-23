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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanTableMenuFactoryImpl implements IBeanTableMenuFactory {

	BeanTableMenuFactoryImpl() {}

	@Override
	public IActionBuilder beanTableSettingsActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableSettingsActionBuilder(table);
	}

	@Override
	public IAction beanTableSettingsAction(final IBeanTable<?> table) {
		return beanTableSettingsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTableHideColumnActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableHideColumnActionBuilder(table);
	}

	@Override
	public IAction beanTableHideColumnAction(final IBeanTable<?> table) {
		return beanTableHideColumnActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTableUnhideColumnsActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTableUnhideColumnsActionBuilder(table);
	}

	@Override
	public IAction beanTableUnhideColumnsAction(final IBeanTable<?> table) {
		return beanTableUnhideColumnsActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTablePackAllActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTablePackAllActionBuilder(table);
	}

	@Override
	public IAction beanTablePackAllAction(final IBeanTable<?> table) {
		return beanTablePackAllActionBuilder(table).build();
	}

	@Override
	public IActionBuilder beanTablePackSelectedActionBuilder(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		return new BeanTablePackSelectedActionBuilder(table);
	}

	@Override
	public IAction beanTablePackSelectedAction(final IBeanTable<?> table) {
		return beanTablePackSelectedActionBuilder(table).build();
	}

	@Override
	public IMenuModel beanTableHeaderFormatMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (EmptyCheck.isEmpty(model.getAttribute(columnIndex).getLabelLong())) {
			return null;
		}
		else {
			return new BeanTableHeaderFormatMenuModel(model, columnIndex);
		}
	}

	@Override
	public IMenuModel beanTableContentFormatMenu(final IBeanTableModel<?> model, final int columnIndex) {
		Assert.paramNotNull(model, "model");
		if (model.getAttribute(columnIndex).getControlPanels().size() > 1) {
			return new BeanTableContentFormatMenuModel(model, columnIndex);
		}
		else {
			return null;
		}
	}

}
