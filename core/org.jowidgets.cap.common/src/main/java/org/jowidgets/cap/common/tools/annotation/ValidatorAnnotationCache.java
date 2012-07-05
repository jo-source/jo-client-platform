/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.common.tools.annotation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class ValidatorAnnotationCache {

	private static final Map<Class<?>, Map<String, Collection<IValidator<Object>>>> PROPERTY_VALIDATORS_CACHE = new ConcurrentHashMap<Class<?>, Map<String, Collection<IValidator<Object>>>>();
	private static final Map<Class<?>, List<IBeanValidator>> BEAN_VALIDATORS_CACHE = new ConcurrentHashMap<Class<?>, List<IBeanValidator>>();

	private ValidatorAnnotationCache() {}

	public static <BEAN_TYPE> List<IBeanValidator<BEAN_TYPE>> getBeanValidators(final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		return Collections.unmodifiableList(getBeanValidatorsCache(beanType));
	}

	public static Map<String, Collection<IValidator<Object>>> getPropertyValidators(final Class<?> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		return Collections.unmodifiableMap(getPropertyValidatorsCache(beanType));
	}

	public static Collection<IValidator<Object>> getPropertyValidators(final Class<?> beanType, final String propertyName) {
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(propertyName, "propertyName");
		final Map<String, Collection<IValidator<Object>>> validatorsCache = getPropertyValidatorsCache(beanType);
		Collection<IValidator<Object>> result = validatorsCache.get(propertyName);
		if (result == null) {
			result = Collections.emptyList();
			validatorsCache.put(propertyName, result);
		}
		return Collections.unmodifiableCollection(result);
	}

	private static Map<String, Collection<IValidator<Object>>> getPropertyValidatorsCache(final Class<?> beanType) {
		Map<String, Collection<IValidator<Object>>> result = PROPERTY_VALIDATORS_CACHE.get(beanType);
		if (result == null) {
			result = ValidatorAnnotationUtil.getPropertyValidators(beanType);
			PROPERTY_VALIDATORS_CACHE.put(beanType, result);
		}
		return result;
	}

	private static List getBeanValidatorsCache(final Class<?> beanType) {
		List result = BEAN_VALIDATORS_CACHE.get(beanType);
		if (result == null) {
			result = ValidatorAnnotationUtil.getBeanValidators(beanType);
			BEAN_VALIDATORS_CACHE.put(beanType, result);
		}
		return result;
	}
}
