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

package org.jowidgets.cap.sample1.service.entity;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.api.service.IDtoDescriptorService;
import org.jowidgets.cap.sample1.common.entity.IUser;

public class UserDtoDescriptorService implements IDtoDescriptorService {

	private final IBeanDtoDescriptor descriptor;

	public UserDtoDescriptorService() {
		final IBeanDtoDescriptorBuilder builder = CapCommonToolkit.dtoDescriptorBuilder(IUser.class);

		IBeanPropertyBluePrint propertyBp = builder.addProperty(IUser.NAME_PROPERTY);
		propertyBp.setLabel("Name").setDescription("The name of the user");
		propertyBp.setMandatory(true);

		propertyBp = builder.addProperty(IUser.LAST_NAME_PROPERTY);
		propertyBp.setLabel("Lastname").setDescription("The lastname of the user");
		propertyBp.setMandatory(true);

		propertyBp = builder.addProperty(IUser.GENDER_PROPERTY);
		propertyBp.setLabel("Gender").setDescription("The gender of the user");
		propertyBp.setValueRange("M", "F");

		propertyBp = builder.addProperty(IUser.DATE_OF_BIRTH_PROPERTY);
		propertyBp.setLabel("Birthday").setLabelLong("Date of Birth").setDescription("The users date of birth");

		propertyBp = builder.addProperty(IUser.AGE_PROPERTY);
		propertyBp.setLabel("Age").setDescription("The users age");

		propertyBp = builder.addProperty(IUser.COUNTRY_PROPERTY);
		propertyBp.setLabel("Country").setDescription("The country where the user lives");
		propertyBp.setValueRange(true, "Germany", "Italy", "Spain");

		propertyBp = builder.addProperty(IUser.LANGUAGES_PROPERTY);
		propertyBp.setLabel("Languages").setDescription("The languages the user speaks");
		propertyBp.setElementValueType(String.class).setSortable(false);

		propertyBp = builder.addProperty(IUser.ADMIN_PROPERTY);
		propertyBp.setLabel("Admin").setDescription("Determines if the user is an administrator");

		propertyBp = builder.addProperty(IUser.MARIED_PROPERTY);
		propertyBp.setLabel("Maried").setDescription("Determines if the user is maried");

		propertyBp = builder.addProperty(IBean.ID_PROPERTY);
		propertyBp.setLabel("Id").setDescription("The id of the user");

		propertyBp = builder.addProperty(IBean.VERSION_PROPERTY);
		propertyBp.setLabel("Version").setDescription("The version of the user record");

		descriptor = builder.build();
	}

	@Override
	public IBeanDtoDescriptor getDescriptor() {
		return descriptor;
	}

}
