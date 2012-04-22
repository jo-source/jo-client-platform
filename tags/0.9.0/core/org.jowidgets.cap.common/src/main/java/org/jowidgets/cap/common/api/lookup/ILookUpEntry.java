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

package org.jowidgets.cap.common.api.lookup;

public interface ILookUpEntry {

	/**
	 * @return The key of the look up
	 */
	Object getKey();

	/**
	 * Gets the value for a given look up value property
	 * 
	 * @param propertyName The (value) property to get the value for
	 * 
	 * @return The value
	 */
	Object getValue(String propertyName);

	/**
	 * Gets the value for the default property
	 * 
	 * @return The value for the default property
	 */
	Object getValue();

	/**
	 * Gets a description for the entry, may be null
	 * 
	 * @return The description of the entry or null, if no description is available
	 */
	String getDescription();

	/**
	 * If the entry is not valid, it can not be selected by the user however it
	 * can be displayed.
	 * 
	 * E.g. DM or Lira may be a invalid currency because they should not used for
	 * new data sets. However there are old data sets, where this currencies was
	 * valid and should be displayed.
	 * 
	 * @return true id the entry is valid, false otherwise
	 */
	boolean isValid();
}
