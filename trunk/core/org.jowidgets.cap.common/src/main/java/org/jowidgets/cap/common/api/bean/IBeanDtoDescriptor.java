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

import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;

public interface IBeanDtoDescriptor {

	/**
	 * Gets the type of the bean. If no type is defined, IBeanDto.class is the default
	 * type.
	 * 
	 * @return The type of the bean, never null. The default type is IBeanDto.class
	 */
	Class<?> getBeanType();

	/**
	 * @return The properties of the bean in the preferred order
	 */
	List<IProperty> getProperties();

	/**
	 * @return The default sorting of the bean
	 */
	List<ISort> getDefaultSorting();

	/**
	 * Gets the validators for the bean
	 * 
	 * @return The validators or an empty list, never null
	 */
	Set<IBeanValidator<?>> getValidators();

	/**
	 * Gets the label in the singular, e.g. Role, Authentication, Member, ...
	 * 
	 * @return The label in the singular or null
	 */
	String getLabelSingular();

	/**
	 * Gets the label in the plural, e.g. Roles, Authentications, Members, ...
	 * 
	 * @return The label in the plural or null
	 */
	String getLabelPlural();

	/**
	 * Gets a pattern that could be used to render instances of the bean.
	 * The pattern may contain property variables starting and ending with $.
	 * The $ symbol could be escaped with a $ symbol
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
	String getRenderingPattern();

	/**
	 * Gets the description
	 * 
	 * @return The decription or null
	 */
	String getDescription();

}
