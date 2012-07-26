/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.security.common.impl;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.security.common.api.CrudServiceType;
import org.jowidgets.cap.security.common.api.ICrudAuthorizationMapper;
import org.jowidgets.cap.security.common.api.annotation.CreateAuthorization;
import org.jowidgets.cap.security.common.api.annotation.CrudAuthorizations;
import org.jowidgets.cap.security.common.api.annotation.DeleteAuthorization;
import org.jowidgets.cap.security.common.api.annotation.ReadAuthorization;
import org.jowidgets.cap.security.common.api.annotation.UpdateAuthorization;
import org.jowidgets.util.reflection.AnnotationCache;

final class BeanTypeAnnotationAuthorizationMapper implements ICrudAuthorizationMapper<String> {

	BeanTypeAnnotationAuthorizationMapper() {}

	@Override
	public String getAuthorization(final Class<? extends IBean> beanType, final Object entityId, final CrudServiceType serviceType) {

		if (beanType != null) {
			final CrudAuthorizations crudAuthorizations;
			crudAuthorizations = AnnotationCache.getTypeAnnotationFromHierarchy(beanType, CrudAuthorizations.class);

			if (CrudServiceType.CREATE == serviceType) {
				final CreateAuthorization authorization;
				authorization = AnnotationCache.getTypeAnnotationFromHierarchy(beanType, CreateAuthorization.class);
				if (authorization != null) {
					return authorization.value();
				}
				else if (crudAuthorizations != null) {
					return crudAuthorizations.create();
				}
			}
			else if (CrudServiceType.READ == serviceType) {
				final ReadAuthorization authorization;
				authorization = AnnotationCache.getTypeAnnotationFromHierarchy(beanType, ReadAuthorization.class);
				if (authorization != null) {
					return authorization.value();
				}
				else if (crudAuthorizations != null) {
					return crudAuthorizations.read();
				}
			}
			else if (CrudServiceType.UPDATE == serviceType) {
				final UpdateAuthorization authorization;
				authorization = AnnotationCache.getTypeAnnotationFromHierarchy(beanType, UpdateAuthorization.class);
				if (authorization != null) {
					return authorization.value();
				}
				else if (crudAuthorizations != null) {
					return crudAuthorizations.update();
				}
			}
			else if (CrudServiceType.DELETE == serviceType) {
				final DeleteAuthorization authorization;
				authorization = AnnotationCache.getTypeAnnotationFromHierarchy(beanType, DeleteAuthorization.class);
				if (authorization != null) {
					return authorization.value();
				}
				else if (crudAuthorizations != null) {
					return crudAuthorizations.delete();
				}
			}

		}

		return null;
	}

}
