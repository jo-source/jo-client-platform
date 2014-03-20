/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.cap.ui.api.command;

import java.util.List;

import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.IFactory;

public interface IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		ICapActionBuilder<IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> {

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSource(
		IBeanSelectionProvider<SOURCE_BEAN_TYPE> selectionProvider);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceMultiSelection(boolean multiSelection);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceModificationPolicy(
		BeanModificationStatePolicy policy);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceMessageStatePolicy(
		BeanMessageStatePolicy policy);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addSourceExecutableChecker(
		IExecutableChecker<SOURCE_BEAN_TYPE> executableChecker);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setSourceSelectionAutoRefresh(
		boolean autoRefresh);

	/**
	 * Sets the entity label plural.
	 * This will set a proper text with the entity label as a variable
	 * 
	 * @param label The label to set
	 * 
	 * @return This builder
	 */
	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkedEntityLabel(String label);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkDefaultFactory(
		IFactory<IBeanProxy<LINK_BEAN_TYPE>> defaultFactory);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkBeanTypeId(Object beanTypeId);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkBeanType(
		Class<? extends LINK_BEAN_TYPE> beanType);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkCreatorService(
		ILinkCreatorService creatorService);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableBeanTypeId(Object beanTypeId);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkableBeanType(
		Class<? extends LINKABLE_BEAN_TYPE> beanType);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addEnabledChecker(IEnabledChecker enabledChecker);

	/**
	 * Sets the server based enabled checking. If set to true, the default enabled checker will use the link creator service
	 * to check the enabled state.
	 * 
	 * @param serviceBasedEnabledChecking
	 * @return this builder
	 */
	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setServiceBasedEnabledChecking(
		boolean serviceBasedEnabledChecking);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setExceptionConverter(
		IBeanExceptionConverter exceptionConverter);

	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> addExecutionInterceptor(
		IExecutionInterceptor<List<IBeanDto>> interceptor);

	/**
	 * If a linked model is set, the linked beans will be inserted in this model after link creation
	 * 
	 * @param model The model to set
	 * 
	 * @return This builder
	 */
	IPasteLinkActionBuilder<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> setLinkedModel(
		IBeanListModel<LINKABLE_BEAN_TYPE> model);

}
