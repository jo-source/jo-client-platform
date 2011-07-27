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

import org.jowidgets.api.widgets.blueprint.builder.IComponentSetupBuilder;
import org.jowidgets.api.widgets.descriptor.IValidationResultLabelDescriptor;
import org.jowidgets.api.widgets.descriptor.setup.IComponentSetup;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.common.types.Border;
import org.jowidgets.common.widgets.descriptor.IWidgetDescriptor;
import org.jowidgets.common.widgets.descriptor.setup.mandatory.Mandatory;

public interface IBeanFormBluePrint<BEAN_TYPE> extends
		IComponentSetup,
		IComponentSetupBuilder<IBeanFormBluePrint<BEAN_TYPE>>,
		IWidgetDescriptor<IBeanForm<BEAN_TYPE>> {

	IBeanFormBluePrint<BEAN_TYPE> setLayouter(IBeanFormLayouter layouter);

	IBeanFormBluePrint<BEAN_TYPE> setAttributes(Collection<? extends IAttribute<?>> attributes);

	IBeanFormBluePrint<BEAN_TYPE> setAutoResetValidation(final boolean autoResetValidation);

	IBeanFormBluePrint<BEAN_TYPE> setContentScrolled(final boolean scrolledContent);

	IBeanFormBluePrint<BEAN_TYPE> setBorder(Border border);

	IBeanFormBluePrint<BEAN_TYPE> setContentBorder(Border border);

	IBeanFormBluePrint<BEAN_TYPE> setValidationLabel(final IValidationResultLabelDescriptor validationLabelDescriptor);

	@Mandatory
	IBeanFormLayouter getLayouter();

	@Mandatory
	Collection<IAttribute<?>> getAttributes();

	@Mandatory
	boolean isAutoResetValidation();

	@Mandatory
	boolean isContentScrolled();

	Border getBorder();

	Border getContentBorder();

	IValidationResultLabelDescriptor getValidationLabel();

}
