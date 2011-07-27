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

package org.jowidgets.cap.sample1.common.entity;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jowidgets.cap.common.api.bean.IBean;

public interface IUser extends IBean {

	String NAME_PROPERTY = "name";
	String LAST_NAME_PROPERTY = "lastName";
	String DATE_OF_BIRTH_PROPERTY = "dateOfBirth";
	String AGE_PROPERTY = "age";
	String GENDER_PROPERTY = "gender";
	String COUNTRY_PROPERTY = "country";
	String LANGUAGES_PROPERTY = "languages";
	String ADMIN_PROPERTY = "admin";
	String MARRIED_PROPERTY = "married";

	List<String> ALL_PROPERTIES = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(NAME_PROPERTY);
			add(LAST_NAME_PROPERTY);
			add(DATE_OF_BIRTH_PROPERTY);
			add(AGE_PROPERTY);
			add(GENDER_PROPERTY);
			add(ADMIN_PROPERTY);
			add(COUNTRY_PROPERTY);
			add(LANGUAGES_PROPERTY);
			add(MARRIED_PROPERTY);
			add(IBean.ID_PROPERTY);
			add(IBean.VERSION_PROPERTY);
		}
	};

	@NotNull
	@Size(min = 1, max = 50)
	String getName();

	void setName(final String name);

	@NotNull
	@Size(min = 1, max = 50)
	String getLastName();

	void setLastName(final String lastName);

	Date getDateOfBirth();

	Integer getAge();

	void setDateOfBirth(final Date dateOfBirth);

	String getGender();

	void setGender(final String gender);

	String getCountry();

	void setCountry(String country);

	List<String> getLanguages();

	void setLanguages(List<String> languages);

	@NotNull
	boolean getAdmin();

	void setAdmin(boolean admin);

	Boolean getMarried();

	void setMarried(Boolean married);

}
