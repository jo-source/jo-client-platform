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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.annotation.BeanValidator;
import org.jowidgets.cap.common.api.annotation.ElementTypePropertyValidator;
import org.jowidgets.cap.common.api.annotation.PropertyValidator;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.validation.ElementTypeValidationDecorator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IFunction;
import org.jowidgets.util.IIterationCallback;
import org.jowidgets.util.reflection.IntrospectionCache;
import org.jowidgets.util.reflection.ReflectionUtils;
import org.jowidgets.validation.IValidator;

public final class ValidatorAnnotationUtil {

	private ValidatorAnnotationUtil() {}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <BEAN_TYPE> List<IBeanValidator<BEAN_TYPE>> getBeanValidators(final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		final Set<Class<? extends IBeanValidator>> validatorsSet = new LinkedHashSet<Class<? extends IBeanValidator>>();
		ReflectionUtils.iterateHierarchy(beanType, new IIterationCallback<Class<?>>() {
			@Override
			public void next(final Class<?> type) {
				final BeanValidator annotation = type.getAnnotation(BeanValidator.class);
				if (annotation != null) {
					final Class<? extends IBeanValidator<?>>[] value = annotation.value();
					for (final Class<? extends IBeanValidator> validator : value) {
						validatorsSet.add(validator);
					}
				}
			}
		});

		final List<IBeanValidator<BEAN_TYPE>> result = new LinkedList<IBeanValidator<BEAN_TYPE>>();
		for (final Class<? extends IBeanValidator> validator : validatorsSet) {
			try {
				result.add(validator.newInstance());
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static Map<String, Collection<IValidator<Object>>> getPropertyValidators(final Class<?> beanType) {
		final Map<String, Collection<IValidator<Object>>> result = new HashMap<String, Collection<IValidator<Object>>>();

		final PropertyValidatorFunction propertyValidatorFunction = new PropertyValidatorFunction();
		final ElementTypePropertyValidatorFunction elementPropertyValidatorFunction = new ElementTypePropertyValidatorFunction();

		ReflectionUtils.iterateHierarchy(beanType, new IIterationCallback<Class<?>>() {
			@Override
			public void next(final Class<?> type) {
				for (final PropertyDescriptor descriptor : IntrospectionCache.getPropertyDescriptors(type)) {
					final String propertyName = descriptor.getName();

					final Set<Class<? extends IValidator<Object>>> validators = getValidatorsFromProperty(
							descriptor,
							PropertyValidator.class,
							propertyValidatorFunction);
					addToMap(result, propertyName, createValidators(validators, PropertyValidator.class));

					final Set<Class<? extends IValidator<Object>>> elementValidators = getValidatorsFromProperty(
							descriptor,
							ElementTypePropertyValidator.class,
							elementPropertyValidatorFunction);
					addToMap(result, propertyName, createValidators(elementValidators, ElementTypePropertyValidator.class));
				}
				for (final Field field : type.getDeclaredFields()) {
					final String propertyName = field.getName();

					final Set<Class<? extends IValidator<Object>>> validators = getValidatorsFromField(
							beanType,
							field,
							PropertyValidator.class,
							propertyValidatorFunction);
					addToMap(result, propertyName, createValidators(validators, PropertyValidator.class));

					final Set<Class<? extends IValidator<Object>>> elementValidators = getValidatorsFromField(
							beanType,
							field,
							ElementTypePropertyValidator.class,
							elementPropertyValidatorFunction);
					addToMap(result, propertyName, createValidators(elementValidators, ElementTypePropertyValidator.class));
				}
			}
		});

		return result;
	}

	private static void addToMap(
		final Map<String, Collection<IValidator<Object>>> map,
		final String propertyName,
		final Collection<IValidator<Object>> augmention) {
		if (!EmptyCheck.isEmpty(augmention)) {
			Collection<IValidator<Object>> validators = map.get(propertyName);
			if (validators == null) {
				validators = new LinkedList<IValidator<Object>>();
				map.put(propertyName, validators);
			}
			validators.addAll(augmention);
		}
	}

	private static Collection<IValidator<Object>> createValidators(
		final Set<Class<? extends IValidator<Object>>> validators,
		final Class<? extends Annotation> anntotaion) {
		final List<IValidator<Object>> result = new LinkedList<IValidator<Object>>();
		for (final Class<? extends IValidator<Object>> validator : validators) {
			try {
				if (ElementTypePropertyValidator.class == anntotaion) {
					result.add(new ElementTypeValidationDecorator<Object>(validator.newInstance()));
				}
				else {
					result.add(validator.newInstance());
				}
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Set<Class<? extends IValidator<Object>>> getValidatorsFromField(
		final Class<?> beanType,
		final Field field,
		final Class<? extends Annotation> anntotaion,
		final IFunction<Class<? extends IValidator<?>>[], Object> validatorsFromAnnotation) {
		final Set result = new LinkedHashSet();
		result.addAll(getValidatorsFromAnnotaion(validatorsFromAnnotation, field.getAnnotation(anntotaion)));
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Set<Class<? extends IValidator<Object>>> getValidatorsFromProperty(
		final PropertyDescriptor propertyDescriptor,
		final Class<? extends Annotation> anntotaion,
		final IFunction<Class<? extends IValidator<?>>[], Object> validatorsFromAnnotation) {
		final Set result = new LinkedHashSet();
		if (propertyDescriptor != null) {
			result.addAll(getValidatorsFromMethod(propertyDescriptor.getReadMethod(), anntotaion, validatorsFromAnnotation));
			result.addAll(getValidatorsFromMethod(propertyDescriptor.getWriteMethod(), anntotaion, validatorsFromAnnotation));
		}
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Set<Class<? extends IValidator<Object>>> getValidatorsFromMethod(
		final Method method,
		final Class<? extends Annotation> anntotaion,
		final IFunction<Class<? extends IValidator<?>>[], Object> validatorsFromAnnotation) {
		final Set result = new LinkedHashSet();
		if (method != null) {
			result.addAll(getValidatorsFromAnnotaion(validatorsFromAnnotation, method.getAnnotation(anntotaion)));
		}
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Set<Class<? extends IValidator<Object>>> getValidatorsFromAnnotaion(
		final IFunction<Class<? extends IValidator<?>>[], Object> validatorsFromAnnotation,
		final Object annotation) {
		final Set result = new LinkedHashSet();
		if (annotation != null) {
			final Class<? extends IValidator<?>>[] value = validatorsFromAnnotation.invoke(annotation);
			for (final Class<? extends IValidator<?>> validator : value) {
				result.add(validator);
			}
		}
		return result;
	}

	private static class PropertyValidatorFunction implements IFunction<Class<? extends IValidator<?>>[], Object> {
		@Override
		public Class<? extends IValidator<?>>[] invoke(final Object argument) {
			return ((PropertyValidator) argument).value();
		}
	}

	private static class ElementTypePropertyValidatorFunction implements IFunction<Class<? extends IValidator<?>>[], Object> {
		@Override
		public Class<? extends IValidator<?>>[] invoke(final Object argument) {
			return ((ElementTypePropertyValidator) argument).value();
		}
	}

}
