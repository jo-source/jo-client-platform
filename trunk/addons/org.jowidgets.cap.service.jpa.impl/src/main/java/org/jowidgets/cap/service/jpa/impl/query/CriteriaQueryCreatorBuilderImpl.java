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

package org.jowidgets.cap.service.jpa.impl.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.ICustomFilterPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IPredicateCreator;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.util.Assert;

final class CriteriaQueryCreatorBuilderImpl<PARAMETER_TYPE> implements ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> {

	private final Class<? extends IBean> beanType;
	private final List<IPredicateCreator<PARAMETER_TYPE>> predicateCreators;
	private final Map<String, ICustomFilterPredicateCreator<PARAMETER_TYPE>> customFilterPredicateCreators;

	private IPredicateCreator<PARAMETER_TYPE> parentLinkPredicateCreator;

	private boolean caseSensitive;

	CriteriaQueryCreatorBuilderImpl(final Class<? extends IBean> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.beanType = beanType;
		this.caseSensitive = false;
		this.predicateCreators = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>();
		this.customFilterPredicateCreators = new HashMap<String, ICustomFilterPredicateCreator<PARAMETER_TYPE>>();
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyName(final String parentPropertyName) {
		Assert.paramNotEmpty(parentPropertyName, "parentPropertyName");
		return setParentPropertyPath(parentPropertyName);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyPath(final String... parentPropertyPath) {
		return setParentPropertyPath(true, parentPropertyPath);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyName(
		final boolean linked,
		final String parentPropertyName) {
		Assert.paramNotEmpty(parentPropertyName, "parentPropertyName");
		return setParentPropertyPath(linked, parentPropertyName);
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setParentPropertyPath(
		final boolean linked,
		final String... parentPropertyPath) {
		Assert.paramNotNull(parentPropertyPath, "parentPropertyPath");
		this.parentLinkPredicateCreator = new ParentLinkPredicateCreator<PARAMETER_TYPE>(
			linked,
			Arrays.asList(parentPropertyPath));
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> setCaseSensitve(final boolean caseSensitve) {
		this.caseSensitive = caseSensitve;
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addPredicateCreator(
		final IPredicateCreator<PARAMETER_TYPE> predicateCreator) {
		Assert.paramNotNull(predicateCreator, "predicateCreator");
		predicateCreators.add(predicateCreator);
		return this;
	}

	@Override
	public ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> addCustomFilterPredicateCreator(
		final String filterType,
		final ICustomFilterPredicateCreator<PARAMETER_TYPE> customFilterPredicateCreator) {
		Assert.paramNotNull(filterType, "filterType");
		Assert.paramNotNull(customFilterPredicateCreators, "customFilterPredicateCreators");
		customFilterPredicateCreators.put(filterType, customFilterPredicateCreator);
		return this;
	}

	@Override
	public IQueryCreator<PARAMETER_TYPE> build() {

		final List<IPredicateCreator<PARAMETER_TYPE>> predicateCreatorsComposite;
		predicateCreatorsComposite = new LinkedList<IPredicateCreator<PARAMETER_TYPE>>(predicateCreators);
		if (parentLinkPredicateCreator != null) {
			predicateCreatorsComposite.add(parentLinkPredicateCreator);
		}

		return new CriteriaQueryCreator<PARAMETER_TYPE>(
			beanType,
			caseSensitive,
			predicateCreatorsComposite,
			customFilterPredicateCreators);
	}

}
