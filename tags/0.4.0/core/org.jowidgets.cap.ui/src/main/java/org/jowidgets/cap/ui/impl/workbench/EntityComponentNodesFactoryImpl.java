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

package org.jowidgets.cap.ui.impl.workbench;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityClass;
import org.jowidgets.cap.common.api.entity.IEntityClassBuilder;
import org.jowidgets.cap.common.api.service.IEntityClassProviderService;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.plugin.IEntityComponentNodesFactoryPlugin;
import org.jowidgets.cap.ui.api.workbench.IEntityComponentNodesFactory;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModelBuilder;
import org.jowidgets.workbench.tools.ComponentNodeModelBuilder;

final class EntityComponentNodesFactoryImpl implements IEntityComponentNodesFactory {

	EntityComponentNodesFactoryImpl() {}

	@Override
	public List<IComponentNodeModel> createNodes() {
		return createNodes(IEntityClassProviderService.ID);
	}

	@Override
	public List<IComponentNodeModel> createNodes(final IServiceId<IEntityClassProviderService> id) {
		return createNodes(ServiceProvider.getService(id));
	}

	@Override
	public List<IComponentNodeModel> createNodes(final IEntityClassProviderService entityClassProviderService) {
		Assert.paramNotNull(entityClassProviderService, "entityClassProviderService");

		final List<IComponentNodeModel> result = new LinkedList<IComponentNodeModel>();

		List<IEntityClass> entities = entityClassProviderService.getEntities();
		for (final IEntityComponentNodesFactoryPlugin plugin : PluginProvider.getPlugins(IEntityComponentNodesFactoryPlugin.ID)) {
			entities = plugin.modify(entities);
		}
		for (final IEntityClass entityClass : entities) {
			result.add(createNodeFromEntity(entityClass));
		}
		return result;
	}

	@Override
	public IComponentNodeModel createNode(final IEntityClass entityClass) {
		Assert.paramNotNull(entityClass, "entityClass");
		return createNodeFromEntity(entityClass);
	}

	@Override
	public IComponentNodeModel createNode(final Object entityId) {
		Assert.paramNotNull(entityId, "entityId");
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanDtoDescriptor beanDtoDescriptor = entityService.getDescriptor(entityId);
			if (beanDtoDescriptor != null) {
				final String labelPlural = beanDtoDescriptor.getLabelPlural();
				if (!EmptyCheck.isEmpty(labelPlural)) {
					final IEntityClassBuilder entityClassBuilder = CapCommonToolkit.entityClassBuilder();
					entityClassBuilder.setId(entityId).setLabel(labelPlural).setDescription(beanDtoDescriptor.getDescription());
					return createNodeFromEntity(entityClassBuilder.build());
				}
				else {
					throw new IllegalArgumentException("The was no label plural found for the entityId '" + entityId + "'");
				}
			}
			else {
				throw new IllegalArgumentException("The was no bean dto descriptor found for the entityId '" + entityId + "'");
			}
		}
		else {
			throw new IllegalStateException("There was no entity service found");
		}
	}

	private IComponentNodeModel createNodeFromEntity(final IEntityClass entityClass) {
		final IComponentNodeModelBuilder builder = new ComponentNodeModelBuilder();
		//TODO MG the id of workbench parts should be an object
		builder.setId(entityClass.getId().toString());
		builder.setLabel(entityClass.getLabel());
		builder.setTooltip(entityClass.getDescription());
		builder.setComponentFactory(CapUiToolkit.workbenchToolkit().entityComponentFactory().create(entityClass));
		for (final IEntityClass childEntityClass : entityClass.getSubClasses()) {
			builder.addChild(createNodeFromEntity(childEntityClass));
		}
		return builder.build();
	}

}
