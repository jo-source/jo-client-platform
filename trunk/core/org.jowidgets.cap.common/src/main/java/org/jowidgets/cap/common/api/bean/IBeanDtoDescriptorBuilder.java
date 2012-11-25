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

package org.jowidgets.cap.common.api.bean;

import java.util.Collection;

import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.i18n.api.IMessage;

public interface IBeanDtoDescriptorBuilder {

	IBeanDtoDescriptorBuilder setLabelSingular(IMessage label);

	IBeanDtoDescriptorBuilder setLabelSingular(String label);

	IBeanDtoDescriptorBuilder setLabelPlural(IMessage label);

	IBeanDtoDescriptorBuilder setLabelPlural(String label);

	IBeanDtoDescriptorBuilder setDescription(String description);

	IBeanDtoDescriptorBuilder setDescription(IMessage description);

	IBeanDtoDescriptorBuilder setDefaultSorting(ISort... defaultSorting);

	IBeanDtoDescriptorBuilder setDefaultSorting(Collection<ISort> defaultSorting);

	/**
	 * Sets a pattern that will be used to render instances of the bean.
	 * The pattern may contain property variables starting and ending with $.
	 * The $ symbol could be escaped with a $ symbol.
	 * 
	 * Example:
	 * 
	 * $name$($country$, $age$, $$$acount$)
	 * 
	 * leads to labels like
	 * 
	 * Michael(Germany, 40, $1000)
	 * Peter(France, 28, $200)
	 * 
	 * @return The rendering pattern or null
	 */
	IBeanDtoDescriptorBuilder setRenderingPattern(IMessage pattern);

	/**
	 * Sets a pattern that will be used to render instances of the bean.
	 * The pattern may contain property variables starting and ending with $.
	 * The $ symbol could be escaped with a $ symbol.
	 * 
	 * Example:
	 * 
	 * $name$($country$, $age$, $$$acount$)
	 * 
	 * leads to labels like
	 * 
	 * Michael(Germany, 40, $1000)
	 * Peter(France, 28, $200)
	 * 
	 * @return The rendering pattern or null
	 */
	IBeanDtoDescriptorBuilder setRenderingPattern(String pattern);

	/**
	 * Sets an icon descriptor that will be used to render the bean.
	 * 
	 * To make that work in the ui, there must be an icon resolver registered in the ui
	 * for that descriptor type.
	 * 
	 * @param iconDescriptor The descriptor to set
	 * 
	 * @return The icon descriptor or null
	 */
	IBeanDtoDescriptorBuilder setIconDescriptor(Object iconDescriptor);

	/**
	 * Sets an icon descriptor that will be used to render the creator action.
	 * 
	 * To make that work in the ui, there must be an icon resolver registered in the ui
	 * for that descriptor type.
	 * 
	 * @param iconDescriptor The descriptor to set
	 * 
	 * @return The icon descriptor or null
	 */
	IBeanDtoDescriptorBuilder setCreateIconDescriptor(Object iconDescriptor);

	/**
	 * Sets an icon descriptor that will be used to render the delete action.
	 * 
	 * To make that work in the ui, there must be an icon resolver registered in the ui
	 * for that descriptor type.
	 * 
	 * @param iconDescriptor The descriptor to set
	 * 
	 * @return The icon descriptor or null
	 */
	IBeanDtoDescriptorBuilder setDeleteIconDescriptor(Object iconDescriptor);

	IBeanDtoDescriptorBuilder addValidator(IBeanValidator<?> validator);

	IBeanDtoDescriptorBuilder setValidators(Collection<? extends IBeanValidator<?>> validators);

	IBeanDtoDescriptorBuilder setProperties(Collection<? extends IProperty> properties);

	IBeanPropertyBluePrint addProperty(String propertyName);

	IBeanDtoDescriptor build();

}
