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

package org.jowidgets.cap.security.ui.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.security.common.api.CrudServiceType;
import org.jowidgets.cap.security.common.api.IAuthorizationChecker;
import org.jowidgets.cap.security.common.api.ICrudAuthorizationMapper;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.plugin.api.IPluginProperties;

final class SecureBeanFormPluginImpl<AUTHORIZATION_TYPE> implements IBeanFormPlugin {

	private final List<ICrudAuthorizationMapper<AUTHORIZATION_TYPE>> mappers;
	private final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker;

	SecureBeanFormPluginImpl(
		final List<ICrudAuthorizationMapper<AUTHORIZATION_TYPE>> mappers,
		final IAuthorizationChecker<AUTHORIZATION_TYPE> authorizationChecker) {
		this.mappers = new LinkedList<ICrudAuthorizationMapper<AUTHORIZATION_TYPE>>(mappers);
		this.authorizationChecker = authorizationChecker;
	}

	@Override
	public void modifySetup(final IPluginProperties properties, final IBeanFormBluePrint<?> bluePrint) {
		if (!isAuthorized(properties, CrudServiceType.CREATE)) {
			bluePrint.setCreateModeAttributes(createReadonlyAttributes(bluePrint.getCreateModeAttributes()));
		}
		if (!isAuthorized(properties, CrudServiceType.UPDATE)) {
			bluePrint.setEditModeAttributes(createReadonlyAttributes(bluePrint.getEditModeAttributes()));
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean isAuthorized(final IPluginProperties properties, final CrudServiceType crudServiceType) {
		final Class beanType = properties.getValue(IBeanFormPlugin.BEAN_TYPE_PROPERTY_KEY);
		final Object entityId = properties.getValue(IBeanFormPlugin.ENTITIY_ID_PROPERTY_KEY);
		return isAuthorized(beanType, entityId, crudServiceType);
	}

	private boolean isAuthorized(
		final Class<? extends IBean> beanType,
		final Object entityId,
		final CrudServiceType crudServiceType) {
		final AUTHORIZATION_TYPE authorization = getAuthorization(beanType, entityId, crudServiceType);
		if (authorization != null) {
			return authorizationChecker.hasAuthorization(authorization);
		}
		else {
			return true;
		}
	}

	private AUTHORIZATION_TYPE getAuthorization(
		final Class<? extends IBean> beanType,
		final Object entityId,
		final CrudServiceType crudServiceType) {
		for (final ICrudAuthorizationMapper<AUTHORIZATION_TYPE> mapper : mappers) {
			final AUTHORIZATION_TYPE authorization = mapper.getAuthorization(beanType, entityId, crudServiceType);
			if (authorization != null) {
				return authorization;
			}
		}
		return null;
	}

	private static List<IAttribute<Object>> createReadonlyAttributes(final Collection<? extends IAttribute<?>> attributes) {
		final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();
		final IAttributeCollectionModifierBuilder modifierBuilder = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifierBuilder.addDefaultModifier().setEditable(false);
		return CapUiToolkit.attributeToolkit().createAttributesCopy(attributes, modifierBuilder.build());
	}

}
