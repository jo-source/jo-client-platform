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

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkData;
import org.jowidgets.cap.common.api.link.ILinkDataBuilder;
import org.jowidgets.cap.common.api.link.LinkData;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanPropertyValidator;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel.IBeanLink;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanelBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IFactory;

final class BeanLinkCreatorCommand<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> implements ICommand, ICommandExecutor {

	private final String nothingSelectedMessage = Messages.getString("BeanLinkCreatorCommand.nothing_selected");
	private final String shortErrorMessage = Messages.getString("BeanLinkCreatorCommand.short_error_message");

	private final IEntityLinkProperties sourceProperties;
	private final IEntityLinkProperties destinationProperties;
	private final ILinkCreatorService linkCreatorService;
	private final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private final IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel;
	private final Class<? extends LINK_BEAN_TYPE> linkBeanType;
	private final IBeanFormBluePrint<LINK_BEAN_TYPE> linkBeanForm;
	private final IFactory<IBeanProxy<LINK_BEAN_TYPE>> linkDefaultFactory;
	private final List<IBeanPropertyValidator<LINK_BEAN_TYPE>> linkBeanPropertyValidators;
	private final Class<? extends LINKABLE_BEAN_TYPE> linkableBeanType;
	private final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableBeanForm;
	private final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTable;
	private final List<IBeanPropertyValidator<LINKABLE_BEAN_TYPE>> linkableBeanPropertyValidators;
	private final IBeanExceptionConverter exceptionConverter;

	private final BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE> enabledChecker;
	private final ExecutionObservable<List<IBeanDto>> executionObservable;

	private Rectangle dialogBounds;

	BeanLinkCreatorCommand(
		final IEntityLinkProperties sourceProperties,
		final IEntityLinkProperties destinationProperties,
		final ILinkCreatorService linkCreatorService,
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final boolean sourceMultiSelection,
		final BeanModificationStatePolicy sourceModificationPolicy,
		final BeanMessageStatePolicy sourceMessageStatePolicy,
		final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers,
		final IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel,
		final Class<? extends LINK_BEAN_TYPE> linkBeanType,
		final IBeanFormBluePrint<LINK_BEAN_TYPE> linkBeanForm,
		final IFactory<IBeanProxy<LINK_BEAN_TYPE>> linkDefaultFactory,
		final List<IBeanPropertyValidator<LINK_BEAN_TYPE>> linkBeanPropertyValidators,
		final Class<? extends LINKABLE_BEAN_TYPE> linkableBeanType,
		final IBeanFormBluePrint<LINKABLE_BEAN_TYPE> linkableBeanForm,
		final IBeanTableBluePrint<LINKABLE_BEAN_TYPE> linkableTable,
		final List<IBeanPropertyValidator<LINKABLE_BEAN_TYPE>> linkableBeanPropertyValidators,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors,
		final IBeanExceptionConverter exceptionConverter) {

		Assert.paramNotNull(sourceProperties, "sourceProperties");
		Assert.paramNotNull(linkCreatorService, "linkCreatorService");
		Assert.paramNotNull(sourceModificationPolicy, "sourceModificationPolicy");
		Assert.paramNotNull(sourceMessageStatePolicy, "sourceMessageStatePolicy");
		Assert.paramNotNull(sourceExecutableCheckers, "sourceExecutableCheckers");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");
		Assert.paramNotNull(linkBeanPropertyValidators, "linkBeanPropertyValidators");
		Assert.paramNotNull(linkableBeanPropertyValidators, "linkableBeanPropertyValidators");

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE>(
			source,
			sourceMultiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			enabledCheckers,
			sourceExecutableCheckers,
			false);

		this.source = source;
		this.sourceProperties = sourceProperties;
		this.destinationProperties = destinationProperties;
		this.linkCreatorService = linkCreatorService;
		this.linkedModel = linkedModel;
		this.linkBeanType = linkBeanType;
		this.linkBeanForm = linkBeanForm;
		this.linkDefaultFactory = linkDefaultFactory;
		this.linkBeanPropertyValidators = new LinkedList<IBeanPropertyValidator<LINK_BEAN_TYPE>>(linkBeanPropertyValidators);
		this.linkableBeanType = linkableBeanType;
		this.linkableBeanForm = linkableBeanForm;
		this.linkableTable = linkableTable;
		this.linkableBeanPropertyValidators = new LinkedList<IBeanPropertyValidator<LINKABLE_BEAN_TYPE>>(
			linkableBeanPropertyValidators);
		this.executionObservable = new ExecutionObservable<List<IBeanDto>>(executionInterceptors);
		this.exceptionConverter = exceptionConverter;
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

		final List<IBeanProxy<SOURCE_BEAN_TYPE>> selection = source.getBeanSelection().getSelection();

		if (EmptyCheck.isEmpty(selection)) {
			Toolkit.getMessagePane().showWarning(executionContext, nothingSelectedMessage);
			return;
		}

		final IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> beanLink = getBeanLink(executionContext);

		if (beanLink != null) {
			linkBeans(executionContext, selection, beanLink);
		}
	}

