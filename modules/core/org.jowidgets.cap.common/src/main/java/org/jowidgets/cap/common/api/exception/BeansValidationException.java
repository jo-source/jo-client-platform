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

package org.jowidgets.cap.common.api.exception;

import java.util.Map;

import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.util.Assert;

public class BeansValidationException extends ServiceException {

	private static final long serialVersionUID = -7579908469741974763L;

	public enum KeyType {

		/**
		 * The key type of the result map is the id of the bean, e.g. for update
		 */
		ID,

		/**
		 * The key type of the result map is the index of the bean in the collection of processed beans. This will be used, if ids
		 * are not available, e.g. when beans will be created
		 */
		INDEX;
	}

	private final Map<Object, IBeanValidationResult> validationResultMap;
	private final KeyType keyType;

	/**
	 * Creates a new instance
	 * 
	 * @param validationResultMap The validation results
	 * @param keyType The type of the keys in the map
	 */
	public BeansValidationException(final Map<Object, IBeanValidationResult> validationResultMap, final KeyType keyType) {
		Assert.paramNotEmpty(validationResultMap, "validationResultMap");
		Assert.paramNotNull(keyType, "keyType");
		this.validationResultMap = validationResultMap;
		this.keyType = keyType;
	}

	/**
	 * Gets the validation results
	 * 
	 * @return The validation results, never null
	 */
	public Map<Object, IBeanValidationResult> getValidationResults() {
		return validationResultMap;
	}

	/**
	 * Gets the key type of the result map
	 * 
	 * @return The key type, never null
	 */
	public KeyType getKeyType() {
		return keyType;
	}

}
