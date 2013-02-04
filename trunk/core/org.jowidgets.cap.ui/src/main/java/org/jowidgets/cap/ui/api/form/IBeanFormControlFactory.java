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

package org.jowidgets.cap.ui.api.form;

import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.ITextLabel;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;

public interface IBeanFormControlFactory {

	/**
	 * Creates the main validation label
	 * 
	 * @return The main validation label or null, if no main validation label exists
	 */
	ICustomWidgetCreator<? extends IControl> createMainValidationLabel();

	/**
	 * Creates a label for the defined property
	 * 
	 * @param propertyName The property to get the label for
	 * @param alignment The alignment of the label
	 * 
	 * @return The creator for the label, or null, if no label can be created for the property
	 */
	ICustomWidgetCreator<ITextLabel> createLabel(String propertyName, AlignmentHorizontal alignment);

	/**
	 * Creates a control for a defined property.
	 * 
	 * @param propertyName The property to get the control for
	 * 
	 * @return The control creator or null, of no control can be created for the property
	 */
	ICustomWidgetCreator<? extends IControl> createControl(String propertyName);

	/**
	 * Creates a validation label for a defined property
	 * 
	 * @param propertyName The property to get the validation label for
	 * 
	 * @return The validation label creator or null, if no validation label can be created
	 */
	ICustomWidgetCreator<? extends IControl> createPropertyValidationLabel(String propertyName);

	/**
	 * @return true, if the properties have validation labels, false otherwise
	 */
	boolean hasPropertyValidationLabels();

	/**
	 * Determines if the bean form may have scrollbars (e.g. for the content). If bean forms
	 * should be embedded or combined with other bean forms, it may be possible, that the parent container should have scrollbars,
	 * so by returning false, the forms scrollbars could be deactivated.
	 * 
	 * @return true, if the bean form can have scroll bars, false otherwise.
	 */
	boolean getScrollbarsAllowed();

	/**
	 * @return The default max width or null if unbound
	 */
	Integer getMaxWidthDefault();

	/**
	 * @return The save button or null, if no save button exists
	 */
	ICustomWidgetCreator<? extends IControl> createSaveButton();

	/**
	 * @return The undo button or null, if no undo button exists
	 */
	ICustomWidgetCreator<? extends IControl> createUndoButton();

}