	private void linkBeans(
		final IExecutionContext executionContext,
		final List<IBeanProxy<SOURCE_BEAN_TYPE>> selection,
		final IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> beanLink) {

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
			@Override
			public void canceled() {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
							bean.setExecutionTask(null);
						}
						executionObservable.fireAfterExecutionCanceled(executionContext);
					}
				});
			}
		});

		final List<ILinkData> linkData = new LinkedList<ILinkData>();
		for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
			bean.setExecutionTask(executionTask);

			//direct link
			if (beanLink.getLinkableBeans().isEmpty() && destinationProperties == null) {
				final IBeanDataBuilder linkBeanBuilder = CapCommonToolkit.beanDataBuilder();
				if (beanLink.getLinkBean() != null) {
					setBeanData(beanLink.getLinkBean(), linkBeanBuilder);
				}
				linkBeanBuilder.setProperty(sourceProperties.getForeignKeyPropertyName(), bean.getId());
				final ILinkDataBuilder linkDataBuilder = LinkData.builder();
				linkDataBuilder.setLinkData(linkBeanBuilder.build());
				linkData.add(linkDataBuilder.build());
			}
			else {
				for (final IBeanProxy<LINKABLE_BEAN_TYPE> linkableBean : beanLink.getLinkableBeans()) {
					final IBeanDataBuilder linkBeanBuilder = CapCommonToolkit.beanDataBuilder();
					if (beanLink.getLinkBean() != null) {
						setBeanData(beanLink.getLinkBean(), linkBeanBuilder);
					}
					linkBeanBuilder.setProperty(sourceProperties.getForeignKeyPropertyName(), bean.getId());

					final ILinkDataBuilder linkDataBuilder = LinkData.builder();

					if (linkableBean.isTransient()) {
						linkDataBuilder.setLinkData(linkBeanBuilder.build());
						linkDataBuilder.setLinkableData(linkableBean.getBeanData());
					}
					else if (destinationProperties != null) {
						linkBeanBuilder.setProperty(destinationProperties.getForeignKeyPropertyName(), linkableBean.getId());
					}
					linkDataBuilder.setLinkData(linkBeanBuilder.build());

					linkData.add(linkDataBuilder.build());
				}
			}
		}

		executionObservable.fireAfterExecutionPrepared(executionContext);

		linkCreatorService.create(createResultCallback(selection, executionContext), linkData, executionTask);

	}

	private IResultCallback<List<IBeanDto>> createResultCallback(
		final List<IBeanProxy<SOURCE_BEAN_TYPE>> selection,
		final IExecutionContext executionContext) {
		return new AbstractUiResultCallback<List<IBeanDto>>() {

			@Override
			protected void finishedUi(final List<IBeanDto> result) {
				for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
					bean.setExecutionTask(null);
				}
				if (linkedModel != null) {
					for (final IBeanDto resultBean : result) {
						linkedModel.addBeanDto(resultBean);
					}
				}
				executionObservable.fireAfterExecutionSuccess(executionContext, result);
			}

			@Override
			protected void exceptionUi(final Throwable exception) {
				for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
					bean.setExecutionTask(null);
					bean.addMessage(exceptionConverter.convert(getShortErrorMessage(), selection, bean, exception));
				}
				executionObservable.fireAfterExecutionError(executionContext, exception);
			}

			private String getShortErrorMessage() {
				final String actionText = executionContext.getAction().getText().replaceAll("\\.", "").trim();
				return MessageReplacer.replace(shortErrorMessage, actionText);
			}
		};
	}

	private void setBeanData(final IBeanProxy<?> bean, final IBeanDataBuilder builder) {
		for (final String propertyName : bean.getProperties()) {
			if (!bean.isTransient() || !propertyName.equals(IBean.ID_PROPERTY)) {
				builder.setProperty(propertyName, bean.getValue(propertyName));
			}
		}
	}

	private IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> getBeanLink(final IExecutionContext executionContext) {
		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
		final IBeanLinkPanelBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> linkPanelBp = cbpf.beanLinkPanel();
		linkPanelBp.setLinkBeanForm(linkBeanForm);
		linkPanelBp.setLinkableBeanForm(linkableBeanForm);
		linkPanelBp.setLinkableTable(linkableTable);

		final IBeanLinkDialogBluePrint<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> dialogBp = cbpf.beanLinkDialog(linkPanelBp);
		dialogBp.setMinPackSize(new Dimension(800, 600));
		dialogBp.setMaxPackSize(new Dimension(1600, 1000));
		dialogBp.setExecutionContext(executionContext);
		dialogBp.setMissingInputHint(null);
		dialogBp.setContentScrolled(false);
		if (dialogBounds != null) {
			dialogBp.setPosition(dialogBounds.getPosition()).setSize(dialogBounds.getSize());
			dialogBp.autoPackOff().autoCenterOff();
		}

		final IBeanLinkDialog<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> linkDialog;
		linkDialog = Toolkit.getActiveWindow().createChildWindow(dialogBp);

		final IBeanProxy<LINK_BEAN_TYPE> defaultLinkBean;
		if (linkDefaultFactory != null) {
			defaultLinkBean = linkDefaultFactory.create();
		}
		else {
			defaultLinkBean = createDefaultBean(linkBeanForm, linkBeanType, linkBeanPropertyValidators);
		}

		final List<IBeanProxy<LINKABLE_BEAN_TYPE>> defaultLinkedBeans;
		if (linkableBeanForm != null) {
			defaultLinkedBeans = Collections.singletonList(createDefaultBean(
					linkableBeanForm,
					linkableBeanType,
					linkableBeanPropertyValidators));
		}
		else {
			defaultLinkedBeans = Collections.emptyList();
		}

		linkDialog.setValue(new IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>() {
			@Override
			public IBeanProxy<LINK_BEAN_TYPE> getLinkBean() {
				return defaultLinkBean;
			}

			@Override
			public List<IBeanProxy<LINKABLE_BEAN_TYPE>> getLinkableBeans() {
				return defaultLinkedBeans;
			}
		});

		linkDialog.setVisible(true);

		final IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> result;
		if (linkDialog.isOkPressed()) {
			final IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> dialogValue = linkDialog.getValue();
			result = new IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>() {
				@Override
				public IBeanProxy<LINK_BEAN_TYPE> getLinkBean() {
					final IBeanProxy<LINK_BEAN_TYPE> original = dialogValue.getLinkBean();
					return original != null ? original : defaultLinkBean;
				}

				@Override
				public List<IBeanProxy<LINKABLE_BEAN_TYPE>> getLinkableBeans() {
					return dialogValue.getLinkableBeans();
				}
			};
		}
		else {
			result = null;
		}

		dialogBounds = linkDialog.getBounds();
		linkDialog.dispose();

		return result;
	}

	private <BEAN_TYPE> IBeanProxy<BEAN_TYPE> createDefaultBean(
		final IBeanFormBluePrint<BEAN_TYPE> formBp,
		final Class<? extends BEAN_TYPE> beanType,
		final List<IBeanPropertyValidator<BEAN_TYPE>> validators) {
		if (formBp != null) {
			final HashMap<String, Object> defaultValues = new HashMap<String, Object>();
			final LinkedList<String> properties = new LinkedList<String>();
			final Collection<IAttribute<?>> attributes = formBp.getCreateModeAttributes();
			for (final IAttribute<?> attribute : attributes) {
				final String propertyName = attribute.getPropertyName();
				properties.add(propertyName);
				final Object defaultValue = attribute.getDefaultValue();
				if (defaultValue != null) {
					defaultValues.put(propertyName, defaultValue);
				}
			}
			final IBeanProxyFactory<BEAN_TYPE> proxyFactory = CapUiToolkit.beanProxyFactory(beanType);
			final IBeanProxy<BEAN_TYPE> result = proxyFactory.createTransientProxy(attributes, defaultValues);
			for (final IBeanPropertyValidator<BEAN_TYPE> validator : validators) {
				result.addBeanPropertyValidator(validator);
			}
			return proxyFactory.createTransientProxy(attributes, defaultValues);
		}
		else {
			return null;
		}
	}
}
