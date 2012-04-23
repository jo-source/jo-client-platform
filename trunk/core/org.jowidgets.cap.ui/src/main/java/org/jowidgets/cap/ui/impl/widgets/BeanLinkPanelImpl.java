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

import java.util.List;

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.ISplitComposite;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel.IBeanLink;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanelBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

final class BeanLinkPanelImpl<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		AbstractInputControl<IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> implements
		IBeanLinkPanel<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> {

	private IBeanForm<LINK_BEAN_TYPE> linkForm;
	private IBeanForm<LINKABLE_BEAN_TYPE> linkableForm;
	private IBeanTable<LINKABLE_BEAN_TYPE> linkableTable;

	public BeanLinkPanelImpl(
		final IComposite composite,
		final IBeanLinkPanelBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> bluePrint) {
		super(composite);

		final IBeanFormBluePrint<LINK_BEAN_TYPE> linkFormBp = bluePrint.getLinkBeanForm();
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableFormBp = bluePrint.getLinkableBeanForm();
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTableBp = bluePrint.getLinkableTable();

		final boolean hasForm = linkFormBp != null || linkableFormBp != null;
		final boolean hasTable = linkableTableBp != null;

		if (hasForm && hasTable) {
			createWithSplit(composite, linkFormBp, linkableFormBp, linkableTableBp);
		}
		else if (hasForm) {
			createForms(composite, linkFormBp, linkableFormBp);
		}
		else if (hasTable) {
			createTable(composite, linkableTableBp);
		}
		else {
			throw new IllegalArgumentException("The bean link blueprint has neither forms nor a table.");
		}
	}

	private void createWithSplit(
		final IComposite composite,
		final IBeanFormBluePrint<LINK_BEAN_TYPE> linkFormBp,
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableFormBp,
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTableBp) {

		composite.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final ISplitComposite split = composite.add(BPF.splitVertical(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		createForms(split.getFirst(), linkFormBp, linkableFormBp);
		createTable(split.getSecond(), linkableTableBp);
	}

	private void createForms(
		final IContainer container,
		final IBeanFormBluePrint<LINK_BEAN_TYPE> linkFormBp,
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableFormBp) {

		container.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final IScrollComposite content = container.add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		if (linkFormBp != null && linkableFormBp != null) {
			content.setLayout(new MigLayoutDescriptor("[grow, 0::]", "0[]0[]0[]0"));
			this.linkForm = content.add(createModifiedFormBp(linkFormBp), "growx, w 0::, wrap");
			content.add(BPF.separator(), "growx, w 0::, wrap");
			this.linkableForm = content.add(createModifiedFormBp(linkableFormBp), "growx, w 0::");
		}
		else {
			content.setLayout(new MigLayoutDescriptor("[grow, 0::]", "[]"));
			if (linkFormBp != null) {
				this.linkForm = content.add(createModifiedFormBp(linkFormBp), "growx, w 0::");
			}
			if (linkableFormBp != null) {
				this.linkableForm = content.add(createModifiedFormBp(linkableFormBp), "growx, w 0::");
			}
		}

	}

	private <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> createModifiedFormBp(final IBeanFormBluePrint<BEAN_TYPE> formBp) {
		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
		cbpf.beanForm();
		final IBeanFormBluePrint<BEAN_TYPE> result = CapUiToolkit.bluePrintFactory().beanForm();
		result.setSetup(formBp);
		result.setEditModeValidationLabelVisible(false);
		result.setCreateModeValidationLabelVisible(false);
		result.setContentScrolled(false);
		return result;
	}

	private void createTable(final IContainer container, final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTableBp) {
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> beanTableBpCopy = CapUiToolkit.bluePrintFactory().beanTable();
		beanTableBpCopy.setSetup(linkableTableBp);
		beanTableBpCopy.setSearchFilterToolbarVisible(true);
		container.setLayout(MigLayoutFactory.growingInnerCellLayout());
		this.linkableTable = container.add(beanTableBpCopy, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
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
		if (value != null) {
			if (linkForm != null) {
				linkForm.setValue(value.getLinkBean());
			}
			if (linkableForm != null) {
				linkableForm.setValue(getFirstTransientBean(value.getLinkedBeans()));
			}
		}
		else {
			if (linkForm != null) {
				linkForm.setValue(null);
			}
			if (linkableForm != null) {
				linkableForm.setValue(null);
			}
		}
		if (linkableTable != null) {
			linkableTable.getModel().load();
		}
	}

	private IBeanProxy<LINKABLE_BEAN_TYPE> getFirstTransientBean(final List<IBeanProxy<LINKABLE_BEAN_TYPE>> linkedBeans) {
		if (linkedBeans != null) {
			for (final IBeanProxy<LINKABLE_BEAN_TYPE> bean : linkedBeans) {
				if (bean.isTransient()) {
					return bean;
				}
			}
		}
		return null;
	}

	@Override
	public IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> getValue() {

		return null;
	}

	@Override
	public void setEditable(final boolean editable) {
		if (linkForm != null) {
			linkForm.setEditable(editable);
		}
		if (linkableForm != null) {
			linkableForm.setEditable(editable);
		}
		if (linkableTable != null) {
			linkableTable.setEditable(editable);
		}
	}

	@Override
	protected IValidationResult createValidationResult() {
		return ValidationResult.ok();
	}

}
