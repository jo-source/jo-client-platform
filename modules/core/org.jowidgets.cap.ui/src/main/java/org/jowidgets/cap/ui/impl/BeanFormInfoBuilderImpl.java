/*
 * Copyright (c) 2013, grossmann
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

import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.cap.common.api.bean.IBeanFormInfoDescriptor;
import org.jowidgets.cap.ui.api.form.IBeanFormInfo;
import org.jowidgets.cap.ui.api.form.IBeanFormInfoBuilder;
import org.jowidgets.cap.ui.api.image.ImageResolver;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.tools.StaticMessage;
import org.jowidgets.util.Assert;

final class BeanFormInfoBuilderImpl implements IBeanFormInfoBuilder {

	private IMessage header;
	private IMessage text;
	private IImageConstant headerIcon;
	private boolean expanded;

	BeanFormInfoBuilderImpl() {
		this.expanded = false;
		//TODO i18n
		this.header = new StaticMessage("Information");
		this.headerIcon = IconsSmall.INFO;
		this.text = new StaticMessage();
	}

	@Override
	public IBeanFormInfoBuilder setHeader(final IMessage header) {
		if (header != null) {
			this.header = header;
		}
		else {
			this.header = new StaticMessage();
		}
		return this;
	}

	@Override
	public IBeanFormInfoBuilder setHeader(final String header) {
		return setHeader(new StaticMessage(header));
	}

	@Override
	public IBeanFormInfoBuilder setText(final IMessage text) {
		if (text != null) {
			this.text = text;
		}
		else {
			this.text = new StaticMessage();
		}
		return this;
	}

	@Override
	public IBeanFormInfoBuilder setText(final String text) {
		return setText(new StaticMessage(text));
	}

	@Override
	public IBeanFormInfoBuilder setHeaderIcon(final IImageConstant icon) {
		this.headerIcon = icon;
		return this;
	}

	@Override
	public IBeanFormInfoBuilder setExpanded(final boolean expanded) {
		this.expanded = expanded;
		return this;
	}

	@Override
	public IBeanFormInfoBuilder setDescriptor(final IBeanFormInfoDescriptor descriptor) {
		Assert.paramNotNull(descriptor, "descriptor");
		setHeader(descriptor.getHeader());
		setText(descriptor.getText());
		setExpanded(descriptor.isExpanded());
		final Object iconDescriptor = descriptor.getHeaderIconDescriptor();
		if (iconDescriptor != null) {
			setHeaderIcon(ImageResolver.resolve(iconDescriptor));
		}
		return this;
	}

	@Override
	public IBeanFormInfo build() {
		return new BeanFormInfoImpl(header, text, headerIcon, expanded);
	}

}
