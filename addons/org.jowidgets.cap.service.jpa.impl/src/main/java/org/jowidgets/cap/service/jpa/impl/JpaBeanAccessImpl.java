/*
 * Copyright (c) 2011, H.Westphal, M. Grossmann
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
package org.jowidgets.cap.service.jpa.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class JpaBeanAccessImpl<BEAN_TYPE extends IBean> implements IBeanAccess<BEAN_TYPE> {

	private final Class<? extends BEAN_TYPE> beanType;

	JpaBeanAccessImpl(final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.beanType = beanType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BEAN_TYPE> getBeans(final Collection<? extends IBeanKey> keys, final IExecutionCallback executionCallback) {
		if (EmptyCheck.isEmpty(keys)) {
			return Collections.emptyList();
		}
		else {
			final Collection<Object> ids = new HashSet<Object>(keys.size());
			for (final IBeanKey beanInfo : keys) {
				ids.add(beanInfo.getId());
			}
			final EntityManager entityManager = EntityManagerProvider.get();
			final CriteriaQuery<? extends BEAN_TYPE> query = entityManager.getCriteriaBuilder().createQuery(beanType);
			final Root<? extends BEAN_TYPE> bean = query.from(beanType);
			query.where(bean.get(IBean.ID_PROPERTY).in(ids));
			CapServiceToolkit.checkCanceled(executionCallback);
			final List<? extends BEAN_TYPE> result = entityManager.createQuery(query).getResultList();
			CapServiceToolkit.checkCanceled(executionCallback);
			return (List<BEAN_TYPE>) result;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<BEAN_TYPE> getBeanType() {
		return (Class<BEAN_TYPE>) beanType;
	}

	@Override
	public void flush() {
		EntityManagerProvider.get().flush();
	}

}
