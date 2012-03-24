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

package org.jowidgets.cap.ui.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelBluePrint;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelConfigurator;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IEntityTypeId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.Tuple;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

public class BeanRelationTreeModelImpl<CHILD_BEAN_TYPE> implements IBeanRelationTreeModel<CHILD_BEAN_TYPE> {

	private final IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> root;
	private final IBeanRelationNodeModelConfigurator nodeConfigurator;
	private final IBeanListModelListener parentModelListener;
	private final BeanSelectionObservable beanSelectionObservable;

	@SuppressWarnings("rawtypes")
	private final Map relationNodes;

	@SuppressWarnings("rawtypes")
	public BeanRelationTreeModelImpl(
		final IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> root,
		final IBeanRelationNodeModelConfigurator nodeConfigurator,
		final IBeanListModel<?> parent,
		final LinkType linkType) {

		Assert.paramNotNull(root, "root");
		Assert.paramNotNull(nodeConfigurator, "nodeConfigurator");

		this.root = root;
		this.nodeConfigurator = nodeConfigurator;
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			final IProvider<Object> parentBeanProvider = new IProvider<Object>() {
				@Override
				public Object get() {
					final List<IBeanProxy<?>> parentBeans = new LinkedList<IBeanProxy<?>>();
					for (final Integer selected : parent.getSelection()) {
						parentBeans.add(parent.getBean(selected.intValue()));
					}
					return parentBeans;
				}
			};
			this.parentModelListener = new ParentBeanListModelListener(this, parentBeanProvider);
			parent.addBeanListModelListener(parentModelListener);
		}
		else {
			this.parentModelListener = null;
		}

		this.relationNodes = new HashMap();
		this.beanSelectionObservable = new BeanSelectionObservable();
	}

	@Override
	public IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> getRoot() {
		return root;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE> IBeanRelationNodeModel<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE> getNode(
		final IEntityTypeId<METHOD_PARENT_BEAN_TYPE> parentEntityTypeId,
		final IBeanProxy<METHOD_PARENT_BEAN_TYPE> parentBean,
		final IEntityTypeId<METHOD_CHILD_BEAN_TYPE> childEntityTypeId) {

		final Tuple<IBeanProxy<METHOD_PARENT_BEAN_TYPE>, IEntityTypeId<METHOD_CHILD_BEAN_TYPE>> key;
		key = new Tuple<IBeanProxy<METHOD_PARENT_BEAN_TYPE>, IEntityTypeId<METHOD_CHILD_BEAN_TYPE>>(parentBean, childEntityTypeId);

		Object result = relationNodes.get(key);
		if (result == null) {
			BeanRelationNodeModelBuilder<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE, IBeanRelationNodeModelBluePrint<?, ?>> builder;
			builder = new BeanRelationNodeModelBuilder<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE, IBeanRelationNodeModelBluePrint<?, ?>>(
				parentEntityTypeId,
				parentBean,
				childEntityTypeId);
			nodeConfigurator.configureNode(childEntityTypeId, builder);
			result = builder.build();
			relationNodes.put(key, result);
		}
		return (IBeanRelationNodeModel<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE>) result;
	}

	@Override
	public void load() {
		clear();
		root.load();
	}

	@Override
	public void clear() {
		root.clear();
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			relationModel.dispose();
		}
		relationNodes.clear();
	}

	@Override
	public void save() {
		root.save();
	}

	@Override
	public void undo() {
		root.undo();
	}

	@Override
	public boolean hasModifications() {
		return false;
	}

	@Override
	public boolean hasExecutions() {
		return false;
	}

	@Override
	public void cancelExecutions() {}

	@Override
	public IValidationResult validate() {
		return null;
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {}

	@Override
	public void addBeanSelectionListener(final IBeanSelectionListener listener) {
		beanSelectionObservable.addBeanSelectionListener(listener);
	}

	@Override
	public void removeBeanSelectionListener(final IBeanSelectionListener listener) {
		beanSelectionObservable.removeBeanSelectionListener(listener);
	}

	@Override
	public ArrayList<IBeanProxy<?>> getSelection() {
		return new ArrayList<IBeanProxy<?>>();
	}

	@Override
	public void setSelection(final Collection<? extends IBeanProxy<IBeanProxy<?>>> selection) {}

}
