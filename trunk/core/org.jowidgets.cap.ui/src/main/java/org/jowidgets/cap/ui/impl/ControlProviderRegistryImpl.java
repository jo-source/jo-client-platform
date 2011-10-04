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

package org.jowidgets.cap.ui.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.control.IInputControlProviderRegistry;
import org.jowidgets.cap.ui.api.control.IInputControlSupport;
import org.jowidgets.util.Assert;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ControlProviderRegistryImpl implements IInputControlProviderRegistry {

	private final Map<Class, IInputControlSupport> map;

	ControlProviderRegistryImpl() {
		this.map = new HashMap<Class, IInputControlSupport>();

		map.put(boolean.class, new ControlSupportBooleanPrimitive());
		map.put(Boolean.class, new ControlSupportBoolean());

		map.put(Date.class, new ControlSupportDate());

		//some default provider
		map.put(String.class, new ControlProviderDefault<String>(String.class));
		map.put(Long.class, new ControlProviderDefault<Long>(Long.class));
		map.put(Long.class, new ControlProviderDefault<Long>(long.class));
		map.put(Integer.class, new ControlProviderDefault<Integer>(Integer.class));
		map.put(Integer.class, new ControlProviderDefault<Integer>(int.class));
		map.put(Short.class, new ControlProviderDefault<Short>(Short.class));
		map.put(Short.class, new ControlProviderDefault<Short>(short.class));

	}

	@Override
	public <ELEMENT_VALUE_TYPE> IInputControlSupport<ELEMENT_VALUE_TYPE> getControls(
		final Class<? extends ELEMENT_VALUE_TYPE> type) {
		Assert.paramNotNull(type, "type");
		IInputControlSupport result = map.get(type);
		if (result == null && Toolkit.getConverterProvider().getConverter(type) != null) {
			result = new ControlProviderDefault<ELEMENT_VALUE_TYPE>(type);
		}
		return result;
	}

	@Override
	public <ELEMENT_VALUE_TYPE> void setControls(
		final Class<? extends ELEMENT_VALUE_TYPE> type,
		final IInputControlSupport<ELEMENT_VALUE_TYPE> controlSupport) {
		Assert.paramNotNull(type, "type");
		map.put(type, controlSupport);
	}

}
