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

import java.util.Collection;
import java.util.Date;

import org.jowidgets.cap.common.api.bean.ILookUpValueRange;
import org.jowidgets.cap.common.api.bean.IStaticValueRange;
import org.jowidgets.cap.common.api.bean.IValueRange;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.ui.api.filter.IOperatorProvider;
import org.jowidgets.util.Assert;

final class DefaultOperatorProvider {

	static final IOperatorProvider<ArithmeticOperator> STRING = string();
	static final IOperatorProvider<ArithmeticOperator> STRING_COLLECTION = stringCollection();
	static final IOperatorProvider<ArithmeticOperator> NUMBER = number();
	static final IOperatorProvider<ArithmeticOperator> NUMBER_COLLECTION = numberCollection();
	static final IOperatorProvider<ArithmeticOperator> DATE = date();
	static final IOperatorProvider<ArithmeticOperator> DATE_COLLECTION = dateCollection();
	static final IOperatorProvider<ArithmeticOperator> BOOL = bool();
	static final IOperatorProvider<ArithmeticOperator> BOOL_PRIMITIVE = boolPrimitive();
	static final IOperatorProvider<ArithmeticOperator> LOOK_UP = lookUp();
	static final IOperatorProvider<ArithmeticOperator> LOOK_UP_COLLECTION = lookUpCollection();

	static final IOperatorProvider<ArithmeticOperator> STRING_PROPERTY = stringProperty();
	static final IOperatorProvider<ArithmeticOperator> STRING_COLLECTION_PROPERTY = stringCollectionProperty();
	static final IOperatorProvider<ArithmeticOperator> NUMBER_PROPERTY = numberProperty();
	static final IOperatorProvider<ArithmeticOperator> NUMBER_COLLECTION_PROPERTY = numberCollectionProperty();
	static final IOperatorProvider<ArithmeticOperator> DATE_PROPERTY = dateProperty();
	static final IOperatorProvider<ArithmeticOperator> DATE_COLLECTION_PROPERTY = dateCollectionProperty();
	static final IOperatorProvider<ArithmeticOperator> BOOL_PROPERTY = boolProperty();
	static final IOperatorProvider<ArithmeticOperator> LOOK_UP_PROPERTY = lookUpProperty();
	static final IOperatorProvider<ArithmeticOperator> LOOK_UP_COLLECTION_PROPERTY = lookUpCollectionProperty();

	private DefaultOperatorProvider() {}

	static IOperatorProvider<ArithmeticOperator> getArithmeticOperatorProvider(
		final Class<?> type,
		final Class<?> elementValueType,
		final IValueRange valueRange) {
		Assert.paramNotNull(type, "type");
		Assert.paramNotNull(valueRange, "valueRange");
		if (Collection.class.isAssignableFrom(type)) {
			Assert.paramNotNull(elementValueType, "elementValueType");
			if (isLookUp(valueRange)) {
				return LOOK_UP_COLLECTION;
			}
			else if (String.class.isAssignableFrom(elementValueType)) {
				return STRING_COLLECTION;
			}
			else if (Number.class.isAssignableFrom(elementValueType)) {
				return NUMBER_COLLECTION;
			}
			else if (Date.class.isAssignableFrom(elementValueType)) {
				return DATE_COLLECTION;
			}
			else if (Boolean.class.isAssignableFrom(elementValueType)) {
				return BOOL;
			}
			else if (boolean.class.isAssignableFrom(elementValueType)) {
				return BOOL;
			}
		}
		else {
			if (isLookUp(valueRange)) {
				return LOOK_UP;
			}
			else if (String.class.isAssignableFrom(type)) {
				return STRING;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return NUMBER;
			}
			else if (Date.class.isAssignableFrom(type)) {
				return DATE;
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return BOOL;
			}
			else if (boolean.class.isAssignableFrom(type)) {
				return BOOL;
			}
		}
		return null;
	}

	static IOperatorProvider<ArithmeticOperator> getArithmeticPropertyOperatorProvider(
		final Class<?> type,
		final Class<?> elementValueType,
		final IValueRange valueRange) {
		Assert.paramNotNull(type, "type");
		Assert.paramNotNull(valueRange, "valueRange");
		if (Collection.class.isAssignableFrom(type)) {
			Assert.paramNotNull(elementValueType, "elementValueType");
			if (isLookUp(valueRange)) {
				return LOOK_UP_COLLECTION_PROPERTY;
			}
			else if (String.class.isAssignableFrom(elementValueType)) {
				return STRING_COLLECTION_PROPERTY;
			}
			else if (Number.class.isAssignableFrom(elementValueType)) {
				return NUMBER_COLLECTION_PROPERTY;
			}
			else if (Date.class.isAssignableFrom(elementValueType)) {
				return DATE_COLLECTION_PROPERTY;
			}
			else if (Boolean.class.isAssignableFrom(elementValueType)) {
				return BOOL_PROPERTY;
			}
			else if (boolean.class.isAssignableFrom(elementValueType)) {
				return BOOL_PROPERTY;
			}
		}
		else {
			if (isLookUp(valueRange)) {
				return LOOK_UP_PROPERTY;
			}
			else if (String.class.isAssignableFrom(type)) {
				return STRING_PROPERTY;
			}
			else if (Number.class.isAssignableFrom(type)) {
				return NUMBER_PROPERTY;
			}
			else if (Date.class.isAssignableFrom(type)) {
				return DATE_PROPERTY;
			}
			else if (Boolean.class.isAssignableFrom(type)) {
				return BOOL_PROPERTY;
			}
			else if (boolean.class.isAssignableFrom(type)) {
				return BOOL_PROPERTY;
			}
		}
		return null;
	}

	private static boolean isLookUp(final IValueRange valueRange) {
		if (valueRange instanceof ILookUpValueRange) {
			return true;
		}
		else if (valueRange instanceof IStaticValueRange) {
			final IStaticValueRange staticValueRange = (IStaticValueRange) valueRange;
			return !staticValueRange.isOpen() && !staticValueRange.getValues().isEmpty();
		}
		return false;
	}

	private static IOperatorProvider<ArithmeticOperator> string() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> stringCollection() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> number() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> numberCollection() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> date() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.BETWEEN, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> dateCollection() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.BETWEEN, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> bool() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL);
	}

	private static IOperatorProvider<ArithmeticOperator> boolPrimitive() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL);
	}

	private static IOperatorProvider<ArithmeticOperator> lookUp() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> lookUpCollection() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EMPTY,
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> stringProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> stringCollectionProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> numberProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> numberCollectionProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> dateProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> dateCollectionProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.GREATER,
			ArithmeticOperator.GREATER_EQUAL,
			ArithmeticOperator.LESS,
			ArithmeticOperator.LESS_EQUAL,
			ArithmeticOperator.BETWEEN,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

	private static IOperatorProvider<ArithmeticOperator> boolProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL);
	}

	private static IOperatorProvider<ArithmeticOperator> lookUpProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY);
	}

	private static IOperatorProvider<ArithmeticOperator> lookUpCollectionProperty() {
		return new OperatorProvider<ArithmeticOperator>(ArithmeticOperator.EQUAL, //default
			ArithmeticOperator.EQUAL,
			ArithmeticOperator.CONTAINS_ANY,
			ArithmeticOperator.CONTAINS_ALL);
	}

}
