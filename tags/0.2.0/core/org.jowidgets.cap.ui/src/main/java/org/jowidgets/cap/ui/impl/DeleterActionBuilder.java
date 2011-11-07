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
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.jowidgets.util.Assert;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;

final class DeleterActionBuilder<BEAN_TYPE> extends AbstractSingleUseBuilder<IAction> implements IDeleterActionBuilder<BEAN_TYPE> {

	private final IBeanListModel<BEAN_TYPE> model;
	private final IActionBuilder builder;
	private final List<IEnabledChecker> enabledCheckers;
	private final List<IExecutableChecker<BEAN_TYPE>> executableCheckers;
	private final List<IExecutionInterceptor> executionInterceptors;

	private IDeleterService deleterService;
	private boolean multiSelection;
	private boolean autoSelection;
	private boolean deletionConfirmDialog;

	private final BeanModificationStatePolicy beanModificationStatePolicy;
	private BeanMessageStatePolicy beanMessageStatePolicy;
	private IBeanExecptionConverter exceptionConverter;

	DeleterActionBuilder(final IBeanListModel<BEAN_TYPE> model) {
		checkExhausted();
		Assert.paramNotNull(model, "model");
		this.model = model;
		this.builder = Toolkit.getActionBuilderFactory().create();
		this.enabledCheckers = new LinkedList<IEnabledChecker>();
		this.executableCheckers = new LinkedList<IExecutableChecker<BEAN_TYPE>>();
		this.executionInterceptors = new LinkedList<IExecutionInterceptor>();
		this.exceptionConverter = new DefaultBeanExceptionConverter();

		this.multiSelection = true;
		this.autoSelection = true;
		this.deletionConfirmDialog = true;
		this.beanModificationStatePolicy = BeanModificationStatePolicy.NO_MODIFICATION;
		this.beanMessageStatePolicy = BeanMessageStatePolicy.NO_WARNING_OR_ERROR;

		builder.setText(Messages.getString("DeleterActionBuilder.delete_data_set"));
		builder.setToolTipText(Messages.getString("DeleterActionBuilder.delete_data_set_tooltip"));
		builder.setAccelerator(VirtualKey.DELETE);
		builder.setIcon(IconsSmall.DELETE);
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setText(final String text) {
		checkExhausted();
		builder.setText(text);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setToolTipText(final String toolTipText) {
		checkExhausted();
		builder.setToolTipText(toolTipText);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setIcon(final IImageConstant icon) {
		checkExhausted();
		builder.setIcon(icon);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setMnemonic(final Character mnemonic) {
		checkExhausted();
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setMnemonic(final char mnemonic) {
		checkExhausted();
		builder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setAccelerator(final Accelerator accelerator) {
		checkExhausted();
		builder.setAccelerator(accelerator);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setAccelerator(final char key, final Modifier... modifier) {
		checkExhausted();
		builder.setAccelerator(key, modifier);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setDeleterService(final IDeleterService deleterService) {
		checkExhausted();
		Assert.paramNotNull(deleterService, "creatorService");
		this.deleterService = deleterService;
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setDeleterService(final IServiceId<IDeleterService> deleterServiceId) {
		checkExhausted();
		Assert.paramNotNull(deleterServiceId, "deleterServiceId");
		final IDeleterService service = ServiceProvider.getService(deleterServiceId);
		if (service == null) {
			throw new IllegalArgumentException("No deleter service found for the id '" + deleterServiceId + "'.");
		}
		return setDeleterService(service);
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setDeleterService(final String deleterServiceId) {
		checkExhausted();
		Assert.paramNotNull(deleterServiceId, "deleterServiceId");
		return setDeleterService(new ServiceId<IDeleterService>(deleterServiceId, IDeleterService.class));
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		Assert.paramNotNull(enabledChecker, "enabledChecker");
		enabledCheckers.add(enabledChecker);
		return this;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> setMultiSelectionPolicy(final boolean multiSelection) {
		checkExhausted();
		this.multiSelection = multiSelection;
		return this;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> setMessageStatePolicy(final BeanMessageStatePolicy policy) {
		checkExhausted();
		Assert.paramNotNull(policy, "policy");
		this.beanMessageStatePolicy = policy;
		return this;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> addExecutableChecker(final IExecutableChecker<BEAN_TYPE> executableChecker) {
		checkExhausted();
		Assert.paramNotNull(executableChecker, "executableChecker");
		executableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> addExecutionInterceptor(final IExecutionInterceptor interceptor) {
		checkExhausted();
		Assert.paramNotNull(interceptor, "interceptor");
		executionInterceptors.add(interceptor);
		return this;
	}

	@Override
	public DeleterActionBuilder<BEAN_TYPE> setExceptionConverter(final IBeanExecptionConverter exceptionConverter) {
		checkExhausted();
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		this.exceptionConverter = exceptionConverter;
		return this;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> setAutoSelection(final boolean autoSelection) {
		this.autoSelection = autoSelection;
		return this;
	}

	@Override
	public IDeleterActionBuilder<BEAN_TYPE> setDeletionConfirmDialog(final boolean deletionConfirmDialog) {
		this.deletionConfirmDialog = deletionConfirmDialog;
		return this;
	}

	@Override
	protected IAction doBuild() {
		final BeanDeleterCommand<BEAN_TYPE> deleterCommand = new BeanDeleterCommand<BEAN_TYPE>(
			model,
			enabledCheckers,
			executableCheckers,
			deleterService,
			executionInterceptors,
			multiSelection,
			beanModificationStatePolicy,
			beanMessageStatePolicy,
			exceptionConverter,
			autoSelection,
			deletionConfirmDialog);
		builder.setCommand((ICommand) deleterCommand);
		return builder.build();
	}
}
