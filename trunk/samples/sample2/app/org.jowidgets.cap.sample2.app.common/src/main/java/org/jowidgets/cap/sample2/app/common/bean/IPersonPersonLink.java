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
package org.jowidgets.cap.sample2.app.common.bean;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.sample2.app.common.security.AuthKeys;
import org.jowidgets.cap.security.common.api.annotation.CreateAuthorization;
import org.jowidgets.cap.security.common.api.annotation.DeleteAuthorization;
import org.jowidgets.cap.security.common.api.annotation.ReadAuthorization;
import org.jowidgets.cap.security.common.api.annotation.UpdateAuthorization;

@CreateAuthorization(AuthKeys.CREATE_PERSON_PERSON_LINK)
@ReadAuthorization(AuthKeys.READ_PERSON_PERSON_LINK)
@UpdateAuthorization(AuthKeys.UPDATE_PERSON_PERSON_LINK)
@DeleteAuthorization(AuthKeys.DELETE_PERSON_PERSON_LINK)
public interface IPersonPersonLink extends IBean {

	String SOURCE_PERSON_ID_PROPERTY = "sourcePersonId";
	String DESTINATION_PERSON_ID_PROPERTY = "destinationPersonId";
	String RELATION_TYPE_ID_PROPERTY = "relationTypeId";
	String COMMENT_PROPERTY = "comment";
	String SOURCE_PERSON_NAME_PROPERTY = "sourcePersonName";
	String SOURCE_PERSON_LAST_NAME_PROPERTY = "sourcePersonLastname";
	String DESTINATION_PERSON_NAME_PROPERTY = "destinationPersonName";
	String DESTINATION_PERSON_LAST_NAME_PROPERTY = "destinationPersonLastname";

	List<String> SOURCE_PERSONS_OF_PERSONS_PROPERTIES = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(SOURCE_PERSON_ID_PROPERTY);
			add(DESTINATION_PERSON_ID_PROPERTY);
			add(RELATION_TYPE_ID_PROPERTY);
			add(COMMENT_PROPERTY);
			add(SOURCE_PERSON_NAME_PROPERTY);
			add(SOURCE_PERSON_LAST_NAME_PROPERTY);
			add(IBean.ID_PROPERTY);
			add(IBean.VERSION_PROPERTY);
		}
	};

	List<String> PERSONS_OF_SOURCE_PERSONS_PROPERTIES = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(SOURCE_PERSON_ID_PROPERTY);
			add(DESTINATION_PERSON_ID_PROPERTY);
			add(RELATION_TYPE_ID_PROPERTY);
			add(COMMENT_PROPERTY);
			add(DESTINATION_PERSON_NAME_PROPERTY);
			add(DESTINATION_PERSON_LAST_NAME_PROPERTY);
			add(IBean.ID_PROPERTY);
			add(IBean.VERSION_PROPERTY);
		}
	};

	List<String> ALL_PROPERTIES = new LinkedList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(SOURCE_PERSON_ID_PROPERTY);
			add(DESTINATION_PERSON_ID_PROPERTY);
			add(RELATION_TYPE_ID_PROPERTY);
			add(COMMENT_PROPERTY);
			add(SOURCE_PERSON_NAME_PROPERTY);
			add(SOURCE_PERSON_LAST_NAME_PROPERTY);
			add(DESTINATION_PERSON_NAME_PROPERTY);
			add(DESTINATION_PERSON_LAST_NAME_PROPERTY);
			add(IBean.ID_PROPERTY);
			add(IBean.VERSION_PROPERTY);
		}
	};

	Long getSourcePersonId();

	void setSourcePersonId(Long id);

	Long getDestinationPersonId();

	void setDestinationPersonId(Long id);

	Long getRelationTypeId();

	void setRelationTypeId(Long id);

	String getComment();

	void setComment(String comment);

	String getSourcePersonName();

	void setSourcePersonName(String name);

	String getSourcePersonLastname();

	void setSourcePersonLastname(String name);

	String getDestinationPersonName();

	void setDestinationPersonName(String name);

	String getDestinationPersonLastname();

	void setDestinationPersonLastname(String name);
}
