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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.widgets.IBeanDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanCreatorCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final int INITIAL_MIN_WIDTH = 450;

	private final IBeanListModel<BEAN_TYPE> model;
	private final IBeanFormBluePrint<BEAN_TYPE> beanFormBp;
	private final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators;
	private final ICreatorService creatorService;
	private final IBeanExceptionConverter exceptionConverter;
	private final BeanListModelEnabledChecker<BEAN_TYPE> enabledChecker;
	private final IBeanProxyFactory<BEAN_TYPE> beanFactory;
	private final ExecutionObservable executionObservable;
	private final Map<String, Object> defaultValues;
	private final List<String> properties;

	private Rectangle dialogBounds;

	BeanCreatorCommand(
		final Class<? extends BEAN_TYPE> beanType,
		final List<IBeanPropertyValidator<BEAN_TYPE>> beanPropertyValidators,
		final IBeanListModel<BEAN_TYPE> model,
		final Collection<IAttribute<?>> attributes,
		final IBeanFormBluePrint<BEAN_TYPE> beanFormBp,
		final List<IEnabledChecker> enabledCheckers,
		final boolean anySelection,
		final ICreatorService creatorService,
		final IBeanExceptionConverter exceptionConverter,
		final List<IExecutionInterceptor> executionInterceptors) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(beanFormBp, "beanFormBp");
		Assert.paramNotNull(beanFormBp.getCreateModeAttributes(), "getCreateModeAttributes()");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(anySelection, "anySelection");
		Assert.paramNotNull(creatorService, "creatorService");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		Assert.paramNotNull(executionInterceptors, "executionInterceptors");

		this.enabledChecker = new BeanListModelEnabledChecker<BEAN_TYPE>(
			model,
			anySelection ? BeanSelectionPolicy.ANY_SELECTION : BeanSelectionPolicy.NO_SELECTION,
			BeanModificationStatePolicy.ANY_MODIFICATION,
			null,
			enabledCheckers,
			null,
			true);

		this.beanFactory = CapUiToolkit.beanProxyFactory(beanType);
		this.executionObservable = new ExecutionObservable(executionInterceptors);

		this.model = model;
		this.beanFormBp = beanFormBp;
		this.creatorService = creatorService;
		this.exceptionConverter = exceptionConverter;

		this.defaultValues = new HashMap<String, Object>();
		this.properties = new LinkedList<String>();
		for (final IAttribute<?> attribute : attributes) {
			final String propertyName = attribute.getPropertyName();
			properties.add(propertyName);
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				defaultValues.put(propertyName, defaultValue);
			}
		}
		this.beanPropertyValidators = new LinkedList<IBeanPropertyValidator<BEAN_TYPE>>(beanPropertyValidators);
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return enabledChecker;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		if (!executionObservable.fireBeforeExecution(executionContext)) {
			return;
		}

		final IBeanProxy<BEAN_TYPE> bean = beanFactory.createTransientProxy(properties, defaultValues);
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			bean.addBeanPropertyValidator(validator);
		}

		final IBeanDialogBluePrint<BEAN_TYPE> beanDialogBp = CapUiToolkit.bluePrintFactory().beanDialog(beanFormBp);
		beanDialogBp.autoPackOff();
		if (dialogBounds != null) {
			beanDialogBp.setPosition(dialogBounds.getPosition()).setSize(dialogBounds.getSize());
			beanDialogBp.autoPackOff().autoCenterOff();
		}
		beanDialogBp.setExecutionContext(executionContext);
		final IBeanDialog<BEAN_TYPE> dialog = Toolkit.getActiveWindow().createChildWindow(beanDialogBp);
		dialog.pack();
		dialog.setBean(bean);

		dialog.setSize(Math.max(dialog.getSize().getWidth(), INITIAL_MIN_WIDTH), dialog.getSize().getHeight());

		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			createBean(executionContext, createUnmodifiedBean(bean));
		}
		else {
			executionObservable.fireAfterExecutionCanceled(executionContext);
		}
		dialogBounds = dialog.getBounds();
		dialog.dispose();
	}

	private IBeanProxy<BEAN_TYPE> createUnmodifiedBean(final IBeanProxy<BEAN_TYPE> proxy) {
		final IBeanProxy<BEAN_TYPE> result = beanFactory.createProxy(proxy, properties);
		result.setTransient(true);
		for (final IBeanPropertyValidator<BEAN_TYPE> validator : beanPropertyValidators) {
			result.addBeanPropertyValidator(validator);
		}
		return result;
	}

	private void createBean(final IExecutionContext executionContext, final IBeanProxy<BEAN_TYPE> bean) {
		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		bean.setExecutionTask(executionTask);
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
			@Override
			public void canceled() {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						bean.setExecutionTask(null);
						model.removeBeans(Collections.singletonList(bean));
						model.fireBeansChanged();
						executionObservable.fireAfterExecutionCanceled(executionContext);
					}
				});
			}
		});

		model.addBean(bean);

		final IBeanData beanData = createBeanData(bean);
		final List<IBeanData> data = Collections.singletonList(beanData);

		executionObservable.fireAfterExecutionPrepared(executionContext);
		creatorService.create(new ResultCallback(executionContext, bean), data, executionTask);
	}

	private IBeanData createBeanData(final IBeanProxy<BEAN_TYPE> bean) {
		final IBeanDataBuilder builder = CapCommonToolkit.beanDataBuilder();
		for (final String propertyName : properties) {
			if (propertyName != IBean.ID_PROPERTY) {
				builder.setProperty(propertyName, bean.getValue(propertyName));
			}
		}
		return builder.build();
	}

	private final class ResultCallback extends AbstractUiResultCallback<List<IBeanDto>> {

		private final IExecutionContext executionContext;
		private final IBeanProxy<BEAN_TYPE> bean;

		ResultCallback(final IExecutionContext executionContext, final IBeanProxy<BEAN_TYPE> bean) {
			this.executionContext = executionContext;
			this.bean = bean;
		}

		@Override
		protected void finishedUi(final List<IBeanDto> result) {
			if (!EmptyCheck.isEmpty(result)) {
				bean.setExecutionTask(null);
				bean.updateTransient(result.get(0));
				model.fireBeansChanged();
				executionObservable.fireAfterExecutionSuccess(executionContext);
			}
			else {
				exceptionUi(null);
			}
		}

		@Override
		protected void exceptionUi(final Throwable exception) {
			if (exception != null) {
				final List<IBeanProxy<BEAN_TYPE>> beans = new LinkedList<IBeanProxy<BEAN_TYPE>>();
				beans.add(bean);
				bean.addMessage(exceptionConverter.convert(beans, bean, exception));
			}
			else {
				final IBeanMessageBuilder messageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
				final String message = Messages.getString("BeanCreatorCommand.object_not_created");
				final String description = Messages.getString("BeanCreatorCommand.object_not_created_description");
				messageBuilder.setMessage(message);
				messageBuilder.setDescription(description);
				bean.addMessage(messageBuilder.build());
			}
			bean.setExecutionTask(null);
			model.fireBeansChanged();
			executionObservable.fireAfterExecutionError(executionContext, exception);
		}

	}
}