/*
 * Copyright (c) 2014, grossmann
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

import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.cap.ui.api.model.ILabelModelBuilder;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Markup;

final class LabelModelBuilder implements ILabelModelBuilder {

	private String text;
	private String description;
	private IImageConstant icon;
	private IColorConstant foregroundColor;
	private Integer fontSize;
	private String fontName;
	private Markup markup;

	@Override
	public String getText() {
		return text;
	}

	@Override
	public ILabelModelBuilder setText(final String text) {
		this.text = text;
		return this;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ILabelModelBuilder setDescription(final String description) {
		this.description = description;
		return this;
	}

	@Override
	public IImageConstant getIcon() {
		return icon;
	}

	@Override
	public ILabelModelBuilder setIcon(final IImageConstant icon) {
		this.icon = icon;
		return this;
	}

	@Override
	public IColorConstant getForegroundColor() {
		return foregroundColor;
	}

	@Override
	public ILabelModelBuilder setForegroundColor(final IColorConstant foregroundColor) {
		this.foregroundColor = foregroundColor;
		return this;
	}

	@Override
	public Integer getFontSize() {
		return fontSize;
	}

	@Override
	public ILabelModelBuilder setFontSize(final int fontSize) {
		this.fontSize = Integer.valueOf(fontSize);
		return this;
	}

	@Override
	public String getFontName() {
		return fontName;
	}

	@Override
	public ILabelModelBuilder setFontName(final String fontName) {
		this.fontName = fontName;
		return this;
	}

	@Override
	public Markup getMarkup() {
		return markup;
	}

	@Override
	public ILabelModelBuilder setMarkup(final Markup markup) {
		this.markup = markup;
		return this;
	}

	@Override
	public ILabelModel build() {
		return new LabelModelImpl(text, description, icon, foregroundColor, fontSize, fontName, markup);
	}

}
