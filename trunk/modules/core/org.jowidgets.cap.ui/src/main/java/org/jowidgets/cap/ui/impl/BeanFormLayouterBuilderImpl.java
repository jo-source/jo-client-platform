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

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.widgets.blueprint.IExpandCompositeBluePrint;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouterBuilder;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.Insets;
import org.jowidgets.common.types.Markup;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

final class BeanFormLayouterBuilderImpl implements IBeanFormLayouterBuilder {

	private final IBeanFormLayout layout;

	private boolean border;
	private boolean headerBorder;
	private boolean contentBorder;
	private Insets insets;
	private Insets contentInsets;
	private Markup headerTextMarkup;
	private IColorConstant headerTextColor;
	private IColorConstant headerBackgroundColor;
	private Markup contentTextMarkup;
	private IColorConstant contentTextColor;
	private IColorConstant contentBackgroundColor;

	public BeanFormLayouterBuilderImpl(final IBeanFormLayout layout) {
		Assert.paramNotNull(layout, "layout");
		this.layout = layout;

		final IExpandCompositeBluePrint expandCompositeBp = BPF.expandComposite();

		this.border = expandCompositeBp.getBorder();
		this.headerBorder = expandCompositeBp.getHeaderBorder();
		this.contentBorder = expandCompositeBp.getContentBorder();
		this.insets = new Insets(0);
		this.contentInsets = new Insets(8);
		this.headerBackgroundColor = expandCompositeBp.getHeaderBackgroundColor();
		this.headerTextMarkup = Markup.STRONG;
		this.headerTextColor = Colors.STRONG;
		this.contentTextMarkup = Markup.DEFAULT;
		this.contentTextColor = Colors.STRONG;
	}

	@Override
	public IBeanFormLayouterBuilder setBorder(final boolean border) {
		this.border = border;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setHeaderBorder(final boolean border) {
		this.headerBorder = border;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setContentBorder(final boolean border) {
		this.contentBorder = border;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setHeaderTextMarkup(final Markup markup) {
		Assert.paramNotNull(markup, "markup");
		this.headerTextMarkup = markup;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setHeaderTextColor(final IColorConstant color) {
		this.headerTextColor = color;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setHeaderBackgroundColor(final IColorConstant color) {
		this.headerBackgroundColor = color;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setContentTextColor(final IColorConstant color) {
		this.contentTextColor = color;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setContentTextMarkup(final Markup markup) {
		Assert.paramNotNull(markup, "markup");
		this.contentTextMarkup = markup;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setContentBackgroundColor(final IColorConstant color) {
		this.contentBackgroundColor = color;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setInsets(final Insets insets) {
		this.insets = insets;
		return this;
	}

	@Override
	public IBeanFormLayouterBuilder setContentInsets(final Insets insets) {
		this.contentInsets = insets;
		return this;
	}

	@Override
	public IBeanFormLayouter build() {
		return new BeanFormLayouterImpl(
			layout,
			border,
			headerBorder,
			contentBorder,
			insets,
			contentInsets,
			headerTextMarkup,
			headerTextColor,
			headerBackgroundColor,
			contentTextMarkup,
			contentTextColor,
			contentBackgroundColor);
	}

}
