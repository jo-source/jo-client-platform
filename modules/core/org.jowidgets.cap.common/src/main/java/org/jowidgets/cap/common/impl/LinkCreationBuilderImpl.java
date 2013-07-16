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

package org.jowidgets.cap.common.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.link.ILinkCreation;
import org.jowidgets.cap.common.api.link.ILinkCreationBuilder;
import org.jowidgets.util.Assert;

final class LinkCreationBuilderImpl implements ILinkCreationBuilder {

	private final Collection<IBeanKey> sourceBeans;
	private final Collection<IBeanData> transientSourceBeans;
	private final Collection<IBeanKey> linkableBeans;
	private final Collection<IBeanData> transientLinkableBeans;

	private IBeanData additionalLinkProperties;

	public LinkCreationBuilderImpl() {
		super();
		this.sourceBeans = new LinkedList<IBeanKey>();
		this.transientSourceBeans = new LinkedList<IBeanData>();
		this.linkableBeans = new LinkedList<IBeanKey>();
		this.transientLinkableBeans = new LinkedList<IBeanData>();
	}

	@Override
	public ILinkCreationBuilder setSourceBeans(final Collection<? extends IBeanKey> beans) {
		Assert.paramNotNull(beans, "beans");
		this.sourceBeans.clear();
		this.sourceBeans.addAll(beans);
		return this;
	}

	@Override
	public ILinkCreationBuilder addSourceBean(final IBeanKey bean) {
		Assert.paramNotNull(bean, "bean");
		sourceBeans.add(bean);
		return this;
	}

	@Override
	public ILinkCreationBuilder setTransientSourceBeans(final Collection<? extends IBeanData> beans) {
		Assert.paramNotNull(beans, "beans");
		this.transientSourceBeans.clear();
		this.transientSourceBeans.addAll(beans);
		return this;
	}

	@Override
	public ILinkCreationBuilder addTransientSourceBean(final IBeanData bean) {
		Assert.paramNotNull(bean, "bean");
		transientSourceBeans.add(bean);
		return this;
	}

	@Override
	public ILinkCreationBuilder setAdditionalLinkProperties(final IBeanData properties) {
		this.additionalLinkProperties = properties;
		return this;
	}

	@Override
	public ILinkCreationBuilder setLinkableBeans(final Collection<? extends IBeanKey> beans) {
		Assert.paramNotNull(beans, "beans");
		this.linkableBeans.clear();
		this.linkableBeans.addAll(beans);
		return this;
	}

	@Override
	public ILinkCreationBuilder addLinkableBean(final IBeanKey bean) {
		Assert.paramNotNull(bean, "bean");
		linkableBeans.add(bean);
		return this;
	}

	@Override
	public ILinkCreationBuilder setTransientLinkableBeans(final Collection<? extends IBeanData> beans) {
		Assert.paramNotNull(beans, "beans");
		this.transientLinkableBeans.clear();
		this.transientLinkableBeans.addAll(beans);
		return this;
	}

	@Override
	public ILinkCreationBuilder addTransientLinkableBean(final IBeanData bean) {
		Assert.paramNotNull(bean, "bean");
		transientLinkableBeans.add(bean);
		return this;
	}

	@Override
	public ILinkCreation build() {
		return new LinkCreationImpl(
			sourceBeans,
			transientSourceBeans,
			additionalLinkProperties,
			linkableBeans,
			transientLinkableBeans);
	}

}
