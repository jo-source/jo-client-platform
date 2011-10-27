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
import org.jowidgets.cap.sample1.service.lookup.Countries;
import org.jowidgets.cap.sample1.service.lookup.Languages;
import org.jowidgets.cap.sample1.service.lookup.RolesLookUpService;

public class UserDtoDescriptorBuilder extends BeanDtoDescriptorBuilder {

	public UserDtoDescriptorBuilder() {
		super(IUser.class);

		setLabel(Messages.getString("UserDtoDescriptorBuilder.label"));

		IBeanPropertyBluePrint propertyBp = addProperty(IUser.NAME_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.name")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.name_description")); //$NON-NLS-1$
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IUser.LAST_NAME_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.lastname")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.lastname_description")); //$NON-NLS-1$
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IUser.GENDER_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.gender")); //$NON-NLS-1$
		propertyBp.setLabelLong(Messages.getString("UserDtoDescriptorBuilder.gender_long")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.gender_description")); //$NON-NLS-1$
		propertyBp.setValueRange("M", "F"); //$NON-NLS-1$ //$NON-NLS-2$
		propertyBp.setMandatory(true);

		propertyBp = addProperty(IUser.DATE_OF_BIRTH_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.birthday")); //$NON-NLS-1$
		propertyBp.setLabelLong(Messages.getString("UserDtoDescriptorBuilder.birthday_long")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.birthday_description")); //$NON-NLS-1$

		propertyBp = addProperty(IUser.AGE_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.age")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.age_description")); //$NON-NLS-1$

		propertyBp = addProperty(IUser.HEIGHT_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.height")); //$NON-NLS-1$
		propertyBp.setLabelLong(Messages.getString("UserDtoDescriptorBuilder.height_long")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.height_description")); //$NON-NLS-1$

		propertyBp = addProperty(IUser.WEIGHT_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.weight")); //$NON-NLS-1$
		propertyBp.setLabelLong(Messages.getString("UserDtoDescriptorBuilder.weight_long")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.weight_description")); //$NON-NLS-1$

		propertyBp = addProperty(IUser.BMI_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.bmi")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.bmi_description")); //$NON-NLS-1$

		propertyBp = addProperty(IUser.COUNTRY_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.country")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.country_description")); //$NON-NLS-1$
		propertyBp.setLookUpValueRange(Countries.LOOK_UP_ID);
		propertyBp.setDefaultValue(Countries.EMPTY);

		propertyBp = addProperty(IUser.LANGUAGES_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.languages")).setDescription(Messages.getString("UserDtoDescriptorBuilder.languages_description")); //$NON-NLS-1$ //$NON-NLS-2$
		propertyBp.setElementValueType(Integer.class).setSortable(false);
		propertyBp.setLookUpValueRange(Languages.LOOK_UP_ID);

		propertyBp = addProperty(IUser.ADMIN_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.admin")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.admin_description")); //$NON-NLS-1$

		propertyBp = addProperty(IUser.ROLES_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.roles")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.roles_description")); //$NON-NLS-1$
		propertyBp.setLookUpValueRange(RolesLookUpService.LOOK_UP_ID);

		propertyBp = addProperty(IUser.MARRIED_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.married")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.married_description")); //$NON-NLS-1$

		propertyBp = addProperty(IBean.ID_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.id")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.id_description")); //$NON-NLS-1$

		propertyBp = addProperty(IBean.VERSION_PROPERTY);
		propertyBp.setLabel(Messages.getString("UserDtoDescriptorBuilder.version")); //$NON-NLS-1$
		propertyBp.setDescription(Messages.getString("UserDtoDescriptorBuilder.version_description")); //$NON-NLS-1$

	}

}
