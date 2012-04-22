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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.plugin.IBeanRelationTreeModelPlugin;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelBluePrint;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelConfigurator;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModelBuilder;
import org.jowidgets.cap.ui.api.tree.IEntityTypeId;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.ITypedKey;

final class BeanRelationTreeModelBuilderImpl<CHILD_BEAN_TYPE> extends
		BeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE>> implements
		IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> {

	private final List<IBeanRelationNodeModelConfigurator> nodeConfigurators;

	private boolean build;

	BeanRelationTreeModelBuilderImpl(final Object entityId, final Class<CHILD_BEAN_TYPE> beanType) {
		super(entityId, beanType);
		this.build = false;
		this.nodeConfigurators = new LinkedList<IBeanRelationNodeModelConfigurator>();
		nodeConfigurators.add(new IBeanRelationNodeModelConfigurator() {
			@Override
			public <METHOD_CHILD_BEAN_TYPE> void configureNode(
				final IEntityTypeId<METHOD_CHILD_BEAN_TYPE> entityTypeId,
				final IBeanRelationNodeModelBluePrint<METHOD_CHILD_BEAN_TYPE, IBeanRelationNodeModelBluePrint<?, ?>> bluePrint) {
				addChildRelations(entityTypeId, bluePrint);
			}
		});

		addChildRelations(new EntityTypeIdImpl<Object>(entityId, beanType), this);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void addChildRelations(final IEntityTypeId<?> entityTypeId, final IBeanRelationNodeModelBluePrint bluePrint) {
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanDtoDescriptor dtoDescriptor = entityService.getDescriptor(entityTypeId.getEntityId());
			if (dtoDescriptor != null) {
				bluePrint.setText("<" + dtoDescriptor.getLabelPlural() + ">");
			}
			final List<IEntityLinkDescriptor> links = entityService.getEntityLinks(entityTypeId.getEntityId());
			if (links != null) {
				for (final IEntityLinkDescriptor link : links) {
					final Object linkedTypeId = link.getLinkedEntityId();
					final IBeanDtoDescriptor linkedDtoDescr = entityService.getDescriptor(linkedTypeId);
					if (linkedDtoDescr != null && linkedDtoDescr.getBeanType() != null) {
						bluePrint.addChildRelation(linkedTypeId, linkedDtoDescr.getBeanType());
					}
					else {
						bluePrint.addChildRelation(linkedTypeId, IBeanDto.class);
					}
				}
			}
		}
	}

	@Override
	public IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> setNodeConfigurators(
		final Collection<? extends IBeanRelationNodeModelConfigurator> configurators) {
		Assert.paramNotNull(configurators, "configurators");
		nodeConfigurators.clear();
		nodeConfigurators.addAll(configurators);
		return this;
	}

	@Override
	public IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> addNodeConfigurator(
		final IBeanRelationNodeModelConfigurator configurator) {
		Assert.paramNotNull(configurator, "configurator");
		nodeConfigurators.add(configurator);
		return this;
	}

	@Override
	public IBeanRelationTreeModelBuilder<CHILD_BEAN_TYPE> setParentSelectionAsReader(
		final IBeanListModel<?> parent,
		final LinkType linkType) {
		Assert.paramNotNull(parent, "parent");
		Assert.paramNotNull(linkType, "linkType");
		setParent(parent, linkType);
		setReaderService(new ParentSelectionReaderService(parent, linkType));
		return this;
	}

	@Override
	public IBeanRelationTreeModel<CHILD_BEAN_TYPE> build() {
		if (build) {
			throw new IllegalStateException("This is a single use builder and so it can only used once for build.");
		}

		modifyByPlugins();

		final BeanRelationNodeModelImpl<Void, CHILD_BEAN_TYPE> rootNode = new BeanRelationNodeModelImpl<Void, CHILD_BEAN_TYPE>(
			getLabel(),
			null,
			null,
			new EntityTypeIdImpl<CHILD_BEAN_TYPE>(getEntityId(), getBeanType()),
			getChildRenderer(),
			new LinkedList<IEntityTypeId<Object>>(getChildRelations()),
			getReaderService(),
			getReaderParameterProvider(),
			getCreatorService(),
			getRefreshService(),
			getUpdaterService(),
			getDeleterService(),
			getDefaultSort(),
			getBeanValidators(),
			getAttributes());

		build = true;

		return new BeanRelationTreeModelImpl<CHILD_BEAN_TYPE>(
			rootNode,
			getNodeConfigurator(),
			getParent(),
			getLinkType(),
			getListenerDelay());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void modifyByPlugins() {

		final ITypedKey<Object> entitiyIdKey = IBeanRelationTreeModelPlugin.ENTITIY_ID_PROPERTY_KEY;
		final IPluginProperties properties = PluginProperties.create(entitiyIdKey, getEntityId());

		final List<IBeanRelationTreeModelPlugin<?>> plugins;
		plugins = PluginProvider.getPlugins(IBeanRelationTreeModelPlugin.ID, properties);

		for (final IBeanRelationTreeModelPlugin plugin : plugins) {
			plugin.modifySetup(properties, this);
		}
	}

	private IBeanRelationNodeModelConfigurator getNodeConfigurator() {
		return new IBeanRelationNodeModelConfigurator() {
			@Override
			public <METHOD_CHILD_BEAN_TYPE> void configureNode(
				final IEntityTypeId<METHOD_CHILD_BEAN_TYPE> entityTypeId,
				final IBeanRelationNodeModelBluePrint<METHOD_CHILD_BEAN_TYPE, IBeanRelationNodeModelBluePrint<?, ?>> bluePrint) {
				for (final IBeanRelationNodeModelConfigurator configurator : nodeConfigurators) {
					configurator.configureNode(entityTypeId, bluePrint);
				}
			}
		};
	}

	private final class ParentSelectionReaderService implements IReaderService<Void> {

		private final IBeanListModel<?> parent;
		private final LinkType linkType;

		private ParentSelectionReaderService(final IBeanListModel<?> parent, final LinkType linkType) {
			this.parent = parent;
			this.linkType = linkType;
		}

		@Override
		public void read(
			final IResultCallback<List<IBeanDto>> resultCallback,
			final List<? extends IBeanKey> parentBeanKeys,
			final IFilter filter,
			final List<? extends ISort> sorting,
			final int firstRow,
			final int maxRows,
			final Void parameter,
			final IExecutionCallback executionCallback) {

			try {
				final List<IBeanDto> result = new LinkedList<IBeanDto>();

				for (final Integer index : getSelection()) {
					final IBeanProxy<?> bean = parent.getBean(index);
					if (bean != null && !bean.isDisposed() && !bean.isDummy()) {
						result.add(bean);
					}
					if (result.size() >= maxRows) {
						break;
					}
				}

				resultCallback.finished(result);
			}
			catch (final Exception e) {
				resultCallback.exception(e);
			}

		}

		private List<Integer> getSelection() {
			if (linkType == LinkType.SELECTION_ALL) {
				return parent.getSelection();
			}
			else if (parent.getSelection().size() > 0) {
				return parent.getSelection().subList(0, 1);
			}
			else {
				return Collections.emptyList();
			}
		}

		@Override
		public void count(
			final IResultCallback<Integer> resultCallback,
			final List<? extends IBeanKey> parentBeanKeys,
			final IFilter filter,
			final Void parameter,
			final IExecutionCallback executionCallback) {

			try {
				resultCallback.finished(getSelection().size());
			}
			catch (final Exception e) {
				resultCallback.exception(e);
			}

		}

	}
}
