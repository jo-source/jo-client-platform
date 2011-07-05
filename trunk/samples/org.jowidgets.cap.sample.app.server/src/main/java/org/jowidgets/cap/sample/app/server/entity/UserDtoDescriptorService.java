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

package org.jowidgets.cap.sample.app.server.entity;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBuilder;
import org.jowidgets.cap.common.api.service.IDtoDescriptorService;
import org.jowidgets.cap.sample.app.common.entity.IUser;

public class UserDtoDescriptorService implements IDtoDescriptorService<IUser> {

	private final IBeanDtoDescriptor<IUser> descriptor;

	public UserDtoDescriptorService() {
		final IBeanDtoDescriptorBuilder<IUser> descrBuilder = CapCommonToolkit.dtoDescriptorBuilder(IUser.class);

		IBeanPropertyBuilder propBuilder = descrBuilder.propertyBuilder(IUser.NAME_PROPERTY);
		propBuilder.setLabel("Name").setDescription("The name of the user");
		propBuilder.setMandatory(true);
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.LAST_NAME_PROPERTY);
		propBuilder.setLabel("Lastname").setDescription("The lastname of the user");
		propBuilder.setMandatory(true);
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.GENDER_PROPERTY);
		propBuilder.setLabel("Gender").setDescription("The gender of the user");
		propBuilder.setValueRange("M", "F");
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.ADMIN_PROPERTY);
		propBuilder.setLabel("Admin").setDescription("Determines if the user is an administrator");
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.DATE_OF_BIRTH_PROPERTY);
		propBuilder.setLabel("Date of Birth").setDescription("The users date of birth");
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.AGE_PROPERTY);
		propBuilder.setLabel("Age").setDescription("The users age");
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.LANGUAGES_PROPERTY);
		propBuilder.setLabel("Languages").setDescription("The languages the user speaks");
		propBuilder.setElementValueType(String.class).setSortable(false);
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IUser.MARIED_PROPERTY);
		propBuilder.setLabel("Maried").setDescription("Determines if the user is maried");
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IBean.ID_PROPERTY);
		propBuilder.setLabel("Id").setDescription("The id of the user");
		descrBuilder.addProperty(propBuilder);

		propBuilder = descrBuilder.propertyBuilder(IBean.VERSION_PROPERTY);
		propBuilder.setLabel("Version").setDescription("The version of the user record");
		descrBuilder.addProperty(propBuilder);

		descriptor = descrBuilder.build();
	}

	@Override
	public IBeanDtoDescriptor<IUser> getDescriptor() {
		return descriptor;
	}

}
