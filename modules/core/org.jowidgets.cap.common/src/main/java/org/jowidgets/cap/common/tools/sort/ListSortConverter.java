/*
 * Copyright (c) 2015, grossmann
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

package org.jowidgets.cap.common.tools.sort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jowidgets.util.Assert;
import org.jowidgets.util.IConverter;

public class ListSortConverter<VALUE_TYPE> implements IConverter<VALUE_TYPE, Integer> {

	private final Map<VALUE_TYPE, Integer> sortMapping;

	public ListSortConverter(final VALUE_TYPE[] values) {
		this(Arrays.asList(Assert.getParamNotNull(values, "values")));
	}

	public ListSortConverter(final List<VALUE_TYPE> values) {
		Assert.paramNotNull(values, "values");
		this.sortMapping = createSortMapping(values);
	}

	private Map<VALUE_TYPE, Integer> createSortMapping(final List<VALUE_TYPE> values) {
		final Map<VALUE_TYPE, Integer> result = new HashMap<VALUE_TYPE, Integer>();
		int order = 0;
		for (final VALUE_TYPE value : values) {
			result.put(value, Integer.valueOf(order));
			order++;
		}
		return result;
	}

	@Override
	public final Integer convert(final VALUE_TYPE source) {
		if (source != null) {
			final Integer result = sortMapping.get(source);
			if (result != null) {
				return result;
			}
			else {
				return Integer.MIN_VALUE;
			}
		}
		else {
			return null;
		}
	}
}
