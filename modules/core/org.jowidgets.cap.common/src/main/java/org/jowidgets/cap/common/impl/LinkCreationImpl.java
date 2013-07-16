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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.link.ILinkCreation;
import org.jowidgets.util.Assert;

final class LinkCreationImpl implements ILinkCreation, Serializable {

	private static final long serialVersionUID = 2496955441718314856L;

	private final Collection<IBeanKey> sourceBeans;
	private final Collection<IBeanData> transientSourceBeans;
	private final IBeanData additionalLinkProperties;
	private final Collection<IBeanKey> linkableBeans;
	private final Collection<IBeanData> transientLinkableBeans;

	public LinkCreationImpl(
		final Collection<IBeanKey> sourceBeans,
		final Collection<IBeanData> transientSourceBeans,
		final IBeanData additionalLinkProperties,
		final Collection<IBeanKey> linkableBeans,
		final Collection<IBeanData> transientLinkableBeans) {

		Assert.paramNotNull(sourceBeans, "sourceBeans");
		Assert.paramNotNull(transientSourceBeans, "transientSourceBeans");
		Assert.paramNotNull(linkableBeans, "linkableBeans");
		Assert.paramNotNull(transientLinkableBeans, "transientLinkableBeans");

		this.sourceBeans = Collections.unmodifiableList(new LinkedList<IBeanKey>(sourceBeans));
		this.transientSourceBeans = Collections.unmodifiableList(new LinkedList<IBeanData>(transientSourceBeans));
		this.additionalLinkProperties = additionalLinkProperties;
		this.linkableBeans = Collections.unmodifiableList(new LinkedList<IBeanKey>(linkableBeans));
		this.transientLinkableBeans = Collections.unmodifiableList(new LinkedList<IBeanData>(transientLinkableBeans));
	}

	@Override
	public Collection<IBeanKey> getSourceBeans() {
		return sourceBeans;
	}

	@Override
	public Collection<IBeanData> getTransientSourceBeans() {
		return transientSourceBeans;
	}

	@Override
	public IBeanData getAdditionalLinkProperties() {
		return additionalLinkProperties;
	}

	@Override
	public Collection<IBeanKey> getLinkableBeans() {
		return linkableBeans;
	}

	@Override
	public Collection<IBeanData> getTransientLinkableBeans() {
		return transientLinkableBeans;
	}

	@Override
	public String toString() {
		return "LinkCreationImpl [sourceBeans="
			+ sourceBeans
			+ ", transientSourceBeans="
			+ transientSourceBeans
			+ ", additionalLinkProperties="
			+ additionalLinkProperties
			+ ", linkableBeans="
			+ linkableBeans
			+ ", transientLinkableBeans="
			+ transientLinkableBeans
			+ "]";
	}

}
