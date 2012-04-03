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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionObservable;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionForm;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionFormBluePrint;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanSelectionFormImpl extends ControlWrapper implements IBeanSelectionForm {

	private final boolean hideReadonlyAttributes;
	private final boolean hideMetaAttributes;
	private final Map<Object, IBeanForm<Object>> beanForms;
	private final Map<Object, IBeanFormBluePrint<Object>> beanFormBluePrints;
	private final IBeanSelectionListener<Object> beanSelectionListener;

	BeanSelectionFormImpl(final IComposite composite, final IBeanSelectionFormBluePrint bluePrint) {
		super(composite);
		this.hideReadonlyAttributes = bluePrint.getHideReadonlyAttributes();
		this.hideMetaAttributes = bluePrint.getHideMetaAttributes();

		this.beanForms = new HashMap<Object, IBeanForm<Object>>();
		this.beanFormBluePrints = new HashMap<Object, IBeanFormBluePrint<Object>>();

		this.beanSelectionListener = new BeanSelectionListener();

		composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));

		final Collection<? extends IBeanSelectionObservable<?>> selectionObservables = bluePrint.getSelectionObservables();
		if (!EmptyCheck.isEmpty(selectionObservables)) {
			for (final IBeanSelectionObservable<?> selectionObservable : selectionObservables) {
				registerSelectionObservable(selectionObservable);
			}
		}

		final Collection<? extends IBeanFormBluePrint<?>> defaultBeanForms = bluePrint.getBeanForms();
		if (!EmptyCheck.isEmpty(defaultBeanForms)) {
			for (final IBeanFormBluePrint<?> beanFormBp : defaultBeanForms) {
				registerForm(beanFormBp);
			}
		}
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void registerSelectionObservable(final IBeanSelectionObservable selectionObservable) {
		Assert.paramNotNull(selectionObservable, "selectionObservable");
		selectionObservable.addBeanSelectionListener(beanSelectionListener);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void unregisterSelectionObservable(final IBeanSelectionObservable selectionObservable) {
		Assert.paramNotNull(selectionObservable, "selectionObservable");
		selectionObservable.removeBeanSelectionListener(beanSelectionListener);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void registerForm(final IBeanFormBluePrint beanFormBp) {
		Assert.paramNotNull(beanFormBp, "beanForm");
		Assert.paramNotNull(beanFormBp.getEntityId(), "beanForm.getEntityId()");
		final Object entityId = beanFormBp.getEntityId();
		beanFormBluePrints.put(entityId, beanFormBp);
		final IBeanForm<Object> beanForm = beanForms.remove(entityId);
		if (beanForm != null) {
			if (beanForm.isVisible()) {
				final IBeanProxy<Object> bean = beanForm.getValue();
				getWidget().remove(beanForm);
				final IBeanForm<Object> newBeanForm = getBeanForm(entityId);
				newBeanForm.setValue(bean);
			}
			else {
				getWidget().remove(beanForm);
			}
		}
	}

	@Override
	public void unregisterForm(final Object entityId) {
		Assert.paramNotNull(entityId, "entityId");
		beanFormBluePrints.remove(entityId);
		final IBeanForm<Object> beanForm = beanForms.remove(entityId);
		if (beanForm != null) {
			getWidget().remove(beanForm);
		}
	}

	private void onSelectionChanged(final IBeanSelectionEvent<Object> selectionEvent) {
		if (selectionEvent.getEntityId() != null) {
			final IBeanForm<Object> form = getBeanForm(selectionEvent.getEntityId());
			form.setValue(selectionEvent.getFirstSelected());
			if (!form.isVisible()) {
				for (final IBeanForm<?> childForm : beanForms.values()) {
					if (childForm != form) {
						childForm.setVisible(false);
					}
				}
				form.setVisible(true);
				form.setSize(getWidget().getSize());
			}
		}
		else {
			for (final IBeanForm<?> childForm : beanForms.values()) {
				childForm.setVisible(false);
				childForm.setValue(null);
			}
		}
	}

	private IBeanForm<Object> getBeanForm(final Object entityId) {
		IBeanForm<Object> result = beanForms.get(entityId);
		if (result == null) {
			result = createBeanForm(entityId);
			beanForms.put(entityId, result);
		}
		return result;
	}

	private IBeanForm<Object> createBeanForm(final Object entityId) {
		final IBeanFormBluePrint<Object> beanFormBp = getBeanFormBluePrint(entityId);
		return getWidget().add(beanFormBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
	}

	private IBeanFormBluePrint<Object> getBeanFormBluePrint(final Object entityId) {
		IBeanFormBluePrint<Object> result = beanFormBluePrints.get(entityId);
		if (result == null) {
			result = createBeanFormBluePrint(entityId);
			beanFormBluePrints.put(entityId, result);
		}
		return result;
	}

	private IBeanFormBluePrint<Object> createBeanFormBluePrint(final Object entityId) {
		List<IAttribute<Object>> attributes = EntityServiceAttributesFactory.createAttributes(entityId);
		if (attributes != null) {
			attributes = getFilteredAttributes(attributes);
			return CapUiToolkit.bluePrintFactory().beanForm(entityId, attributes);
		}
		else {
			throw new IllegalStateException("Can not create bean form for entity id '" + entityId + "'. No attributes available.");
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

	private final class BeanSelectionListener implements IBeanSelectionListener<Object> {

		@Override
		public void selectionChanged(final IBeanSelectionEvent<Object> selectionEvent) {
			onSelectionChanged(selectionEvent);
		}

	}

}
