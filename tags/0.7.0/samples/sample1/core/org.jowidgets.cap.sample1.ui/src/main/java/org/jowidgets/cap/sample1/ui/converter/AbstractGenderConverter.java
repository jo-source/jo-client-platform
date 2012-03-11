/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

package org.jowidgets.cap.sample1.ui.converter;

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.tools.converter.MapConverter;

public abstract class AbstractGenderConverter {

	private static final String ERROR_MSG = "Must be '%1' or '%2'";

	private final IConverter<String> converter;

	public AbstractGenderConverter(final String male, final String female) {

		final Map<String, String> objectToString = new HashMap<String, String>();
		objectToString.put("M", male);
		objectToString.put("F", female);
		objectToString.put(null, "");

		final Map<String, String> stringToObject = new HashMap<String, String>();
		stringToObject.put(male, "M");
		stringToObject.put(male.toLowerCase(), "M");
		stringToObject.put(female, "F");
		stringToObject.put(female.toLowerCase(), "F");

		this.converter = new MapConverter<String>(objectToString, stringToObject, Toolkit.getMessageReplacer().replace(
				ERROR_MSG,
				male,
				female));
	}

	public IConverter<String> getConverter() {
		return converter;
	}

}
