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

package org.jowidgets.cap.ui.api.filter;

import java.util.List;

import org.jowidgets.cap.ui.api.attribute.IAttribute;

public interface IUiFilterTools {

	/**
	 * Adds a filter to the source filter. If the source filter is null, the added filter will be returned.
	 * If the source filter is a boolean filter, the added filter will be added to the list of filter of the source filter
	 * If the source filter is not a boolean filter, a boolean (and) filter will be returned,
	 * that has the source filter and added filter as arguments
	 * 
	 * @param sourceFilter
	 * @param addedFilter
	 * 
	 * @return A new filter where the addedFilter has been added to the source filter
	 */
	IUiFilter addFilter(IUiFilter sourceFilter, IUiFilter addedFilter);

	/**
	 * Removes all predicates from the filter with the given attribute
	 * 
	 * @param sourceFilter The filter where the properties should be removed
	 * @param propertyName The property to remove
	 * 
	 * @return A new filter where the predicates of the given property has been removed. Null will be returned if
	 *         the source filter only consists of predicates for the given property name
	 */
	IUiFilter removeProperty(IUiFilter sourceFilter, String propertyName);

	/**
	 * Inverts the source filter
	 * 
	 * @param sourceFilter The filter to invert
	 * 
	 * @return A new filter that is the inverted source filter
	 */
	IUiFilter invert(IUiFilter sourceFilter);

	/**
	 * Determines if a property is part of the filter
	 * 
	 * @param sourceFilter The filter to search for the property
	 * @param propertyName The property to search for
	 * 
	 * @return True if the property is part of the filter, false otherwise
	 */
	boolean isPropertyFiltered(IUiFilter sourceFilter, String propertyName);

	/**
	 * Determines if a filter has property filters except the given ones
	 * 
	 * @param sourceFilter The filter to search for property filters
	 * @param ignoredProperties The property that should not be considered
	 * 
	 * @return True if property filters except the ignored filters exists, false otherwise
	 */
	boolean hasPropertyFilters(IUiFilter sourceFilter, String... ignoredProperties);

	/**
	 * Gets a human readable string representation of the given filter.
	 * 
	 * @param filter The filter to get the string representation for
	 * @param attributes The attributes the filter terms may use
	 * 
	 * @return A human readable string representation
	 */
	String toHumanReadable(IUiFilter filter, List<IAttribute<?>> attributes);
}
