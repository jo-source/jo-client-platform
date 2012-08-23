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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.table.BeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfig;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeSelection;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeSelectionListener;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetail;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetailBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableLifecycleInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.tools.bean.SingleBeanSelectionProvider;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;
import org.jowidgets.util.Assert;
import org.jowidgets.util.Tuple;

final class BeanRelationTreeDetailImpl<CHILD_BEAN_TYPE> extends ControlWrapper implements
		IBeanRelationTreeDetail<CHILD_BEAN_TYPE> {

	private final IBeanRelationTreeModel<CHILD_BEAN_TYPE> treeModel;
	private final Map<Object, Tuple<IBeanTableModelConfig, ArrayList<Integer>>> tableConfigs;
	private final Set<IBeanTableLifecycleInterceptor<Object>> tableLifecycleInterceptors;
	private final ICapApiBluePrintFactory cbpf;
	private final IComposite tableContainer;
	private final IComposite beanFormContainer;
	private final IBeanRelationTreeSelectionListener treeSelectionListener;

	private IBeanTable<Object> lastBeanTable;
	private IBeanRelationNodeModel<Object, Object> lastParentRelation;

	BeanRelationTreeDetailImpl(final IComposite composite, final IBeanRelationTreeDetailBluePrint<CHILD_BEAN_TYPE> bluePrint) {
		super(composite);
		Assert.paramNotNull(bluePrint.getModel(), "bluePrint.getModel()");

		this.treeModel = bluePrint.getModel();
		this.tableConfigs = new HashMap<Object, Tuple<IBeanTableModelConfig, ArrayList<Integer>>>();
		this.tableLifecycleInterceptors = new LinkedHashSet<IBeanTableLifecycleInterceptor<Object>>();
		this.cbpf = CapUiToolkit.bluePrintFactory();

		composite.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[grow, 0::]0"));

		this.beanFormContainer = composite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		beanFormContainer.setLayout(MigLayoutFactory.growingCellLayout());
		beanFormContainer.add(cbpf.beanSelectionForm(treeModel), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

		this.tableContainer = composite.add(BPF.composite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		tableContainer.setLayout(MigLayoutFactory.growingInnerCellLayout());
		tableContainer.setVisible(false);

		this.treeSelectionListener = new IBeanRelationTreeSelectionListener() {
			@Override
			public void selectionChanged(final IBeanRelationTreeSelection selection) {
				onSelectionChanged(selection);
			}
		};

		treeModel.addBeanRelationTreeSelectionListener(treeSelectionListener);
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addBeanTableLifecycleInterceptor(final IBeanTableLifecycleInterceptor<?> interceptor) {
		Assert.paramNotNull(interceptor, "interceptor");
		tableLifecycleInterceptors.add((IBeanTableLifecycleInterceptor<Object>) interceptor);
	}

	@Override
	public void removeBeanTableLifecycleInterceptor(final IBeanTableLifecycleInterceptor<?> interceptor) {
		Assert.paramNotNull(interceptor, "interceptor");
		tableLifecycleInterceptors.remove(interceptor);
	}

	@Override
	public void dispose() {
		treeModel.removeBeanRelationTreeSelectionListener(treeSelectionListener);
		tableConfigs.clear();
		tableLifecycleInterceptors.clear();
		disposeTable();
		super.dispose();
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> getModel() {
		return treeModel;
	}

	private void onSelectionChanged(final IBeanRelationTreeSelection selection) {
		saveTableConfig();

		final IBeanRelationNodeModel<Object, Object> relation = selection.getParentRelation();
		if (selection.getFirstBean() == null && relation != null) {
			onParentRelationChanged(relation);
		}
		else {
			onBeanChanged();
		}
	}

	private void onBeanChanged() {
		if (!beanFormContainer.isVisible()) {
			getWidget().layoutBegin();
			beanFormContainer.setVisible(true);
			tableContainer.setVisible(false);
			getWidget().layoutEnd();
		}
		disposeTable();
		lastBeanTable = null;
		lastParentRelation = null;
	}

	private void onParentRelationChanged(final IBeanRelationNodeModel<Object, Object> relation) {
		if (relation != lastParentRelation) {
			getWidget().layoutBegin();

			lastParentRelation = relation;
			beanFormContainer.setVisible(false);

			disposeTable();

			final IBeanTableModel<Object> tableModel = createBeanTableModel(lastParentRelation);

			final IBeanTableBluePrint<Object> beanTableBp = cbpf.beanTable(tableModel);
			fireOnTableCreate(relation, beanTableBp);
			lastBeanTable = tableContainer.add(beanTableBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

			setTableConfigIfExists();

			fireAfterTableCreated(relation, lastBeanTable);

			tableModel.load();

			if (!tableContainer.isVisible()) {
				tableContainer.setVisible(true);
			}
			getWidget().layoutEnd();
		}
	}

	private IBeanTableModel<Object> createBeanTableModel(final IBeanRelationNodeModel<Object, Object> relation) {
		final IBeanTableModelBuilder<Object> builder;
		builder = BeanTableModel.builder(relation.getChildEntityId(), relation.getChildBeanType());

		if (relation.getParentBean() != null) {
			builder.setParent(new SingleBeanSelectionProvider<Object>(
				relation.getParentBean(),
				relation.getChildEntityId(),
				relation.getChildBeanType()), LinkType.SELECTION_FIRST);
		}

		fireOnModelCreate(relation, builder);
		final IBeanTableModel<Object> result = builder.build();
		fireAfterModelCreated(relation, result);
		treeModel.addDataModel(result);
		return result;
	}

	private void saveTableConfig() {
		if (lastBeanTable != null && lastParentRelation != null) {
			final IBeanTableModelConfig tableModelConfig = lastBeanTable.getModel().getConfig();
			final ArrayList<Integer> columnPermutation = lastBeanTable.getColumnPermutation();
			Tuple<IBeanTableModelConfig, ArrayList<Integer>> config;
			config = new Tuple<IBeanTableModelConfig, ArrayList<Integer>>(tableModelConfig, columnPermutation);
			tableConfigs.put(lastParentRelation.getChildEntityId(), config);
		}
	}

	private void setTableConfigIfExists() {
		if (lastBeanTable != null && lastParentRelation != null) {
			final Tuple<IBeanTableModelConfig, ArrayList<Integer>> config = tableConfigs.get(lastParentRelation.getChildEntityId());
			if (config != null) {
				lastBeanTable.getModel().setConfig(config.getFirst());
				lastBeanTable.setColumnPermutation(config.getSecond());
			}
		}
	}

	private void disposeTable() {
		if (lastBeanTable != null) {
			final IBeanTableModel<Object> model = lastBeanTable.getModel();
			fireBeforeTableDispose(lastBeanTable);
			treeModel.removeDataModel(model);
			tableContainer.removeAll();
			fireBeforeModelDispose(model);
			model.dispose();
		}
	}

	private void fireOnModelCreate(
		final IBeanRelationNodeModel<Object, Object> relationNode,
		final IBeanTableModelBuilder<Object> builder) {
		for (final IBeanTableLifecycleInterceptor<Object> interceptor : new LinkedList<IBeanTableLifecycleInterceptor<Object>>(
			tableLifecycleInterceptors)) {
			interceptor.onModelCreate(relationNode, builder);
		}
	}

	private void fireAfterModelCreated(
		final IBeanRelationNodeModel<Object, Object> relationNode,
		final IBeanTableModel<Object> model) {
		for (final IBeanTableLifecycleInterceptor<Object> interceptor : new LinkedList<IBeanTableLifecycleInterceptor<Object>>(
			tableLifecycleInterceptors)) {
			interceptor.afterModelCreated(relationNode, model);
		}
	}

	private void fireOnTableCreate(
		final IBeanRelationNodeModel<Object, Object> relationNode,
		final IBeanTableSetupBuilder<Object> builder) {
		for (final IBeanTableLifecycleInterceptor<Object> interceptor : new LinkedList<IBeanTableLifecycleInterceptor<Object>>(
			tableLifecycleInterceptors)) {
			interceptor.onTableCreate(relationNode, builder);
		}
	}

	private void fireAfterTableCreated(final IBeanRelationNodeModel<Object, Object> relationNode, final IBeanTable<Object> table) {
		for (final IBeanTableLifecycleInterceptor<Object> interceptor : new LinkedList<IBeanTableLifecycleInterceptor<Object>>(
			tableLifecycleInterceptors)) {
			interceptor.afterTableCreated(relationNode, table);
		}
	}

	private void fireBeforeTableDispose(final IBeanTable<Object> table) {
		for (final IBeanTableLifecycleInterceptor<Object> interceptor : new LinkedList<IBeanTableLifecycleInterceptor<Object>>(
			tableLifecycleInterceptors)) {
			interceptor.beforeTableDispose(table);
		}
	}

	private void fireBeforeModelDispose(final IBeanTableModel<Object> model) {
		for (final IBeanTableLifecycleInterceptor<Object> interceptor : new LinkedList<IBeanTableLifecycleInterceptor<Object>>(
			tableLifecycleInterceptors)) {
			interceptor.beforeModelDispose(model);
		}
	}
}
