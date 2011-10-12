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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesForm;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesFormBluePrint;
import org.jowidgets.common.widgets.controller.IFocusListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;

final class BeanTablesFormImpl extends ControlWrapper implements IBeanTablesForm {

	private final Map<IBeanTable<?>, IBeanForm<?>> forms;
	private final Map<IBeanTable<?>, FocusListener> focusListeners;
	private final Map<IBeanTable<?>, TableModelListener<?>> tableModelListeners;

	BeanTablesFormImpl(final IComposite composite, final IBeanTablesFormBluePrint bluePrint) {
		super(composite);
		this.forms = new HashMap<IBeanTable<?>, IBeanForm<?>>();
		this.focusListeners = new HashMap<IBeanTable<?>, FocusListener>();
		this.tableModelListeners = new HashMap<IBeanTable<?>, TableModelListener<?>>();
		composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public <BEAN_TYPE> void registerTable(final IBeanTable<BEAN_TYPE> table, final IBeanFormBluePrint<BEAN_TYPE> formBluePrint) {
		Assert.paramNotNull(table, "table");
		Assert.paramNotNull(formBluePrint, "formBluePrint");
		if (!forms.containsKey(table)) {
			final IBeanForm<BEAN_TYPE> beanForm = getWidget().add(formBluePrint, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

			forms.put(table, beanForm);
			final IBeanTableModel<BEAN_TYPE> model = table.getModel();

			final TableModelListener<BEAN_TYPE> tableModelListener = new TableModelListener<BEAN_TYPE>(table, beanForm);
			tableModelListeners.put(table, tableModelListener);
			model.addBeanListModelListener(tableModelListener);

			final FocusListener focusListener = new FocusListener(table);
			focusListeners.put(table, focusListener);
			table.addFocusListener(focusListener);

			table.addDisposeListener(new IDisposeListener() {
				@Override
				public void onDispose() {
					unregisterTable(table);
				}
			});

			if (forms.size() > 1) {
				if (table.hasFocus()) {
					switchToTable(table);
				}
				else {
					beanForm.setVisible(false);
				}
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void registerTable(final IBeanTable table) {
		Assert.paramNotNull(table, "table");
		final List<IAttribute> attributes = table.getModel().getAttributes();
		final List editableAttributes = new LinkedList();
		for (final IAttribute attribute : attributes) {
			if (attribute.isEditable()) {
				editableAttributes.add(attribute);
			}
		}
		final IBeanFormBluePrint beanFormBp = CapUiToolkit.bluePrintFactory().beanForm(editableAttributes);
		registerTable(table, beanFormBp);
	}

	@Override
	public void unregisterTable(final IBeanTable<?> table) {
		Assert.paramNotNull(table, "table");
		final IBeanForm<?> form = forms.get(table);
		if (form != null) {
			table.getModel().removeBeanListModelListener(tableModelListeners.get(table));
			table.removeFocusListener(focusListeners.get(table));
			getWidget().remove(form);
			forms.remove(form);
		}
	}

	private void switchToTable(final IBeanTable<?> table) {
		final IBeanForm<?> form = forms.get(table);
		if (!form.isVisible()) {
			for (final IBeanForm<?> childForm : forms.values()) {
				childForm.setVisible(false);
			}
			form.setVisible(true);
			form.setSize(getWidget().getSize());
		}
	}

	private final class TableModelListener<BEAN_TYPE> implements IBeanListModelListener {

		private final IBeanTable<BEAN_TYPE> table;
		private final IBeanTableModel<BEAN_TYPE> model;
		private final IBeanForm<BEAN_TYPE> beanForm;

		private TableModelListener(final IBeanTable<BEAN_TYPE> table, final IBeanForm<BEAN_TYPE> beanForm) {
			super();
			this.table = table;
			this.model = table.getModel();
			this.beanForm = beanForm;
		}

		@Override
		public void selectionChanged() {
			switchToTable(table);
			setSelectedBeanValue();
		}

		@Override
		public void beansChanged() {
			setSelectedBeanValue();
		}

		private void setSelectedBeanValue() {

			final ArrayList<Integer> selection = model.getSelection();
			if (selection.size() > 0) {
				beanForm.setValue(model.getBean(selection.get(0)));
			}
			else {
				beanForm.setValue(null);
			}
		}

	}

	private final class FocusListener implements IFocusListener {

		private final IBeanTable<?> table;

		private FocusListener(final IBeanTable<?> table) {
			this.table = table;
		}

		@Override
		public void focusGained() {
			switchToTable(table);
		}

		@Override
		public void focusLost() {}

	}
}
