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

package org.jowidgets.cap.sample1.ui.attribute;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IProperty;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.ui.converter.GenderConverterLong;
import org.jowidgets.cap.sample1.ui.converter.GenderConverterShort;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBluePrint;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeModifier;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProviderBuilder;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.service.api.ServiceProvider;

public class UserAttributesFactory {

	private final IAttributeToolkit attributeToolkit;

	public UserAttributesFactory() {
		this.attributeToolkit = CapUiToolkit.attributeToolkit();
	}

	public List<IAttribute<Object>> tableAttributes() {
		final List<IProperty> properties = createProperties();

		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();

		modifierBuilder.addDefaultEditableModifier(true);

		modifierBuilder.addModifier(IUser.GENDER_PROPERTY, new IAttributeModifier<Object>() {
			@Override
			public void modify(final IProperty source, final IAttributeBluePrint<Object> bluePrint) {
				IControlPanelProviderBuilder<Object> controlBuilder = attributeToolkit.createControlPanelProviderBuilder(
						source.getValueType(),
						source.getElementValueType(),
						source.getValueRange());

				controlBuilder.setConverter(new GenderConverterLong().getConverter());
				controlBuilder.setDisplayFormat(DisplayFormat.LONG);
				bluePrint.addControlPanel(controlBuilder.build());

				controlBuilder = attributeToolkit.createControlPanelProviderBuilder(
						source.getValueType(),
						source.getElementValueType(),
						source.getValueRange());

				controlBuilder.setConverter(new GenderConverterShort().getConverter());
				controlBuilder.setDisplayFormat(DisplayFormat.SHORT);
				bluePrint.addControlPanel(controlBuilder.build());

				bluePrint.setDisplayFormatId(DisplayFormat.SHORT.getId());
				bluePrint.setTableAlignment(AlignmentHorizontal.CENTER);
			}
		});

		modifierBuilder.addModifier(IUser.DATE_OF_BIRTH_PROPERTY).setLabelDisplayFormat(DisplayFormat.LONG);
		modifierBuilder.addModifier(IUser.ADMIN_PROPERTY).setTableAlignment(AlignmentHorizontal.CENTER);
		modifierBuilder.addModifier(IUser.MARIED_PROPERTY).setTableAlignment(AlignmentHorizontal.CENTER);
		modifierBuilder.addModifier(IUser.AGE_PROPERTY).setTableAlignment(AlignmentHorizontal.CENTER);

		return attributeToolkit.createAttributes(properties, modifierBuilder.build());
	}

	public List<IAttribute<Object>> formAttributes() {
		final List<IAttribute<Object>> attributes = tableAttributes();

		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addAcceptEditableAttributesFilter();

		modifierBuilder.addModifier(IUser.GENDER_PROPERTY).setDisplayFormatId(DisplayFormat.LONG.getId());

		return attributeToolkit.createAttributesCopy(attributes, modifierBuilder.build());
	}

	private List<IProperty> createProperties() {
		return ServiceProvider.getService(IEntityService.ID).getDescriptor(IUser.class).getProperties();
	}

}
