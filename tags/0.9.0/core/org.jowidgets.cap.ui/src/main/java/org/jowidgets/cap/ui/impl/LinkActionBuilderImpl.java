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

package org.jowidgets.cap.ui.impl;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.entity.IEntityLinkPropertiesBuilder;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.command.ILinkActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class LinkActionBuilderImpl<BEAN_TYPE> extends AbstractCapActionBuilderImpl<ILinkActionBuilder<BEAN_TYPE>> implements
		ILinkActionBuilder<BEAN_TYPE> {

	private final IBeanListModel<BEAN_TYPE> model;

	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutableChecker<BEAN_TYPE>> executableCheckers;
	private final List<IExecutionInterceptor> executionInterceptors;

	private String destinationEntityLabel;
	private ICreatorService linkCreatorService;
	private List<IAttribute<Object>> linkAttributes;
	private IReaderService<Void> linkableReaderService;
	private Object linkableTableEntityId;
	private List<IAttribute<Object>> linkableTableAttributes;
	private String linkableTableLabel;
	private IEntityLinkProperties sourceLinkProperties;
	private IEntityLinkProperties destinationLinkProperties;
	private boolean multiSelection;
	private BeanModificationStatePolicy beanModificationStatePolicy;
	private BeanMessageStatePolicy beanMessageStatePolicy;
	private IBeanExceptionConverter exceptionConverter;
	private IDataModel linkedDataModel;

	LinkActionBuilderImpl(final IBeanListModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		this.model = model;
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executableCheckers = new LinkedList<IExecutableChecker<BEAN_TYPE>>();
		this.beanModificationStatePolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.beanMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;
		this.executionInterceptors = new LinkedList<IExecutionInterceptor>();
		this.exceptionConverter = new DefaultBeanExceptionConverter();
		this.multiSelection = false;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setDestinationEntityLabelPlural(final String label) {
		checkExhausted();
		this.destinationEntityLabel = label;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkCreatorService(final ICreatorService creatorService) {
		checkExhausted();
		Assert.paramNotNull(creatorService, "creatorService");
		this.linkCreatorService = creatorService;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkAttributes(final List<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		this.linkAttributes = (List<IAttribute<Object>>) attributes;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkableTableEntityId(final Object id) {
		checkExhausted();
		Assert.paramNotNull(id, "id");
		this.linkableTableEntityId = id;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkableTableAttributes(final List<? extends IAttribute<?>> attributes) {
		checkExhausted();
		Assert.paramNotNull(attributes, "attributes");
		this.linkableTableAttributes = (List<IAttribute<Object>>) attributes;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkableTableLabel(final String label) {
		checkExhausted();
		Assert.paramNotEmpty(label, "label");
		this.linkableTableLabel = label;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkableTableReaderService(final IReaderService<Void> readerService) {
		checkExhausted();
		Assert.paramNotNull(readerService, "readerService");
		this.linkableReaderService = readerService;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setSourceProperties(final IEntityLinkProperties properties) {
		checkExhausted();
		Assert.paramNotNull(properties, "properties");
		this.sourceLinkProperties = properties;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setSourceProperties(final String keyPropertyName, final String foreignKeyPropertyName) {
		checkExhausted();
		final IEntityLinkPropertiesBuilder linkPropertiesBuilder = CapCommonToolkit.entityLinkPropertiesBuilder();
		linkPropertiesBuilder.setKeyPropertyName(keyPropertyName).setForeignKeyPropertyName(foreignKeyPropertyName);
		return setSourceProperties(linkPropertiesBuilder.build());
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setDestinationProperties(final IEntityLinkProperties properties) {
		checkExhausted();
		Assert.paramNotNull(properties, "properties");
		this.destinationLinkProperties = properties;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setDestinationProperties(
		final String keyPropertyName,
		final String foreignKeyPropertyName) {
		checkExhausted();
		final IEntityLinkPropertiesBuilder linkPropertiesBuilder = CapCommonToolkit.entityLinkPropertiesBuilder();
		linkPropertiesBuilder.setKeyPropertyName(keyPropertyName).setForeignKeyPropertyName(foreignKeyPropertyName);
		return setSourceProperties(linkPropertiesBuilder.build());
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setMultiSelection(final boolean multiSelection) {
		checkExhausted();
		this.multiSelection = multiSelection;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setModificationPolicy(final BeanModificationStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		beanModificationStatePolicy = policy;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setMessageStatePolicy(final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		beanMessageStatePolicy = policy;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> addExecutableChecker(final IExecutableChecker<BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		executableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> addExecutionInterceptor(final IExecutionInterceptor interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setLinkedDataModel(final IDataModel model) {
		checkExhausted();
		this.linkedDataModel = model;
		return this;
	}

	@Override
	public ILinkActionBuilder<BEAN_TYPE> setExceptionConverter(final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	private void setDefaultTextIfNecessary() {
		if (EmptyCheck.isEmpty(getText()) && !EmptyCheck.isEmpty(destinationEntityLabel)) {
			final String message = Messages.getString("LinkActionBuilderImpl.link_var");
			setText(MessageReplacer.replace(message, destinationEntityLabel));
		}

	}

	@Override
	protected IAction doBuild() {
		setDefaultTextIfNecessary();

		final ICommand command = new BeanLinkCommand<BEAN_TYPE>(
			model,
			linkedDataModel,
			linkCreatorService,
			linkAttributes,
			linkableTableEntityId,
			linkableReaderService,
			linkableTableAttributes,
			linkableTableLabel,
			sourceLinkProperties,
			destinationLinkProperties,
			enabledCheckers,
			executableCheckers,
			multiSelection,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			executionInterceptors,
			exceptionConverter);

		final IActionBuilder builder = getBuilder();
		builder.setCommand(command);
		return builder.build();
	}
}
