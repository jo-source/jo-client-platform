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

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.service.jpa.api.query.IPredicateCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

public class ParentLinkPredicateCreator<PARAMETER_TYPE> implements IPredicateCreator<PARAMETER_TYPE> {

	private final List<String> propertyPath;
	private final boolean linked;

	public ParentLinkPredicateCreator(final boolean linked, final List<String> propertyPath) {
		Assert.paramNotEmpty(propertyPath, "propertyPath");
		this.propertyPath = propertyPath;
		this.linked = linked;
	}

	@Override
	public Predicate createPredicate(
		final CriteriaBuilder criteriaBuilder,
		final Root<?> bean,
		final CriteriaQuery<?> query,
		final List<IBeanKey> parentBeanKeys,
		final List<Object> parentBeanIds,
		final PARAMETER_TYPE parameter) {

		if (EmptyCheck.isEmpty(parentBeanIds)) {
			return bean.get(IBean.ID_PROPERTY).in(-1);
		}
		else if (linked) {
			return getParentPath(bean).get(IBean.ID_PROPERTY).in(parentBeanIds);
		}
		else {
			@SuppressWarnings("unchecked")
			final Class<Object> javaType = (Class<Object>) bean.getJavaType();
			final Subquery<Object> subquery = query.subquery(javaType);
			final Root<Object> subqueryRoot = subquery.from(javaType);
			subquery.select(subqueryRoot.get(IBean.ID_PROPERTY));
			subquery.where(getParentPath(subqueryRoot).get(IBean.ID_PROPERTY).in(parentBeanIds));
			return criteriaBuilder.not(bean.get(IBean.ID_PROPERTY).in(subquery));
		}
	}

	private Path<?> getParentPath(final Root<?> bean) {
		Path<?> parentPath = bean;
		ManagedType<?> type = bean.getModel();
		for (final String propertyName : propertyPath) {
			if (!IBean.ID_PROPERTY.equals(propertyName)) {
				final Attribute<?, ?> attribute;
				if (type != null) {
					attribute = type.getAttribute(propertyName);
					if (attribute != null) {
						type = attribute.getDeclaringType();
					}
				}
				else {
					attribute = null;
				}
				if (attribute != null && attribute.isCollection() && parentPath instanceof From) {
					final From<?, ?> from = (From<?, ?>) parentPath;
					parentPath = from.join(propertyName);
				}
				else {
					parentPath = parentPath.get(propertyName);
				}
			}
		}
		return parentPath;
	}
}
