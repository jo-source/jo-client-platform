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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.ISplitComposite;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IFilterToolkit;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiBooleanFilterBuilder;
import org.jowidgets.cap.ui.api.filter.IUiFilterFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel.IBeanLink;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanelBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

final class BeanLinkPanelImpl<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		AbstractInputControl<IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> implements
		IBeanLinkPanel<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> {

	private static final String BEAN_LINK_SEARCH_FILTER_ID = "BEAN_LINK_SEARCH_FILTER_ID";

	private static final IMessage SELECT_DATASET = Messages.getMessage("BeanLinkPanelImpl.selectDataset");
	private static final IMessage SELECT_OR_INPUT_DATASET = Messages.getMessage("BeanLinkPanelImpl.selectOrInputDataset");

	private static final int LOAD_DELAY = 400;

	private final BeanFilterListener beanFilterListener;

	private IBeanForm<LINK_BEAN_TYPE> linkForm;
	private IBeanForm<LINKABLE_BEAN_TYPE> linkableForm;
	private IBeanTable<LINKABLE_BEAN_TYPE> linkableTable;

	private IBeanProxy<LINKABLE_BEAN_TYPE> createdLinkableBean;

	private boolean editable;

	public BeanLinkPanelImpl(
		final IComposite composite,
		final IBeanLinkPanelBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> bluePrint) {
		super(composite);

		this.editable = true;

		this.beanFilterListener = new BeanFilterListener();

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

		final IValidationConditionListener validationConditionListener = new IValidationConditionListener() {
			@Override
			public void validationConditionsChanged() {
				setValidationCacheDirty();
			}
		};

		if (linkForm != null) {
			linkForm.addValidationConditionListener(validationConditionListener);
		}
		if (linkableForm != null) {
			linkableForm.addValidationConditionListener(validationConditionListener);
		}
	}

	private void createWithSplit(
		final IComposite composite,
		final IBeanFormBluePrint<LINK_BEAN_TYPE> linkFormBp,
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableFormBp,
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTableBp) {

		composite.setLayout(MigLayoutFactory.growingInnerCellLayout());
		final ISplitComposite split = composite.add(BPF.splitVertical(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		createTable(split.getFirst(), linkableTableBp);
		createForms(split.getSecond(), linkFormBp, linkableFormBp);
	}

	private void createForms(
		final IContainer container,
		final IBeanFormBluePrint<LINK_BEAN_TYPE> linkFormBp,
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableFormBp) {

		if (linkFormBp != null && linkableFormBp != null) {

			final boolean scrolledContent = linkFormBp.getScrollbarsAllowed() && linkableFormBp.getScrollbarsAllowed();

			final IContainer content;
			if (scrolledContent) {
				container.setLayout(MigLayoutFactory.growingInnerCellLayout());
				content = container.add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
			}
			else {
				content = container;
			}

			content.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[]0[]0[]0"));
			final IBeanFormBluePrint<LINK_BEAN_TYPE> modifiedLinkFormBp = createModifiedFormBp(linkFormBp);
			if (scrolledContent) {
				modifiedLinkFormBp.setScrollbarsAllowed(false);
			}
			this.linkForm = content.add(modifiedLinkFormBp, "growx, w 0::, wrap");

			content.add(BPF.separator(), "growx, w 0::, wrap");
			final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> modifiedLinkableFormBp = createModifiedFormBp(linkableFormBp);
			if (scrolledContent) {
				modifiedLinkableFormBp.setScrollbarsAllowed(false);
			}
			this.linkableForm = content.add(modifiedLinkableFormBp, "growx, w 0::");
		}
		else {
			container.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[grow, 0::]0"));
			if (linkFormBp != null) {
				this.linkForm = container.add(createModifiedFormBp(linkFormBp), "growx, w 0::, growy, h 0::");
			}
			if (linkableFormBp != null) {
				this.linkableForm = container.add(createModifiedFormBp(linkableFormBp), "growx, w 0::, growy, h 0::");
			}
		}

	}

	private <BEAN_TYPE> IBeanFormBluePrint<BEAN_TYPE> createModifiedFormBp(final IBeanFormBluePrint<BEAN_TYPE> formBp) {
		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
		cbpf.beanForm();
		final IBeanFormBluePrint<BEAN_TYPE> result = CapUiToolkit.bluePrintFactory().beanForm();
		result.setSetup(formBp);
		result.setEditModeValidationLabel(null);
		result.setCreateModeValidationLabel(null);
		result.setSaveAction(null);
		result.setUndoAction(null);
		return result;
	}

	private void createTable(final IContainer container, final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTableBp) {
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> beanTableBpCopy = CapUiToolkit.bluePrintFactory().beanTable();
		beanTableBpCopy.setSetup(linkableTableBp);
		beanTableBpCopy.setSearchFilterToolbarVisible(true);
		container.setLayout(MigLayoutFactory.growingInnerCellLayout());
		this.linkableTable = container.add(beanTableBpCopy, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		final IBeanSelectionListener<LINKABLE_BEAN_TYPE> selectionListener = new IBeanSelectionListener<LINKABLE_BEAN_TYPE>() {
			@Override
			public void selectionChanged(final IBeanSelectionEvent<LINKABLE_BEAN_TYPE> selectionEvent) {
				final IBeanProxy<LINKABLE_BEAN_TYPE> firstSelected = selectionEvent.getFirstSelected();

				if (firstSelected != null) {
					if (linkableForm != null) {
						if (createdLinkableBean != null && linkableTable != null) {
							createdLinkableBean.removePropertyChangeListener(beanFilterListener);
						}
						linkableForm.setValue(firstSelected);
						linkableForm.setEditable(false);
					}
				}
				else {
					if (linkableForm != null) {
						linkableForm.setValue(createdLinkableBean);
						if (createdLinkableBean != null && linkableTable != null) {
							createdLinkableBean.addPropertyChangeListener(beanFilterListener);
						}
						linkableForm.setEditable(true);
					}
				}

				setValidationCacheDirty();
			}
		};
		linkableTable.getModel().addBeanSelectionListener(selectionListener);

		linkableTable.addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				linkableTable.getModel().removeBeanSelectionListener(selectionListener);
			}
		});
	}

	@Override
	protected IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();
		if (!hasSelection()) {
			if (linkableForm != null) {
				if (!linkableForm.hasModifications()) {
					if (linkableTable != null) {
						builder.addInfoError(SELECT_OR_INPUT_DATASET.get());
					}
					else {
						builder.addInfoError(SELECT_DATASET.get());
					}
				}
				else {
					builder.addResult(linkableForm.validate());
				}
			}
			else if (linkableTable != null) {
				builder.addInfoError(SELECT_DATASET.get());
			}
		}
		if (linkForm != null) {
			builder.addResult(linkForm.validate());
		}
		return builder.build();
	}

	private boolean hasSelection() {
		if (linkableTable != null) {
			return !linkableTable.getModel().getSelectedBeans().isEmpty();
		}
		return false;
	}

	@Override
	public boolean hasModifications() {
		if (linkableTable != null && !linkableTable.getModel().getSelectedBeans().isEmpty()) {
			return true;
		}
		if (linkForm != null && linkForm.hasModifications()) {
			return true;
		}
		if (linkableForm != null && linkableForm.hasModifications()) {
			return true;
		}
		return false;
	}

	@Override
	public void resetModificationState() {
		if (linkForm != null) {
			linkForm.resetModificationState();
		}
		if (linkableForm != null) {
			linkableForm.resetModificationState();
		}
	}

	@Override
	public void setValue(final IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> value) {
		if (createdLinkableBean != null && linkableTable != null) {
			createdLinkableBean.removePropertyChangeListener(beanFilterListener);
		}

		if (value != null) {
			if (linkForm != null) {
				linkForm.setValue(value.getLinkBean());
			}
			if (linkableForm != null) {
				linkableForm.setValue(getFirstTransientBean(value.getLinkableBeans()));
				createdLinkableBean = linkableForm.getValue();
				if (createdLinkableBean != null && linkableTable != null) {
					createdLinkableBean.addPropertyChangeListener(beanFilterListener);
				}
			}
		}
		else {
			if (linkForm != null) {
				linkForm.setValue(null);
			}
			if (linkableForm != null) {
				linkableForm.setValue(null);
				createdLinkableBean = null;
			}
		}
		if (linkableTable != null) {
			doFilter(createdLinkableBean);
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
		final IBeanProxy<LINK_BEAN_TYPE> linkBean = getLinkBean();
		final List<IBeanProxy<LINKABLE_BEAN_TYPE>> linkedBeans = Collections.unmodifiableList(getLinkedBeans());

		return new IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>() {
			@Override
			public IBeanProxy<LINK_BEAN_TYPE> getLinkBean() {
				return linkBean;
			}

			@Override
			public List<IBeanProxy<LINKABLE_BEAN_TYPE>> getLinkableBeans() {
				return linkedBeans;
			}
		};
	}

	private IBeanProxy<LINK_BEAN_TYPE> getLinkBean() {
		if (linkForm != null) {
			return linkForm.getValue();
		}
		else {
			return null;
		}
	}

	private List<IBeanProxy<LINKABLE_BEAN_TYPE>> getLinkedBeans() {
		if (linkableTable != null) {
			final List<IBeanProxy<LINKABLE_BEAN_TYPE>> selectedBeans = linkableTable.getModel().getSelectedBeans();
			if (!EmptyCheck.isEmpty(selectedBeans)) {
				return selectedBeans;
			}
		}
		return getLinkedBeansFromForm();
	}

	private List<IBeanProxy<LINKABLE_BEAN_TYPE>> getLinkedBeansFromForm() {
		if (linkableForm != null) {
			final IBeanProxy<LINKABLE_BEAN_TYPE> value = linkableForm.getValue();
			if (value != null) {
				return Collections.singletonList(value);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void setEditable(final boolean editable) {
		this.editable = editable;
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
	public boolean isEditable() {
		return editable;
	}

	private void doFilter(final IBeanProxy<LINKABLE_BEAN_TYPE> bean) {
		final IBeanTableModel<LINKABLE_BEAN_TYPE> tableModel = linkableTable.getModel();

		final IFilterToolkit filterToolkit = CapUiToolkit.filterToolkit();
		final IUiFilterFactory filterFactory = filterToolkit.filterFactory();
		final IUiBooleanFilterBuilder filterBuilder;
		filterBuilder = filterFactory.booleanFilterBuilder().setOperator(BooleanOperator.AND);
		if (bean != null) {
			for (final IBeanModification modification : bean.getModifications()) {
				final String propertyName = modification.getPropertyName();
				final IAttribute<Object> attribute = tableModel.getAttribute(propertyName);

				final IIncludingFilterFactory<Object> includingFilterFactory = getIncludingFilterFactory(attribute);
				if (includingFilterFactory != null) {
					final Object operand = getModifiedFilterOperand(modification.getNewValue());
					filterBuilder.addFilter(includingFilterFactory.getIncludingFilter(operand));
				}
			}
		}

		if (filterBuilder.hasEntries()) {
			tableModel.setFilter(BEAN_LINK_SEARCH_FILTER_ID, filterBuilder.build());
		}
		else {
			tableModel.setFilter(BEAN_LINK_SEARCH_FILTER_ID, null);
		}

		tableModel.loadScheduled(LOAD_DELAY);
	}

	private IIncludingFilterFactory<Object> getIncludingFilterFactory(final IAttribute<Object> attribute) {
		if (attribute.isFilterable() && attribute.isSearchable()) {
			final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();
			if (controlPanel != null) {
				final IFilterSupport<Object> filterSupport = controlPanel.getFilterSupport();
				if (filterSupport != null) {
					return filterSupport.getIncludingFilterFactory();
				}
			}
		}
		return null;
	}

	private Object getModifiedFilterOperand(final Object value) {
		if (value instanceof Collection<?>) {
			final Collection<?> collection = (Collection<?>) value;
			final List<Object> resultList = new LinkedList<Object>();
			for (final Object element : collection) {
				resultList.add(getModifiedFilterOperand(element));
			}
			return resultList;
		}
		else if (value instanceof String) {
			return value.toString().trim() + "*";
		}
		else {
			return value;
		}
	}

	private final class BeanFilterListener implements PropertyChangeListener {

		@SuppressWarnings("unchecked")
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			doFilter((IBeanProxy<LINKABLE_BEAN_TYPE>) evt.getSource());
		}

	}

	@Override
	public void dispose() {
		if (linkForm != null) {
			linkForm.dispose();
		}
		if (linkableForm != null) {
			linkableForm.dispose();
		}
		if (linkableTable != null) {
			linkableTable.dispose();
		}
		super.dispose();
	}

}
