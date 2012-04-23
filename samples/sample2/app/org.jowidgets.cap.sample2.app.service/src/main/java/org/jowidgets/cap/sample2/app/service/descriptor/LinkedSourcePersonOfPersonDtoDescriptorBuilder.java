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
import org.jowidgets.cap.sample2.app.common.bean.IPersonPersonLink;
import org.jowidgets.cap.sample2.app.common.lookup.LookUpIds;

public class LinkedSourcePersonOfPersonDtoDescriptorBuilder extends BeanDtoDescriptorBuilder {

	public LinkedSourcePersonOfPersonDtoDescriptorBuilder() {
		super(IPersonPersonLink.class);

		setLabelSingular("Related person (backward)");
		setLabelPlural("Related persons (backward)");

		IBeanPropertyBluePrint propertyBp;

		propertyBp = addProperty(IBean.ID_PROPERTY);
		propertyBp.setLabel("Id");
		propertyBp.setDescription("The relations technical identifier");
		propertyBp.setVisible(false);

		propertyBp = addProperty(IPersonPersonLink.RELATION_TYPE_ID_PROPERTY);
		propertyBp.setLabel("Relation type");
		propertyBp.setDescription("The relations type");
		propertyBp.setLookUpValueRange(LookUpIds.SOURCE_PERSON_OF_PERSON_RELATION_TYPE);
		propertyBp.setEditable(false);
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IPersonPersonLink.SOURCE_PERSON_NAME_PROPERTY);
		propertyBp.setLabel("Name");
		propertyBp.setDescription("The users name");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IPersonPersonLink.SOURCE_PERSON_LAST_NAME_PROPERTY);
		propertyBp.setLabel("Lastname");
		propertyBp.setDescription("The users lastname");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IBean.VERSION_PROPERTY);
		propertyBp.setLabel("Version");
		propertyBp.setDescription("The version of the dataset");
		propertyBp.setVisible(false);

	}
}
