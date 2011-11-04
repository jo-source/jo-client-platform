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
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.EmptyCheck;

final class LinkActionBuilderFactory {

	private LinkActionBuilderFactory() {}

	static ILinkActionBuilder createLinkActionBuilder(final IEntityLinkDescriptor link, final ILinkActionBuilder builder) {
		return createLinkActionBuilder(ServiceProvider.getService(IEntityService.ID), link, builder);
	}

	static ILinkActionBuilder createLinkActionBuilder(
		final IEntityService entityService,
		final IEntityLinkDescriptor link,
		final ILinkActionBuilder builder) {
		if (entityService != null && link != null && link.getLinkedTypeId() != null && link.getLinkableTypeId() != null) {

			final IBeanServicesProvider linkTypeServices = entityService.getBeanServices(link.getLinkTypeId());
			final IBeanDtoDescriptor linkableTypeDescriptor = entityService.getDescriptor(link.getLinkableTypeId());
			final IBeanServicesProvider linkableTypeServices = entityService.getBeanServices(link.getLinkableTypeId());

			if (isBeanLinkableDescriptorOk(linkableTypeDescriptor)
				&& isLinkServicesOk(linkTypeServices)
				&& isLinkableServicesOk(linkableTypeServices)) {
				return createLinkActionBuilder(link, linkTypeServices, linkableTypeDescriptor, linkableTypeServices, builder);
			}

		}
		return null;
	}

	private static boolean isBeanLinkableDescriptorOk(final IBeanDtoDescriptor descriptor) {
		return descriptor != null
			&& !EmptyCheck.isEmpty(descriptor.getLabel())
			&& !EmptyCheck.isEmpty(descriptor.getProperties());
	}

	private static boolean isLinkServicesOk(final IBeanServicesProvider services) {
		return services != null && services.creatorService() != null;
	}

	private static boolean isLinkableServicesOk(final IBeanServicesProvider services) {
		return services != null && services.readerService() != null;
	}

	private static ILinkActionBuilder createLinkActionBuilder(
		final IEntityLinkDescriptor link,
		final IBeanServicesProvider linkTypeServices,
		final IBeanDtoDescriptor linkableTypeDescriptor,
		final IBeanServicesProvider linkableTypeServices,
		final ILinkActionBuilder builder) {

		builder.setText(linkableTypeDescriptor.getLabel() + " verknüpfen ...");

		builder.setLinkCreatorService(linkTypeServices.creatorService());

		builder.setLinkableTableReaderService(linkableTypeServices.readerService());
		builder.setLinkableTableLabel(linkableTypeDescriptor.getLabel());
		builder.setLinkableTableAttributes(createAttributes(linkableTypeDescriptor));

		builder.setSourceProperties(link.getSourceProperties());
		builder.setDestinationProperties(link.getDestinationProperties());

		return builder;
	}

	private static List<IAttribute<Object>> createAttributes(final IBeanDtoDescriptor beanDtoDescriptor) {
		return CapUiToolkit.attributeToolkit().createAttributes(beanDtoDescriptor.getProperties());
	}
}
