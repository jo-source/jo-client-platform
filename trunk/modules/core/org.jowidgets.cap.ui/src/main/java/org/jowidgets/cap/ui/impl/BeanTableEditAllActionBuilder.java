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

import java.util.ArrayList;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandAction;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IPopupMenuListener;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.tools.command.ActionBuilder;

final class BeanTableEditAllActionBuilder extends ActionBuilder {

	private static final IMessage EDIT_SELECTION = Messages.getMessage("BeanTableEditAllActionBuilder.editSelection");
	private static final IMessage EDIT_ALL = Messages.getMessage("BeanTableEditAllActionBuilder.editAll");

	private final IBeanTable<?> table;

	@SuppressWarnings({"rawtypes", "unchecked"})
	BeanTableEditAllActionBuilder(final IBeanTable<?> table, final int columnIndex) {
		super();
		this.table = table;
		setIcon(IconsSmall.EDIT);
		setCommand((ICommand) new BeanTableEditAllCommand(table.getModel(), columnIndex));
	}

	@Override
	public ICommandAction build() {
		final ICommandAction result = super.build();
		table.addCellMenuListener(new IPopupMenuListener<ITableCellPopupEvent>() {
			@Override
			public void beforeMenuShow(final ITableCellPopupEvent event) {
				final int columnIndex = event.getColumnIndex();

				final IBeanTableModel<?> model = table.getModel();
				final String entityLabelPlural = model.getEntityLabelPlural();

				final IAttribute<Object> attribute = model.getAttribute(columnIndex);
				final String columnLabel = attribute.getLabel().get();

				final ArrayList<Integer> selection = model.getSelection();
				if (selection.size() > 1) {//set to selection
					result.setText(MessageReplacer.replace(EDIT_SELECTION.get(), columnLabel, entityLabelPlural));
				}
				else if (selection.size() <= 1) {//set to all
					result.setText(MessageReplacer.replace(EDIT_ALL.get(), columnLabel, entityLabelPlural));
				}
			}
		});
		return result;
	}

}
