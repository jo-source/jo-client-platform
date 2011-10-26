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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilter;
import org.jowidgets.cap.ui.api.attribute.IAttributeFilterFactory;
import org.jowidgets.util.Assert;

final class AttributeFilterFactoryImpl implements IAttributeFilterFactory {

	private static final IAttributeFilter ACCEPT_EDIATBLE_FILTER = createAcceptEditableFilter();

	AttributeFilterFactoryImpl() {}

	private static IAttributeFilter createAcceptEditableFilter() {
		return new IAttributeFilter() {
			@Override
			public boolean accept(final IAttribute<?> attribute) {
				return attribute.isEditable() && !attribute.isReadonly();
			}
		};
	}

	@Override
	public IAttributeFilter acceptEdiableFilter() {
		return ACCEPT_EDIATBLE_FILTER;
	}

	@Override
	public IAttributeFilter whiteListFilter(final String... propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		return new ListFilter(true, propertyNames);
	}

	@Override
	public IAttributeFilter whiteListFilter(final Collection<String> propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		return new ListFilter(true, propertyNames);
	}

	@Override
	public IAttributeFilter blackListFilter(final String... propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		return new ListFilter(false, propertyNames);
	}

	@Override
	public IAttributeFilter blackListFilter(final Collection<String> propertyNames) {
		Assert.paramNotNull(propertyNames, "propertyNames");
		return new ListFilter(false, propertyNames);
	}

	private static final class ListFilter implements IAttributeFilter {

		private final Set<String> propertyNames;
		private final boolean whiteListFilter;

		private ListFilter(final boolean white, final String... propertyNames) {
			this(white, Arrays.asList(propertyNames));
		}

		private ListFilter(final boolean white, final Collection<String> propertyNames) {
			this.propertyNames = new HashSet<String>(propertyNames);
			this.whiteListFilter = white;
		}

		@Override
		public boolean accept(final IAttribute<?> attribute) {
			if (propertyNames.contains(attribute.getPropertyName())) {
				return whiteListFilter;
			}
			else {
				return !whiteListFilter;
			}
		}
	}

}
