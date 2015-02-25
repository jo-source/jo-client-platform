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

package org.jowidgets.cap.common.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jowidgets.cap.common.api.annotation.SortConverter;
import org.jowidgets.cap.common.api.sort.ISortConverterMap;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IConverter;
import org.jowidgets.util.IIterationCallback;
import org.jowidgets.util.reflection.IntrospectionCache;
import org.jowidgets.util.reflection.ReflectionUtils;

final class SortConveterMapFactory {

	private SortConveterMapFactory() {}

	static ISortConverterMap create(final Class<?> beanType) {
		final Map<String, IConverter<?, ?>> converterMap = new HashMap<String, IConverter<?, ?>>();
		ReflectionUtils.iterateHierarchy(beanType, new IIterationCallback<Class<?>>() {
			@Override
			public void next(final Class<?> type) {
				converterMap.putAll(getConvertersFromMethods(type));
				converterMap.putAll(getConvertersFromFields(type));
			}
		});
		return new SortConverterMapImpl(converterMap);
	}

	private static Map<String, IConverter<?, ?>> getConvertersFromMethods(final Class<?> type) {
		final Map<String, IConverter<?, ?>> result = new HashMap<String, IConverter<?, ?>>();
		for (final PropertyDescriptor descriptor : IntrospectionCache.getPropertyDescriptors(type)) {
			final String propertyName = descriptor.getName();
			final IConverter<?, ?> converter = getConverterFromMethods(descriptor);
			if (converter != null) {
				result.put(propertyName, converter);
			}
		}
		return result;
	}

	private static Map<String, IConverter<?, ?>> getConvertersFromFields(final Class<?> type) {
		final Map<String, IConverter<?, ?>> result = new HashMap<String, IConverter<?, ?>>();
		for (final Field field : type.getDeclaredFields()) {
			final String propertyName = field.getName();
			final IConverter<?, ?> converter = getConverterFromAnnotation(field.getAnnotation(SortConverter.class));
			if (converter != null) {
				result.put(propertyName, converter);
			}
		}
		return result;
	}

	private static IConverter<?, ?> getConverterFromMethods(final PropertyDescriptor propertyDescriptor) {
		IConverter<?, ?> result = getConverterFromMethod(propertyDescriptor.getReadMethod());
		if (result == null) {
			result = getConverterFromMethod(propertyDescriptor.getWriteMethod());
		}
		return result;
	}

	private static IConverter<?, ?> getConverterFromMethod(final Method method) {
		if (method != null) {
			return getConverterFromAnnotation(method.getAnnotation(SortConverter.class));
		}
		return null;
	}

	private static IConverter<?, ?> getConverterFromAnnotation(final SortConverter converterAnnotation) {
		if (converterAnnotation != null) {
			try {
				return converterAnnotation.value().newInstance();
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private static final class SortConverterMapImpl implements ISortConverterMap {

		private final Map<String, IConverter<?, ?>> converterMap;

		private SortConverterMapImpl(final Map<String, IConverter<?, ?>> converterMap) {
			Assert.paramNotNull(converterMap, "converterMap");
			this.converterMap = converterMap;
		}

		@Override
		public IConverter<?, ?> getConverter(final String propertyName) {
			Assert.paramNotEmpty(propertyName, "propertyName");
			return converterMap.get(propertyName);
		}

		@Override
		public Collection<String> getProperties() {
			return converterMap.keySet();
		}

	}

}
