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

public interface IProperty {

	/**
	 * @return The name of the property, never null
	 */
	String getName();

	/**
	 * @return The value range, never null
	 */
	IValueRange getValueRange();

	/**
	 * @return Gets the i18n default label of the property in a short version, never null
	 */
	String getLabelDefault();

	/**
	 * @return Gets the i18n default label of the property in a long version or null
	 */
	String getLabelLongDefault();

	/**
	 * @return Gets the i18n property description. This will be used e.g. for tool tips
	 */
	String getDescriptionDefault();

	/**
	 * @return True if the property should be visible by default, false otherwise
	 */
	boolean isVisibleDefault();

	/**
	 * @return True if this property is mandatory by default, false otherwise
	 */
	boolean isMandatoryDefault();

	/**
	 * 
	 * @return The type of the property, never null
	 */
	Class<?> getValueType();

	/**
	 * @return If the value type is a collection, the type of the elements will be returned, else the value type will be returned.
	 *         Result is never null an is never an instance of collection (wrapped collections must be unwrapped).
	 */
	Class<?> getElementValueType();

	/**
	 * Gets the (logical) cardinality of the property.
	 * 
	 * Remark: The logical cardinality may differ from the technical cardinality. E.g. for any technical reason,
	 * a Set is used to hold the property value, but the user want's a cardinality of <= 1 for user inputs. Then the
	 * 
	 * @link {@link IProperty#getValueType()} method returns 'Set.class' however the cardinality is LESS_OR_EQUAL_ONE.
	 * 
	 * @return The logical cardinality, never null
	 */
	Cardinality getCardinality();

	/**
	 * @return True if the property only has a getter, false otherwise
	 */
	boolean isReadonly();

	/**
	 * @return True if the property could be used in the sorting
	 */
	boolean isSortable();

	/**
	 * @return True if the property could be used in the filter
	 */
	boolean isFilterable();

}
