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

package org.jowidgets.cap.ui.api.attribute;

import java.util.Collection;

public interface IAttributeCollectionModifierBuilder {

	IAttributeCollectionModifierBuilder addFilter(IAttributeFilter filter);

	IAttributeCollectionModifierBuilder addBlackListFilter(Collection<String> propertyNames);

	IAttributeCollectionModifierBuilder addBlackListFilter(String... propertyNames);

	IAttributeCollectionModifierBuilder addWhiteListFilter(Collection<String> propertyNames);

	IAttributeCollectionModifierBuilder addWhiteListFilter(String... propertyNames);

	IAttributeCollectionModifierBuilder addAcceptEditableAttributesFilter();

	IAttributeCollectionModifierBuilder addDefaultModifier(IAttributeModifier<?> modifier);

	/**
	 * Adds a modifier. The modification that should be done can be made on the returned IAttributeBluePrint
	 * 
	 * @return A bluePrint to make the modifications on.
	 */
	IAttributeBluePrint<Object> addDefaultModifier();

	IAttributeCollectionModifierBuilder addDefaultEditableModifier(boolean editable);

	IAttributeCollectionModifierBuilder addDefaultVisibleModifier(boolean visible);

	<ELEMENT_VALUE_TYPE> IAttributeCollectionModifierBuilder addModifier(
		String propertyName,
		IAttributeModifier<ELEMENT_VALUE_TYPE> modifier);

	/**
	 * Adds a modifier. The modification that should be done can be made on the returned IAttributeBluePrint
	 * 
	 * @param <ELEMENT_VALUE_TYPE>
	 * @param propertyName The property to add the modifier for
	 * @return A bluePrint to make the modifications on.
	 */
	<ELEMENT_VALUE_TYPE> IAttributeBluePrint<ELEMENT_VALUE_TYPE> addModifier(String propertyName);

	boolean isModified();

	IAttributeCollectionModifier build();

}
