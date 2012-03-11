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

package org.jowidgets.cap.sample2.app.service.descriptor;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.tools.bean.BeanDtoDescriptorBuilder;
import org.jowidgets.cap.sample2.app.common.bean.IPersonRelationType;

public class PersonRelationTypeDtoDescriptorBuilder extends BeanDtoDescriptorBuilder {

	public PersonRelationTypeDtoDescriptorBuilder() {
		super(IPersonRelationType.class);

		setLabelSingular("Person relation type");
		setLabelPlural("Person relation types");

		IBeanPropertyBluePrint propertyBp;

		propertyBp = addProperty(IBean.ID_PROPERTY);
		propertyBp.setLabel("Id");
		propertyBp.setDescription("The relation types technical identifier");

		propertyBp = addProperty(IPersonRelationType.SOURCE_NAME_PROPERTY);
		propertyBp.setLabel("Source name");
		propertyBp.setDescription("The relation types name considered from from the source side");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IPersonRelationType.SOURCE_DESCRIPTION_PROPERTY);
		propertyBp.setLabel("Source description");
		propertyBp.setDescription("The relation types description considered from from the source side");

		propertyBp = addProperty(IPersonRelationType.DESTINATION_NAME_PROPERTY);
		propertyBp.setLabel("Destination name");
		propertyBp.setDescription("The relation types name considered from from the destination side");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IPersonRelationType.DESTINATION_DESCRIPTION_PROPERTY);
		propertyBp.setLabel("Destination description");
		propertyBp.setDescription("The relation types description considered from from the destination side");

		propertyBp = addProperty(IBean.VERSION_PROPERTY);
		propertyBp.setLabel("Version");
		propertyBp.setDescription("The version of the dataset");
	}
}
