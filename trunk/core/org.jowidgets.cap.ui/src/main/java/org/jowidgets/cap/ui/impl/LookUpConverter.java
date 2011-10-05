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

import org.jowidgets.api.convert.IConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.lookup.ILookUpProperty;
import org.jowidgets.common.mask.ITextMask;
import org.jowidgets.common.verify.IInputVerifier;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

final class LookUpConverter<KEY_TYPE> implements IConverter<KEY_TYPE> {

	private final IConverter<Object> valueConverter;

	LookUpConverter(final Object lookUpId, final ILookUpProperty lookUpProperty) {
		this(lookUpId, lookUpProperty, Toolkit.getConverterProvider().getConverter(lookUpProperty.getValueType()));
	}

	@SuppressWarnings("unchecked")
	LookUpConverter(final Object lookUpId, final ILookUpProperty lookUpProperty, final IConverter<?> valueConverter) {
		Assert.paramNotNull(lookUpId, "lookUpId");
		Assert.paramNotNull(lookUpProperty, "lookUpProperty");
		Assert.paramNotNull(valueConverter, "valueConverter");
		this.valueConverter = (IConverter<Object>) valueConverter;
	}

	@Override
	public KEY_TYPE convertToObject(final String string) {
		//TODO MG implement convertToObject
		return null;
	}

	@Override
	public String convertToString(final KEY_TYPE value) {
		//TODO MG implement convertToString
		return null;
	}

	@Override
	public String getDescription(final KEY_TYPE value) {
		//TODO MG implement getDescription
		return null;
	}

	@Override
	public IValidator<String> getStringValidator() {
		return valueConverter.getStringValidator();
	}

	@Override
	public IInputVerifier getInputVerifier() {
		return valueConverter.getInputVerifier();
	}

	@Override
	public String getAcceptingRegExp() {
		return valueConverter.getAcceptingRegExp();
	}

	@Override
	public ITextMask getMask() {
		return valueConverter.getMask();
	}

}
