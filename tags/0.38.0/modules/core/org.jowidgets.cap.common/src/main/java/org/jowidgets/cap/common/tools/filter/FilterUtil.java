/*
 * Copyright (c) 2014, MGrossmann
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

package org.jowidgets.cap.common.tools.filter;

import java.util.HashSet;

import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IArithmeticPropertyFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.ICustomFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Nothing;
import org.jowidgets.util.maybe.Some;

public final class FilterUtil {

	private FilterUtil() {}

	public static boolean equals(final IFilter filter1, final IFilter filter2) {
		return NullCompatibleEquivalence.equals(filter1, filter2);
	}

	public static boolean isEmpty(final IFilter filter) {
		if (filter == null) {
			return true;
		}
		else if (filter instanceof IBooleanFilter) {
			return EmptyCheck.isEmpty(((IBooleanFilter) filter).getFilters());
		}
		return false;
	}

	/**
	 * Predicts if a new filter will have less or equal results than an old filter
	 * 
	 * @param oldFilter
	 * @param newFilter
	 * 
	 * @return True if newFilter has less or equals results than oldFilter, and Nothing if result is not predictable
	 */
	public static IMaybe<Boolean> hasLessOrEqualResults(final IFilter oldFilter, final IFilter newFilter) {
		if (isEmpty(oldFilter) && isEmpty(newFilter)) {
			return new Some<Boolean>(Boolean.TRUE);
		}
		else if (isEmpty(oldFilter)) {//new filter is not empty here
			return new Some<Boolean>(Boolean.TRUE);
		}
		else if (isEmpty(newFilter)) {//old filter is not empty here
			return Nothing.getInstance();
		}
		else if (oldFilter instanceof IBooleanFilter && newFilter instanceof IBooleanFilter) {
			return hasLessOrEqualResults((IBooleanFilter) oldFilter, (IBooleanFilter) newFilter);
		}
		else if (oldFilter instanceof IArithmeticFilter && newFilter instanceof IArithmeticFilter) {
			return hasLessOrEqualResults((IArithmeticFilter) oldFilter, (IArithmeticFilter) newFilter);
		}
		else if (oldFilter instanceof IArithmeticPropertyFilter && newFilter instanceof IArithmeticPropertyFilter) {
			return hasLessOrEqualResults((IArithmeticPropertyFilter) oldFilter, (IArithmeticPropertyFilter) newFilter);
		}
		else if (oldFilter instanceof ICustomFilter && newFilter instanceof ICustomFilter) {
			return hasLessOrEqualResults((ICustomFilter) oldFilter, (ICustomFilter) newFilter);
		}
		else {
			return Nothing.getInstance();
		}
	}

	public static IMaybe<Boolean> hasLessOrEqualResults(final IBooleanFilter oldFilter, final IBooleanFilter newFilter) {
		Assert.paramNotNull(oldFilter, "oldFilter");
		Assert.paramNotNull(newFilter, "newFilter");
		if (oldFilter.equals(newFilter)) {
			return new Some<Boolean>(Boolean.TRUE);
		}
		else if (oldFilter.getOperator() == BooleanOperator.OR && newFilter.getOperator() == BooleanOperator.OR) {

			final HashSet<IFilter> oldFilterChildren = new HashSet<IFilter>(oldFilter.getFilters());
			final HashSet<IFilter> newFilterChildren = new HashSet<IFilter>(newFilter.getFilters());

			//test if all conditions of the new filter are part of the old filter
			for (final IFilter newFilterChild : newFilter.getFilters()) {
				if (oldFilterChildren.remove(newFilterChild)) {
					newFilterChildren.remove(newFilterChild);
				}
			}

			if (newFilterChildren.size() == 0) {
				//all conditions of the new filter are part of the old filter
				return new Some<Boolean>(Boolean.TRUE);
			}
			else if (oldFilterChildren.size() == 1 && newFilterChildren.size() == 1) {
				//they differ by one condition, so check this condition
				return (hasLessOrEqualResults(oldFilterChildren.iterator().next(), newFilterChildren.iterator().next()));
			}
			else {
				//TODO add more complex tests here
				return Nothing.getInstance();
			}
		}
		else if (oldFilter.getOperator() == BooleanOperator.AND && newFilter.getOperator() == BooleanOperator.AND) {

			final HashSet<IFilter> oldFilterChildren = new HashSet<IFilter>(oldFilter.getFilters());
			final HashSet<IFilter> newFilterChildren = new HashSet<IFilter>(newFilter.getFilters());

			//test if all conditions of the old filter are part of the new filter
			for (final IFilter newFilterChild : newFilter.getFilters()) {
				if (newFilterChildren.remove(newFilterChild)) {
					oldFilterChildren.remove(newFilterChild);
				}
			}

			if (oldFilterChildren.size() == 0) {
				//all conditions of the old filter are part of the new filter
				return new Some<Boolean>(Boolean.TRUE);
			}
			else if (oldFilterChildren.size() == 1 && newFilterChildren.size() == 1) {
				//they differ by one condition, so check this condition
				return (hasLessOrEqualResults(oldFilterChildren.iterator().next(), newFilterChildren.iterator().next()));
			}
			else {
				//TODO add more complex tests here
				return Nothing.getInstance();
			}
		}
		else {//if operators changed, it is not predictable
			return Nothing.getInstance();
		}
	}

	public static IMaybe<Boolean> hasLessOrEqualResults(final IArithmeticFilter oldFilter, final IArithmeticFilter newFilter) {
		Assert.paramNotNull(oldFilter, "oldFilter");
		Assert.paramNotNull(newFilter, "newFilter");

		if (oldFilter.equals(newFilter)) {
			return new Some<Boolean>(Boolean.TRUE);
		}
		else if (oldFilter.getOperator() != newFilter.getOperator()) {
			return Nothing.getInstance();
		}
		else if (!NullCompatibleEquivalence.equals(oldFilter.getPropertyName(), newFilter.getPropertyName())) {
			return Nothing.getInstance();
		}
		else {
			//same property, same operator but not equal
			//TODO decide for each operator individual with help of the Arithmetic filter implementation specification
			return Nothing.getInstance();
		}

	}

	public static IMaybe<Boolean> hasLessOrEqualResults(
		final IArithmeticPropertyFilter oldFilter,
		final IArithmeticPropertyFilter newFilter) {

		Assert.paramNotNull(oldFilter, "oldFilter");
		Assert.paramNotNull(newFilter, "newFilter");

		if (oldFilter.equals(newFilter)) {
			return new Some<Boolean>(Boolean.TRUE);
		}
		else {
			return Nothing.getInstance();
		}
	}

	public static IMaybe<Boolean> hasLessOrEqualResults(final ICustomFilter oldFilter, final ICustomFilter newFilter) {
		Assert.paramNotNull(oldFilter, "oldFilter");
		Assert.paramNotNull(newFilter, "newFilter");

		if (oldFilter.equals(newFilter)) {
			return new Some<Boolean>(Boolean.TRUE);
		}
		else {
			return Nothing.getInstance();
		}
	}
}
