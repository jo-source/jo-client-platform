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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableView;
import org.jowidgets.cap.ui.api.widgets.IBeanTableViewListener;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesForm;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesFormBluePrint;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;

final class BeanTablesFormImpl extends ControlWrapper implements IBeanTablesForm {

	private final boolean hideReadonlyAttributes;
	private final boolean hideMetaAttributes;
	private final Map<IBeanTableView<?>, IBeanForm<?>> forms;
	private final Map<IBeanTableView<?>, TableViewListener> viewListeners;
	private final Map<IBeanTableView<?>, BeanSelectionListener<?>> tableSelectionListeners;

	BeanTablesFormImpl(final IComposite composite, final IBeanTablesFormBluePrint bluePrint) {
		super(composite);
		this.hideReadonlyAttributes = bluePrint.getHideReadonlyAttributes();
		this.hideMetaAttributes = bluePrint.getHideMetaAttributes();
		this.forms = new HashMap<IBeanTableView<?>, IBeanForm<?>>();
		this.viewListeners = new HashMap<IBeanTableView<?>, TableViewListener>();
		this.tableSelectionListeners = new HashMap<IBeanTableView<?>, BeanSelectionListener<?>>();
		composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public <BEAN_TYPE> void registerView(final IBeanTableView<BEAN_TYPE> view, final IBeanFormBluePrint<BEAN_TYPE> formBluePrint) {
		Assert.paramNotNull(view, "view");
		Assert.paramNotNull(formBluePrint, "formBluePrint");
		if (!forms.containsKey(view)) {
			final IBeanForm<BEAN_TYPE> beanForm = getWidget().add(formBluePrint, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

			forms.put(view, beanForm);
			final IBeanTableModel<BEAN_TYPE> model = view.getModel();

			final BeanSelectionListener<BEAN_TYPE> selectionListener = new BeanSelectionListener<BEAN_TYPE>(view, beanForm);
			tableSelectionListeners.put(view, selectionListener);
			model.addBeanSelectionListener(selectionListener);

			final TableViewListener viewListener = new TableViewListener(view);
			viewListeners.put(view, viewListener);
			view.addViewListener(viewListener);

			view.addDisposeListener(new IDisposeListener() {
				@Override
				public void onDispose() {
					unregisterView(view);
				}
			});

			if (forms.size() > 1) {
				beanForm.setVisible(false);
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void registerView(final IBeanTableView view) {
		Assert.paramNotNull(view, "view");
		final IBeanTableModel model = view.getModel();
		List<IAttribute<Object>> attributes = EntityServiceAttributesFactory.createAttributes(model.getEntityId());
		if (attributes == null) {
			attributes = model.getAttributes();
		}
		attributes = getFilteredAttributes(attributes);
		final IBeanFormBluePrint beanFormBp = CapUiToolkit.bluePrintFactory().beanForm(model.getEntityId(), attributes);
		registerView(view, beanFormBp);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void unregisterView(final IBeanTableView<?> view) {
		Assert.paramNotNull(view, "table");
		final IBeanForm form = forms.get(view);
		if (form != null) {
			final IBeanTableModel model = view.getModel();
			model.removeBeanSelectionListener(tableSelectionListeners.get(view));
			view.removeViewListener(viewListeners.get(view));
			getWidget().remove(form);
			forms.remove(form);
		}
	}

	private List<IAttribute<Object>> getFilteredAttributes(final List<IAttribute<Object>> attributes) {
		final List<IAttribute<Object>> result = new LinkedList<IAttribute<Object>>();
		for (final IAttribute<Object> attribute : attributes) {
			if ((!hideReadonlyAttributes || attribute.isEditable())
				&& (!hideMetaAttributes || !IBeanProxy.ALL_META_ATTRIBUTES.contains(attribute.getPropertyName()))) {
				result.add(attribute);
			}
		}
		return result;
	}

	private void switchToView(final IBeanTableView<?> view) {
		final IBeanForm<?> form = forms.get(view);
		if (!form.isVisible()) {
			for (final IBeanForm<?> childForm : forms.values()) {
				childForm.setVisible(false);
			}
			form.setVisible(true);
			form.setSize(getWidget().getSize());
		}
	}

	private final class BeanSelectionListener<BEAN_TYPE> implements IBeanSelectionListener<BEAN_TYPE> {

		private final IBeanTableView<BEAN_TYPE> view;
		private final IBeanForm<BEAN_TYPE> beanForm;

		private BeanSelectionListener(final IBeanTableView<BEAN_TYPE> view, final IBeanForm<BEAN_TYPE> beanForm) {
			super();
			this.view = view;
			this.beanForm = beanForm;
		}

		@Override
		public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
			switchToView(view);
			beanForm.setValue(selectionEvent.getFirstSelected());
		}

	}

	private final class TableViewListener implements IBeanTableViewListener {

		private final IBeanTableView<?> view;

		private TableViewListener(final IBeanTableView<?> view) {
			this.view = view;
		}

		@Override
		public void viewActivated() {
			switchToView(view);
		}

	}
}
