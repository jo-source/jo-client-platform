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

package org.jowidgets.cap.ui.api.widgets;

import java.util.Collection;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.widgets.blueprint.builder.IComponentSetupBuilder;
import org.jowidgets.api.widgets.descriptor.setup.IComponentSetup;
import org.jowidgets.api.widgets.descriptor.setup.IInputComponentValidationLabelSetup;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.form.IBeanFormInfo;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.Border;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.descriptor.setup.mandatory.Mandatory;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IProvider;

public interface IBeanFormBluePrint<BEAN_TYPE> extends
		IComponentSetup,
		IComponentSetupBuilder<IBeanFormBluePrint<BEAN_TYPE>>,
		IWidgetDescriptor<IBeanForm<BEAN_TYPE>>,
		IBeanFormSetupConvenience<BEAN_TYPE, IBeanFormBluePrint<BEAN_TYPE>> {

	IBeanFormBluePrint<BEAN_TYPE> setEntityId(Object entityId);

	IBeanFormBluePrint<BEAN_TYPE> setBeanType(Class<BEAN_TYPE> beanType);

	IBeanFormBluePrint<BEAN_TYPE> setEditModeLayouter(IBeanFormLayouter layouter);

	IBeanFormBluePrint<BEAN_TYPE> setEditModeAttributes(Collection<? extends IAttribute<?>> attributes);

	IBeanFormBluePrint<BEAN_TYPE> setCreateModeLayouter(IBeanFormLayouter layouter);

	IBeanFormBluePrint<BEAN_TYPE> setCreateModeAttributes(Collection<? extends IAttribute<?>> attributes);

	IBeanFormBluePrint<BEAN_TYPE> setAutoResetValidation(boolean autoResetValidation);

	/**
	 * Determines if the bean form may have scrollbars (e.g. for the content). If bean forms
	 * should be embedded or combined with other bean forms, it may be possible, that the parent container should have scrollbars,
	 * so by returning false, the forms scrollbars could be deactivated.
	 * 
	 * @return this builder
	 */
	IBeanFormBluePrint<BEAN_TYPE> setScrollbarsAllowed(boolean scrollBarsAllowed);

	/**
	 * Sets the default value for the maximal width of the forms content. If set to null, the width is unbounded.
	 * This value can be used by a {@link IBeanFormLayouter} the determine the max width default.
	 * 
	 * Remark that the layouter may ignore or override this value!
	 * 
	 * @param maxWidth The max width to set or null
	 * 
	 * @return This builder
	 */
	IBeanFormBluePrint<BEAN_TYPE> setMaxWidthDefault(Integer maxWidth);

	IBeanFormBluePrint<BEAN_TYPE> setBorder(Border border);

	IBeanFormBluePrint<BEAN_TYPE> setEditModeValidationLabel(IInputComponentValidationLabelSetup validationLabel);

	IBeanFormBluePrint<BEAN_TYPE> setCreateModeValidationLabel(IInputComponentValidationLabelSetup validationLabel);

	IBeanFormBluePrint<BEAN_TYPE> setPropertyValidationLabel(IValidationLabelSetup validationLabel);

	IBeanFormBluePrint<BEAN_TYPE> setMandatoryBackgroundColor(IColorConstant color);

	IBeanFormBluePrint<BEAN_TYPE> setMandatoryLabelDecorator(IDecorator<String> decorator);

	IBeanFormBluePrint<BEAN_TYPE> setEditModeInputHint(String hint);

	IBeanFormBluePrint<BEAN_TYPE> setCreateModeInputHint(String hint);

	IBeanFormBluePrint<BEAN_TYPE> setCreateFormInfo(IBeanFormInfo info);

	IBeanFormBluePrint<BEAN_TYPE> setEditFormInfo(IBeanFormInfo info);

	IBeanFormBluePrint<BEAN_TYPE> setCreateModeForegroundColor(IColorConstant color);

	IBeanFormBluePrint<BEAN_TYPE> setEditModeForegroundColor(IColorConstant color);

	IBeanFormBluePrint<BEAN_TYPE> setModifiedForegroundColor(IColorConstant color);

	IBeanFormBluePrint<BEAN_TYPE> setSaveAction(IProvider<IAction> saveAction);

	IBeanFormBluePrint<BEAN_TYPE> setUndoAction(IProvider<IAction> undoAction);

	IBeanFormBluePrint<BEAN_TYPE> setCustomActions(Collection<? extends IProvider<IAction>> actions);

	Object getEntityId();

	Class<BEAN_TYPE> getBeanType();

	IBeanFormLayouter getEditModeLayouter();

	@Mandatory
	Collection<IAttribute<?>> getEditModeAttributes();

	IBeanFormLayouter getCreateModeLayouter();

	@Mandatory
	Collection<IAttribute<?>> getCreateModeAttributes();

	@Mandatory
	boolean isAutoResetValidation();

	@Mandatory
	boolean getScrollbarsAllowed();

	Integer getMaxWidthDefault();

	Border getBorder();

	IBeanFormInfo getCreateFormInfo();

	IBeanFormInfo getEditFormInfo();

	IInputComponentValidationLabelSetup getEditModeValidationLabel();

	IInputComponentValidationLabelSetup getCreateModeValidationLabel();

	IColorConstant getMandatoryBackgroundColor();

	IDecorator<String> getMandatoryLabelDecorator();

	String getEditModeInputHint();

	String getCreateModeInputHint();

	IColorConstant getCreateModeForegroundColor();

	IColorConstant getEditModeForegroundColor();

	IColorConstant getModifiedForegroundColor();

	IProvider<IAction> getSaveAction();

	IProvider<IAction> getUndoAction();

	Collection<IProvider<IAction>> getCustomActions();

	IValidationLabelSetup getPropertyValidationLabel();

}
