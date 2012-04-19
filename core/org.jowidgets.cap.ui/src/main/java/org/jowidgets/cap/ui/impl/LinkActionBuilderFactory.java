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

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.command.ILinkActionBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.EmptyCheck;

final class LinkActionBuilderFactory {

	private LinkActionBuilderFactory() {}

	static <BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> createLinkActionBuilder(
		final IEntityLinkDescriptor link,
		final ILinkActionBuilder<BEAN_TYPE> builder) {
		return createLinkActionBuilder(ServiceProvider.getService(IEntityService.ID), link, builder);
	}

	static <BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> createLinkActionBuilder(
		final IBeanTableModel<BEAN_TYPE> model,
		final IBeanTableModel<?> linkedModel) {
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final List<IEntityLinkDescriptor> links = entityService.getEntityLinks(model.getEntityId());
			if (links != null) {
				for (final IEntityLinkDescriptor link : links) {
					if (link.getLinkedEntityId() == linkedModel.getEntityId()) {
						final ILinkActionBuilder<BEAN_TYPE> result;
						result = createLinkActionBuilder(link, new LinkActionBuilderImpl<BEAN_TYPE>(model));
						if (result != null) {
							result.setLinkedDataModel(linkedModel);
						}
						return result;
					}
				}
			}
		}
		return null;
	}

	static <BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> createLinkActionBuilder(
		final IEntityService entityService,
		final IEntityLinkDescriptor link,
		final ILinkActionBuilder<BEAN_TYPE> builder) {
		if (entityService != null && link != null && link.getLinkedEntityId() != null && link.getLinkableEntityId() != null) {

			final IBeanDtoDescriptor linkTypeDescriptor = entityService.getDescriptor(link.getLinkEntityId());
			final IBeanServicesProvider linkTypeServices = entityService.getBeanServices(link.getLinkEntityId());
			final IBeanDtoDescriptor linkableTypeDescriptor = entityService.getDescriptor(link.getLinkableEntityId());
			final IBeanServicesProvider linkableTypeServices = entityService.getBeanServices(link.getLinkableEntityId());

			if (isBeanLinkableDescriptorOk(linkableTypeDescriptor)
				&& isLinkServicesOk(linkTypeServices)
				&& isLinkableServicesOk(linkableTypeServices)) {
				return createLinkActionBuilder(
						link,
						linkTypeServices,
						linkTypeDescriptor,
						linkableTypeDescriptor,
						linkableTypeServices,
						builder);
			}

		}
		return null;
	}

	private static boolean isBeanLinkableDescriptorOk(final IBeanDtoDescriptor descriptor) {
		return descriptor != null
			&& !EmptyCheck.isEmpty(descriptor.getLabelPlural())
			&& !EmptyCheck.isEmpty(descriptor.getProperties());
	}

	private static boolean isLinkServicesOk(final IBeanServicesProvider services) {
		return services != null && services.creatorService() != null;
	}

	private static boolean isLinkableServicesOk(final IBeanServicesProvider services) {
		return services != null && services.readerService() != null;
	}

	private static <BEAN_TYPE> ILinkActionBuilder<BEAN_TYPE> createLinkActionBuilder(
		final IEntityLinkDescriptor link,
		final IBeanServicesProvider linkTypeServices,
		final IBeanDtoDescriptor linkTypeDescriptor,
		final IBeanDtoDescriptor linkableTypeDescriptor,
		final IBeanServicesProvider linkableTypeServices,
		final ILinkActionBuilder<BEAN_TYPE> builder) {

		builder.setDestinationEntityLabelPlural(linkableTypeDescriptor.getLabelPlural());

		builder.setLinkCreatorService(linkTypeServices.creatorService());
		if (linkTypeDescriptor != null) {
			builder.setLinkAttributes(createAttributes(linkTypeDescriptor));
		}

		builder.setLinkableTableReaderService(linkableTypeServices.readerService());
		builder.setLinkableTableEntityId(link.getLinkableEntityId());
		builder.setLinkableTableLabel(linkableTypeDescriptor.getLabelPlural());
		builder.setLinkableTableAttributes(createAttributes(linkableTypeDescriptor));

		builder.setSourceProperties(link.getSourceProperties());
		builder.setDestinationProperties(link.getDestinationProperties());

		return builder;
	}

	private static List<IAttribute<Object>> createAttributes(final IBeanDtoDescriptor beanDtoDescriptor) {
		return CapUiToolkit.attributeToolkit().createAttributes(beanDtoDescriptor.getProperties());
	}
}
