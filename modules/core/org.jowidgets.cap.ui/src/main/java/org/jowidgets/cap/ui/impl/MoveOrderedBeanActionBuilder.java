/*
 * Copyright (c) 2016, grossmann
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

import java.util.List;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.command.IMoveOrderedBeanActionBuilder;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Accelerator;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.builder.AbstractSingleUseBuilder;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Some;

final class MoveOrderedBeanActionBuilder<BEAN_TYPE extends IOrderedBean> extends AbstractSingleUseBuilder<IAction>
		implements IMoveOrderedBeanActionBuilder<BEAN_TYPE> {

	private final IBeanListModel<BEAN_TYPE> model;
	private final Direction direction;
	private final ExecutorActionBuilderImpl<BEAN_TYPE, Void> executorActionBuilder;

	private ISortModel sortModel;
	private IDataModel dataModel;

	private String text;
	private IMaybe<String> toolTipText;

	private String entityLabelSingular;
	private String entityLabelPlural;

	private String notPossibleSingleMessage;
	private String notPossibleMultiMessage;

	private boolean singleSelection;

	MoveOrderedBeanActionBuilder(final Direction direction, final IBeanListModel<BEAN_TYPE> model) {

		Assert.paramNotNull(direction, "direction");
		Assert.paramNotNull(model, "model");

		this.direction = direction;
		this.model = model;

		this.executorActionBuilder = new ExecutorActionBuilderImpl<BEAN_TYPE, Void>(model);
		this.singleSelection = false;
		executorActionBuilder.setSelectionPolicy(BeanSelectionPolicy.MULTI_SELECTION);

		if (Direction.UP.equals(direction)) {
			setIcon(IconsSmall.MOVE_UP);
			setAccelerator(VirtualKey.U, Modifier.CTRL);
		}
		else {
			setIcon(IconsSmall.MOVE_DOWN);
			setAccelerator(VirtualKey.D, Modifier.CTRL);
		}

	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setSortModelProvider(final ISortModel sortModel) {
		checkExhausted();
		this.sortModel = sortModel;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setDataModel(final IDataModel dataModel) {
		checkExhausted();
		this.dataModel = dataModel;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setEntityLabels(final Object entityId) {
		checkExhausted();
		if (entityId != null) {
			final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
			if (entityService != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(entityId);
				if (descriptor != null) {
					final IMessage labelSingular = descriptor.getLabelSingular();
					if (labelSingular != null && !EmptyCheck.isEmpty(labelSingular.get())) {
						setEntityLabelSingular(labelSingular.get());
					}

					final IMessage labelPlural = descriptor.getLabelPlural();
					if (labelPlural != null && !EmptyCheck.isEmpty(labelPlural.get())) {
						setEntityLabelPlural(labelPlural.get());
					}
				}
			}
		}
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setEntityLabelPlural(final String label) {
		checkExhausted();
		Assert.paramNotEmpty(label, "label");
		this.entityLabelPlural = label;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setEntityLabelSingular(final String label) {
		checkExhausted();
		Assert.paramNotEmpty(label, "label");
		this.entityLabelSingular = label;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMoveNotPossibleSingleSelectionMessage(final String message) {
		checkExhausted();
		this.notPossibleSingleMessage = message;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMoveNotPossibleMultiSelectionMessage(final String message) {
		checkExhausted();
		this.notPossibleMultiMessage = message;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setText(final String text) {
		checkExhausted();
		this.text = text;
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setToolTipText(final String toolTipText) {
		checkExhausted();
		this.toolTipText = new Some<String>(toolTipText);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setIcon(final IImageConstant icon) {
		checkExhausted();
		executorActionBuilder.setIcon(icon);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMnemonic(final Character mnemonic) {
		checkExhausted();
		executorActionBuilder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMnemonic(final char mnemonic) {
		checkExhausted();
		executorActionBuilder.setMnemonic(mnemonic);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setAccelerator(final Accelerator accelerator) {
		checkExhausted();
		executorActionBuilder.setAccelerator(accelerator);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setAccelerator(final char key, final Modifier... modifier) {
		checkExhausted();
		executorActionBuilder.setAccelerator(key, modifier);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setAccelerator(final VirtualKey virtualKey, final Modifier... modifier) {
		checkExhausted();
		executorActionBuilder.setAccelerator(virtualKey, modifier);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setSingleSelection(final boolean singleSelection) {
		checkExhausted();
		this.singleSelection = singleSelection;
		if (singleSelection) {
			executorActionBuilder.setSelectionPolicy(BeanSelectionPolicy.SINGLE_SELECTION);
		}
		else {
			executorActionBuilder.setSelectionPolicy(BeanSelectionPolicy.MULTI_SELECTION);
		}
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setMessageStatePolicy(final BeanMessageStatePolicy policy) {
		checkExhausted();
		executorActionBuilder.setMessageStatePolicy(policy);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> addEnabledChecker(final IEnabledChecker enabledChecker) {
		checkExhausted();
		executorActionBuilder.addEnabledChecker(enabledChecker);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> addExecutableChecker(final IExecutableChecker<BEAN_TYPE> executableChecker) {
		checkExhausted();
		executorActionBuilder.addExecutableChecker(executableChecker);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> addExecutionInterceptor(
		final IExecutionInterceptor<List<IBeanDto>> interceptor) {
		checkExhausted();
		executorActionBuilder.addExecutionInterceptor(interceptor);
		return this;
	}

	@Override
	public IMoveOrderedBeanActionBuilder<BEAN_TYPE> setExceptionConverter(final IBeanExceptionConverter exceptionConverter) {
		checkExhausted();
		executorActionBuilder.setExceptionConverter(exceptionConverter);
		return this;
	}

	IBeanListModel<BEAN_TYPE> getModel() {
		return model;
	}

	IDataModel getDataModel() {
		return dataModel;
	}

	ISortModel getSortModel() {
		return sortModel;
	}

	Direction getDirection() {
		return direction;
	}

	String getNotPossibleSingleMessage() {
		return notPossibleSingleMessage;
	}

	String getNotPossibleMultiMessage() {
		return notPossibleMultiMessage;
	}

	private void setTextAndTooltipText() {
		setText();
		setTooltipText();
	}

	private void setText() {
		if (!EmptyCheck.isEmpty(text)) {
			executorActionBuilder.setText(text);
		}
		else {
			setDefaultText();
		}
	}

	private void setDefaultText() {
		if (Direction.UP.equals(direction)) {
			setDefaultUpText();
		}
		else {
			setDefaultDownText();
		}
	}

	private void setDefaultUpText() {
		final String entityLabel = getEntityLabel();
		if (EmptyCheck.isEmpty(entityLabel)) {
			executorActionBuilder.setText(Messages.getMessage("MoveOrderedBeanActionBuilder.move_up_label").get());
		}
		else {
			final IMessage message = Messages.getMessage("MoveOrderedBeanActionBuilder.move_up_label_entity");
			executorActionBuilder.setText(MessageReplacer.replace(message, entityLabel));
		}
	}

	private void setDefaultDownText() {
		final String entityLabel = getEntityLabel();
		if (EmptyCheck.isEmpty(entityLabel)) {
			executorActionBuilder.setText(Messages.getMessage("MoveOrderedBeanActionBuilder.move_down_label").get());
		}
		else {
			final IMessage message = Messages.getMessage("MoveOrderedBeanActionBuilder.move_down_label_entity");
			executorActionBuilder.setText(MessageReplacer.replace(message, entityLabel));
		}
	}

	private String getEntityLabel() {
		if (singleSelection) {
			return entityLabelSingular;
		}
		else {
			return entityLabelPlural;
		}
	}

	private void setTooltipText() {
		if (toolTipText != null) {
			if (toolTipText.isSomething() && !EmptyCheck.isEmpty(toolTipText.getValue())) {
				executorActionBuilder.setToolTipText(toolTipText.getValue());
			}
		}
		else {
			setDefaultTooltipText();
		}
	}

	private void setDefaultTooltipText() {
		if (Direction.UP.equals(direction)) {
			setDefaultUpTooltipText();
		}
		else {
			setDefaultDownTooltipText();
		}
	}

	private void setDefaultUpTooltipText() {
		if (singleSelection) {
			final String message = Messages.getMessage("MoveOrderedBeanActionBuilder.move_up_tooltip_single").get();
			executorActionBuilder.setToolTipText(message);
		}
		else {
			final String message = Messages.getMessage("MoveOrderedBeanActionBuilder.move_up_tooltip_multi").get();
			executorActionBuilder.setToolTipText(message);
		}
	}

	private void setDefaultDownTooltipText() {
		if (singleSelection) {
			final String message = Messages.getMessage("MoveOrderedBeanActionBuilder.move_down_tooltip_single").get();
			executorActionBuilder.setToolTipText(message);
		}
		else {
			final String message = Messages.getMessage("MoveOrderedBeanActionBuilder.move_down_tooltip_multi").get();
			executorActionBuilder.setToolTipText(message);
		}
	}

	@Override
	protected IAction doBuild() {
		setTextAndTooltipText();
		final MoveOrderedBeanExecutor<BEAN_TYPE> executor = new MoveOrderedBeanExecutor<BEAN_TYPE>(this);
		executorActionBuilder.addEnabledChecker(executor);
		executorActionBuilder.setExecutor(executor);
		return executorActionBuilder.build();
	}

}
