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

package org.jowidgets.cap.sample2.app.service.lookup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.lookup.ILookUpEntry;
import org.jowidgets.cap.common.api.lookup.ILookUpEntryBuilder;
import org.jowidgets.cap.common.api.lookup.ILookUpToolkit;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRange;
import org.jowidgets.cap.common.api.lookup.ILookUpValueRangeBuilder;
import org.jowidgets.cap.service.api.adapter.ISyncLookUpService;

public final class GenderLookUpService implements ISyncLookUpService {

	public static final String LOOK_UP_ID = GenderLookUpService.class.getName() + "_id";

	public static final String MALE_KEY = "M";
	public static final String FEMALE_KEY = "F";

	private static final String SHORT_PROPERTY_NAME = "SHORT";
	private static final String LONG_PROPERTY_NAME = "LONG";

	private static List<ILookUpEntry> entries;

	private static ILookUpValueRange valueRange;

	@Override
	public List<ILookUpEntry> readValues(final IExecutionCallback executionCallback) {
		if (entries == null) {
			entries = createEntries();
		}
		return entries;
	}

	private static List<ILookUpEntry> createEntries() {
		final ILookUpToolkit lookUpToolkit = CapCommonToolkit.lookUpToolkit();
		final List<ILookUpEntry> result = new LinkedList<ILookUpEntry>();

		ILookUpEntryBuilder entryBuilder = lookUpToolkit.lookUpEntryBuilder();
		entryBuilder.setKey(MALE_KEY).setValue(SHORT_PROPERTY_NAME, "M").setValue(LONG_PROPERTY_NAME, "Male");
		result.add(entryBuilder.build());

		entryBuilder = lookUpToolkit.lookUpEntryBuilder();
		entryBuilder.setKey(FEMALE_KEY).setValue(SHORT_PROPERTY_NAME, "F").setValue(LONG_PROPERTY_NAME, "Female");
		result.add(entryBuilder.build());

		return Collections.unmodifiableList(result);
	}

	public static ILookUpValueRange valueRange() {
		if (valueRange == null) {
			valueRange = createValueRange();
		}
		return valueRange;
	}

	private static ILookUpValueRange createValueRange() {
		final ILookUpToolkit lookUpToolkit = CapCommonToolkit.lookUpToolkit();
		final ILookUpValueRangeBuilder builder = lookUpToolkit.lookUpValueRangeBuilder();
		builder.setLookUpId(LOOK_UP_ID);
		builder.setDefaultValuePropertyName(LONG_PROPERTY_NAME);
		builder.addValueProperty(lookUpToolkit.lookUpProperty(SHORT_PROPERTY_NAME, "Short"));
		builder.addValueProperty(lookUpToolkit.lookUpProperty(LONG_PROPERTY_NAME, "Long"));
		return builder.build();
	}
}
