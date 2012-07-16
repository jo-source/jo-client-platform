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

package org.jowidgets.cap.ui.impl;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.ui.api.bean.BeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.plugin.IServiceActionDecoratorPlugin;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class LinkDeleterActionBuilderImpl<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> extends
		AbstractCapActionBuilderImpl<ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE>> implements
		ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> {

	private final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers;
	private final List<IExecutableChecker<LINKED_BEAN_TYPE>> linkedExecutableCheckers;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors;

	private ILinkDeleterService deleterService;
	private IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private boolean sourceMultiSelection;
	private BeanModificationStatePolicy sourceModificationPolicy;
	private BeanMessageStatePolicy sourceMessageStatePolicy;
	private boolean linkedMultiSelection;
	private BeanModificationStatePolicy linkedModificationPolicy;
	private BeanMessageStatePolicy linkedMessageStatePolicy;
	private IBeanListModel<LINKED_BEAN_TYPE> linkedModel;
	private String linkedEntityLabelSingular;
	private String linkedEntityLabelPlural;
	private IBeanExceptionConverter exceptionConverter;
	private boolean autoSelection;
	private boolean deletionConfirmDialog;

	LinkDeleterActionBuilderImpl(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final IBeanListModel<LINKED_BEAN_TYPE> linkedModel,
		final IEntityLinkDescriptor linkDescriptor) {
		this();
		Assert.paramNotNull(source, "source");
		Assert.paramNotNull(linkedModel, "linkedModel");
		Assert.paramNotNull(linkDescriptor, "linkDescriptor");

		setSource(source);
		setLinkedModel(linkedModel);

		setLinkDeleterService(linkDescriptor.getLinkDeleterService());

		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final Object linkedEntityId = linkDescriptor.getLinkedEntityId();
			if (linkedEntityId != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkedEntityId);
				if (descriptor != null) {
					setLinkedEntityLabelSingular(descriptor.getLabelSingular());
					setLinkedEntityLabelPlural(descriptor.getLabelPlural());
				}
			}
		}
	}

	LinkDeleterActionBuilderImpl() {
		super();
		this.autoSelection = true;
		this.deletionConfirmDialog = true;

		this.sourceExecutableCheckers = new LinkedList<IExecutableChecker<SOURCE_BEAN_TYPE>>();
		this.linkedExecutableCheckers = new LinkedList<IExecutableChecker<LINKED_BEAN_TYPE>>();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor<List<IBeanDto>>>();

		this.sourceMultiSelection = false;
		this.sourceModificationPolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.sourceMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;

		this.linkedMultiSelection = true;
		this.linkedModificationPolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.linkedMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;
		this.exceptionConverter = BeanExceptionConverter.get();

		setIcon(IconsSmall.SUB);
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSource(
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> selectionProvider) {
		checkExhausted();
		Assert.paramNotNull(selectionProvider, "selectionProvider");
		this.source = selectionProvider;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSourceMultiSelection(final boolean multiSelection) {
		checkExhausted();
		this.sourceMultiSelection = multiSelection;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedMultiSelection(final boolean multiSelection) {
		checkExhausted();
		this.linkedMultiSelection = multiSelection;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSourceModificationPolicy(
		final BeanModificationStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.sourceModificationPolicy = policy;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedModificationPolicy(
		final BeanModificationStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.linkedModificationPolicy = policy;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setSourceMessageStatePolicy(
		final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.sourceMessageStatePolicy = policy;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedMessageStatePolicy(
		final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.linkedMessageStatePolicy = policy;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addSourceExecutableChecker(
		final IExecutableChecker<SOURCE_BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		sourceExecutableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addLinkedExecutableChecker(
		final IExecutableChecker<LINKED_BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		linkedExecutableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedEntityLabelSingular(final String label) {
		checkExhausted();
		this.linkedEntityLabelSingular = label;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedEntityLabelPlural(final String label) {
		checkExhausted();
		this.linkedEntityLabelPlural = label;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkDeleterService(
		final ILinkDeleterService deleterService) {
		checkExhausted();
		Assert.paramNotNull(deleterService, "deleterService");
		this.deleterService = deleterService;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		this.enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setAutoSelection(final boolean autoSelection) {
		checkExhausted();
		this.autoSelection = autoSelection;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setDeletionConfirmDialog(
		final boolean deletionConfirmDialog) {
		this.deletionConfirmDialog = deletionConfirmDialog;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setExceptionConverter(
		final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> addExecutionInterceptor(
		final IExecutionInterceptor<List<IBeanDto>> interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		this.executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public ILinkDeleterActionBuilder<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE> setLinkedModel(
		final IBeanListModel<LINKED_BEAN_TYPE> model) {
		checkExhausted();
		this.linkedModel = model;
		return this;
	}

	private void setDefaultTextIfNecessary() {
		if (EmptyCheck.isEmpty(getText())) {
			if (!EmptyCheck.isEmpty(linkedEntityLabelSingular) && !linkedMultiSelection) {
				final String message = Messages.getString("LinkDeleterActionBuilder.delete_single_var");
				setText(MessageReplacer.replace(message, linkedEntityLabelSingular));
			}
			else if (!EmptyCheck.isEmpty(linkedEntityLabelPlural) && linkedMultiSelection) {
				final String message = Messages.getString("LinkDeleterActionBuilder.delete_multi_var");
				setText(MessageReplacer.replace(message, linkedEntityLabelPlural));
			}
			else if (!linkedMultiSelection) {
				setText(Messages.getString("LinkDeleterActionBuilder.delete_single"));
			}
			else {
				setText(Messages.getString("LinkDeleterActionBuilder.delete_multi"));
			}
		}
	}

	private void setDefaultToolTipTextIfNecessary() {
		if (EmptyCheck.isEmpty(getToolTipText())) {
			if (!linkedMultiSelection) {
				setToolTipText(Messages.getString("LinkDeleterActionBuilder.delete_single_tooltip"));
			}
			else {
				setToolTipText(Messages.getString("LinkDeleterActionBuilder.delete_multi_tooltip"));
			}
		}
	}

	@Override
	public IAction doBuild() {
		return decorateActionWithPlugins(buildAction());
	}

	private IAction decorateActionWithPlugins(final IAction action) {
		IAction result = action;
		final IPluginProperties properties = PluginProperties.create(
				IServiceActionDecoratorPlugin.SERVICE_TYPE_PROPERTY_KEY,
				ILinkDeleterService.class);
		final List<IServiceActionDecoratorPlugin> plugins = PluginProvider.getPlugins(
				IServiceActionDecoratorPlugin.ID,
				properties);
		for (final IServiceActionDecoratorPlugin plugin : plugins) {
			result = plugin.decorate(result, deleterService);
			if (result == null) {
				return null;
			}
		}
		return result;
	}

	private IAction buildAction() {
		setDefaultTextIfNecessary();
		setDefaultToolTipTextIfNecessary();
		final ICommand command = new BeanLinkDeleterCommand<SOURCE_BEAN_TYPE, LINKED_BEAN_TYPE>(
			deleterService,
			deletionConfirmDialog,
			source,
			sourceMultiSelection,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			sourceExecutableCheckers,
			linkedModel,
			linkedMultiSelection,
			linkedModificationPolicy,
			linkedMessageStatePolicy,
			linkedExecutableCheckers,
			enabledCheckers,
			autoSelection,
			executionInterceptors,
			exceptionConverter);

		final IActionBuilder builder = getBuilder();
		builder.setCommand(command);

		return builder.build();
	}
}
