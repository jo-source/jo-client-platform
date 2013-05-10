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

import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.security.common.api.ISecureObject;
import org.jowidgets.cap.security.ui.api.ISecureControlMapper;
import org.jowidgets.cap.security.ui.api.SecurityIcons;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.wrapper.WrapperUtil;

final class SecureBeanTableMapper<BEAN_TYPE, AUTHORIZATION_TYPE> implements
		ISecureControlMapper<IBeanTable<BEAN_TYPE>, IBeanTableBluePrint<BEAN_TYPE>, AUTHORIZATION_TYPE> {

	private static final IMessage AUTHORIZATION_FAILED = Messages.getMessage("SecureBeanTableAuthorizationMapper.authorizationFailed");
	private static final IMessage AUTHORIZATION_FAILED_WITH_ENTITY = Messages.getMessage("SecureBeanTableAuthorizationMapper.authorizationFailedWithEntity");

	@SuppressWarnings("unchecked")
	@Override
	public AUTHORIZATION_TYPE getAuthorization(final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		Assert.paramNotNull(bluePrint, "bluePrint");
		final IBeanTableModel<BEAN_TYPE> model = bluePrint.getModel();
		if (model != null) {
			final IReaderService<Object> readerService = model.getReaderService();
			final ISecureObject<?> secureObject = WrapperUtil.tryToCast(readerService, ISecureObject.class);
			if (secureObject != null) {
				return (AUTHORIZATION_TYPE) secureObject.getAuthorization();
			}
		}
		return null;
	}

	@Override
	public String getAuthorizationFailedText(final IBeanTableBluePrint<BEAN_TYPE> bluePrint, final IBeanTable<BEAN_TYPE> widget) {
		Assert.paramNotNull(bluePrint, "bluePrint");
		final IBeanTableModel<BEAN_TYPE> model = bluePrint.getModel();
		if (model != null) {
			final String entityLabelPlural = model.getEntityLabelPlural();
			if (!EmptyCheck.isEmpty(entityLabelPlural)) {
				return MessageReplacer.replace(AUTHORIZATION_FAILED_WITH_ENTITY.get(), entityLabelPlural);
			}
		}
		return AUTHORIZATION_FAILED.get();
	}

	@Override
	public IImageConstant getAuthorizationFailedIcon(
		final IBeanTableBluePrint<BEAN_TYPE> bluePrint,
		final IBeanTable<BEAN_TYPE> widget) {
		return SecurityIcons.READ_NOT_ALLOWED_BIG;
	}

	@Override
	public Class<?> getWidgetType() {
		return IBeanTable.class;
	}

}
