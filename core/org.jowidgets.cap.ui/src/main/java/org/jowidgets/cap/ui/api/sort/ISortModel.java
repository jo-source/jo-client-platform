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

package org.jowidgets.cap.ui.api.sort;

import java.util.List;

import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.util.event.IChangeObservable;

public interface ISortModel extends ISortModelConfig, IChangeObservable {

	/**
	 * After the method was invoked, no changed events will be fired by the model on modifications, until
	 * modification end was invoked.
	 * 
	 * @see #modelModificationEnd()
	 */
	void modelModificationStart();

	/**
	 * Marks that a model modification was completed. If the model state changed after modelModificationStart
	 * was invoked, a changed event will be fired.
	 * 
	 * @see #modelModificationStart()
	 */
	void modelModificationEnd();

	/**
	 * Gets the sorting combined by current sorting and default sorting.
	 * 
	 * The current sorting overrides the default sorting.
	 * 
	 * @return The sorting
	 */
	List<ISort> getSorting();

	void setConfig(ISortModelConfig config);

	ISortModelConfig getConfig();

	void setSorting(List<ISort> defaultSorting, List<ISort> currentSorting);

	void setCurrentSorting(List<ISort> sorting);

	void setDefaultSorting(List<ISort> sorting);

	void clearSorting();

	void clearCurrentSorting();

	void clearDefaultSorting();

	/**
	 * Sets a property in the current sorting. All other current properties in the model will be removed.
	 * 
	 * @param propertyName The property to set.
	 */
	void setCurrentProperty(String propertyName, SortOrder order);

	/**
	 * Adds a property to the current sorting.
	 * 
	 * @param propertyName The property to add
	 */
	void addCurrentProperty(String propertyName, SortOrder order);

	/**
	 * Sets a property in the default sorting. All other default properties in the model will be removed.
	 * 
	 * @param propertyName The property to set.
	 */
	void setDefaultProperty(String propertyName, SortOrder order);

	/**
	 * Adds a property to the default sorting.
	 * 
	 * @param propertyName The property to add
	 */
	void addDefaultProperty(String propertyName, SortOrder order);

	/**
	 * Sets a property in the current sorting. All other current properties in the model will be removed.
	 * If the property is already set,
	 * it will be toggled. The toggle sequence is: ASC, DESC, Not sorted, ASC, ...
	 * 
	 * @param propertyName The property to set.
	 */
	void setOrToggleCurrentProperty(String propertyName);

	/**
	 * Adds a property to the current sorting. If the property is already set,
	 * it will be toggled. The toggle sequence is: ASC, DESC, Not sorted, ASC, ...
	 * 
	 * @param propertyName The property to add.
	 */
	void addOrToggleCurrentProperty(String propertyName);

	/**
	 * Gets the sort info for a defined propery name
	 * 
	 * @param propertyName The property name to get the sort info for
	 * 
	 * @return The sort info, never null
	 */
	IPropertySort getPropertySort(String propertyName);

}
