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
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.jpa.tools.entity.EntityManagerProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class ParentLinkPredicateCreator<PARAMETER_TYPE> {

	private final List<String> propertyPath;

	ParentLinkPredicateCreator(final List<String> propertyPath) {
		Assert.paramNotEmpty(propertyPath, "propertyPath");
		this.propertyPath = propertyPath;
	}

	Predicate createPredicate(final Root<?> bean, final List<Object> parentBeanIds) {
		if (EmptyCheck.isEmpty(parentBeanIds)) {
			final CriteriaBuilder criteriaBuilder = EntityManagerProvider.get().getCriteriaBuilder();
			return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.FALSE));
		}
		else {
			return getParentPath(bean).get(IBean.ID_PROPERTY).in(parentBeanIds);
		}
	}

	private Path<?> getParentPath(final Root<?> bean) {
		Path<?> parentPath = bean;
		Type<?> type = bean.getModel();
		for (final String propertyName : propertyPath) {
			if (!IBean.ID_PROPERTY.equals(propertyName)) {
				final Attribute<?, ?> attribute;
				if (type != null && type instanceof ManagedType) {
					attribute = ((ManagedType<?>) type).getAttribute(propertyName);
					type = getType(attribute);
				}
				else {
					attribute = null;
				}
				if (isJoinableType(attribute) && parentPath instanceof From) {
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

	private boolean isJoinableType(final Attribute<?, ?> attribute) {
		return attribute != null && attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC;
	}

	private Type<?> getType(final Attribute<?, ?> attribute) {
		if (attribute != null) {
			if (attribute instanceof PluralAttribute) {
				final PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
				return pluralAttribute.getElementType();
			}
			else if (attribute instanceof SingularAttribute) {
				final SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
				return singularAttribute.getType();
			}
			else {
				return attribute.getDeclaringType();
			}
		}
		return null;
	}
}
