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

package org.jowidgets.cap.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.api.bean.IUniqueConstraintChecker;
import org.jowidgets.cap.service.api.bean.IUniqueConstraintCheckerBuilder;
import org.jowidgets.cap.service.api.factory.IBeanServiceFactory;
import org.jowidgets.util.Assert;

final class UniqueConstraintCheckerBuilderImpl implements IUniqueConstraintCheckerBuilder {

	private final IBeanServiceFactory serviceFactory;
	private final Class<? extends IBean> beanType;
	private final Object beanTypeId;
	private final List<String> propertyNames;

	UniqueConstraintCheckerBuilderImpl(
		final IBeanServiceFactory serviceFactory,
		final Class<? extends IBean> beanType,
		final Object beanTypeId) {
		Assert.paramNotNull(serviceFactory, "serviceFactory");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		this.serviceFactory = serviceFactory;
		this.beanType = beanType;
		this.beanTypeId = beanTypeId;
		this.propertyNames = new LinkedList<String>();
	}

	@Override
	public IUniqueConstraintCheckerBuilder setProperties(final Collection<String> propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		this.propertyNames.clear();
		for (final String propertyName : propertyNames) {
			Assert.paramNotEmpty(propertyName, "propertyName");
			propertyNames.add(propertyName);
		}
		this.propertyNames.addAll(propertyNames);
		return this;
	}

	@Override
	public IUniqueConstraintCheckerBuilder setProperties(final String... propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		final List<String> propertyNamesList = Arrays.asList(propertyNames);
		return setProperties(propertyNamesList);
	}

	@Override
	public IUniqueConstraintCheckerBuilder addProperty(final String propertyName) {
		Assert.paramNotEmpty(propertyName, "propertyName");
		this.propertyNames.add(propertyName);
		return this;
	}

	@Override
	public IUniqueConstraintChecker build() {
		return new UniqueConstraintCheckerImpl(serviceFactory, beanType, beanTypeId, propertyNames);
	}

}
