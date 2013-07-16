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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.cap.ui.api.model.ILabelModel;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.types.Markup;

final class LabelModelImpl implements ILabelModel {

	private final String text;
	private final String description;
	private final IImageConstant icon;
	private final IColorConstant foregroundColor;
	private final Integer fontSize;
	private final String fontName;
	private final Markup markup;

	LabelModelImpl(final String text) {
		this(text, null, null);
	}

	LabelModelImpl(final String text, final IImageConstant icon) {
		this(text, null, icon);
	}

	LabelModelImpl(final String text, final String description, final IImageConstant icon) {
		this(text, description, icon, null, null, null, null);
	}

	public LabelModelImpl(
		final String text,
		final String description,
		final IImageConstant icon,
		final IColorConstant foregroundColor,
		final Integer fontSize,
		final String fontName,
		final Markup markup) {

		this.text = text;
		this.description = description;
		this.icon = icon;
		this.foregroundColor = foregroundColor;
		this.fontSize = fontSize;
		this.fontName = fontName;
		this.markup = markup;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public IImageConstant getIcon() {
		return icon;
	}

	@Override
	public IColorConstant getForegroundColor() {
		return foregroundColor;
	}

	@Override
	public Integer getFontSize() {
		return fontSize;
	}

	@Override
	public String getFontName() {
		return fontName;
	}

	@Override
	public Markup getMarkup() {
		return markup;
	}

	@Override
	public String toString() {
		return "LabelModelImpl [text=" + text + ", description=" + description + "]";
	}

}
