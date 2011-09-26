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

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.tools.bean.BeanDtoDescriptorBuilder;
import org.jowidgets.cap.sample1.common.entity.IUser;

public class UserDtoDescriptorBuilder extends BeanDtoDescriptorBuilder {

	public UserDtoDescriptorBuilder() {
		super(IUser.class);

		IBeanPropertyBluePrint propertyBp = addProperty(IUser.NAME_PROPERTY);
		propertyBp.setLabel("Name").setDescription("The name of the user");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IUser.LAST_NAME_PROPERTY);
		propertyBp.setLabel("Lastname").setDescription("The lastname of the user");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IUser.GENDER_PROPERTY);
		propertyBp.setLabel("Gender").setLabelLong("Person gender").setDescription("The gender of the user");
		propertyBp.setValueRange("M", "F");

		propertyBp = addProperty(IUser.DATE_OF_BIRTH_PROPERTY);
		propertyBp.setLabel("Birthday").setLabelLong("Date of Birth").setDescription("The user's date of birth");

		propertyBp = addProperty(IUser.AGE_PROPERTY);
		propertyBp.setLabel("Age").setDescription("The user's age");

		propertyBp = addProperty(IUser.HEIGHT_PROPERTY);
		propertyBp.setLabel("Height").setLabelLong("Height (cm)").setDescription("The users body height in cm");

		propertyBp = addProperty(IUser.WEIGHT_PROPERTY);
		propertyBp.setLabel("Weight").setLabelLong("Weight (kg)").setDescription("The users body weight in kg");

		propertyBp = addProperty(IUser.BMI_PROPERTY);
		propertyBp.setLabel("BMI").setDescription("The users body mass index (BMI)");

		propertyBp = addProperty(IUser.COUNTRY_PROPERTY);
		propertyBp.setLabel("Country").setDescription("The country where the user lives");
		propertyBp.setValueRange(true, "Germany", "Italy", "Spain");

		propertyBp = addProperty(IUser.LANGUAGES_PROPERTY);
		propertyBp.setLabel("Languages").setDescription("The languages the user speaks");
		propertyBp.setElementValueType(String.class).setSortable(false);
		propertyBp.setValueRange(true, "English", "German", "Italian", "Spanish", "French");

		propertyBp = addProperty(IUser.ADMIN_PROPERTY);
		propertyBp.setLabel("Admin").setDescription("Determines if the user is an administrator");

		propertyBp = addProperty(IUser.MARRIED_PROPERTY);
		propertyBp.setLabel("Married").setDescription("Determines if the user is married");

		propertyBp = addProperty(IBean.ID_PROPERTY);
		propertyBp.setLabel("Id").setDescription("The id of the user");

		propertyBp = addProperty(IBean.VERSION_PROPERTY);
		propertyBp.setLabel("Version").setDescription("The version of the user record");

	}

}
