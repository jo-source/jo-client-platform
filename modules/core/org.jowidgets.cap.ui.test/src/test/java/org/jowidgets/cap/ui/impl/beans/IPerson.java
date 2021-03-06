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
package org.jowidgets.cap.ui.impl.beans;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jowidgets.cap.common.api.annotation.BeanValidator;
import org.jowidgets.cap.common.api.annotation.PropertyValidator;
import org.jowidgets.cap.common.api.bean.IBean;

@BeanValidator({PersonNameWordCountValidator.class, PersonNameLastNameValidator.class})
public interface IPerson extends IBean {

	String NAME_PROPERTY = "name";
	String LAST_NAME_PROPERTY = "lastname";
	String GENDER_PROPERTY = "gender";

	List<String> ALL_PROPERTIES = Arrays.asList(NAME_PROPERTY, LAST_NAME_PROPERTY, GENDER_PROPERTY);

	@NotNull
	@Size(min = 2, max = 50)
	@PropertyValidator(PersonNameValidator.class)
	String getName();

	void setName(String name);

	@NotNull
	@Size(min = 2, max = 50)
	String getLastname();

	void setLastname(String name);

	@NotNull
	@Size(min = 1, max = 1)
	Gender getGender();

	void setGender(final Gender gender);

}
