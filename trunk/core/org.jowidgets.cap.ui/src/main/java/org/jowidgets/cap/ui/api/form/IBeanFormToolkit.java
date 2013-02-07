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

import org.jowidgets.cap.common.api.bean.IBeanFormInfoDescriptor;

public interface IBeanFormToolkit {

	IBeanFormLayoutBuilder layoutBuilder();

	IBeanFormGroupBuilder groupBuilder();

	IBeanFormPropertyBuilder propertyBuilder();

	/**
	 * Creates an layouter for an defined layout. The layouter layouts the full bean form
	 * with beanFormInfo (if exists), validation label and scrollbars (if exists)
	 * 
	 * @param layout The layout to get the layouter for
	 * 
	 * @return The layouter, never null
	 */
	IBeanFormLayouter layouter(IBeanFormLayout layout);

	/**
	 * Gets an layouter builder for an defined layout.
	 * 
	 * @param layout The layout to get the builder for
	 * 
	 * @return The builder, never null
	 */
	IBeanFormLayouterBuilder layouterBuilder(IBeanFormLayout layout);

	/**
	 * Creates a layouter that layouts only the content of the form (with buttons, if defined)
	 * 
	 * @param layout The layout to get the layouter for
	 * 
	 * @return The layouter, never null
	 */
	IBeanFormLayouter contentLayouter(IBeanFormLayout layout);

	IBeanFormInfoBuilder infoBuilder();

	/**
	 * Returns the bean form info for the descriptor, or null if the descriptor is null
	 * 
	 * @param descriptor The descriptor to get the info for
	 * @return The info or null, if the descriptor is null
	 */
	IBeanFormInfo info(IBeanFormInfoDescriptor descriptor);

}
