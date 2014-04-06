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

import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionStringRenderer;

final class BeanSelectionStringRendererImpl<BEAN_TYPE> implements IBeanSelectionStringRenderer<BEAN_TYPE> {

	@Override
	public String render(final IBeanSelection<BEAN_TYPE> selection) {

		final StringBuilder result = new StringBuilder();
		for (final IBeanProxy<BEAN_TYPE> bean : selection.getSelection()) {
			result.append(renderBean(bean));
			result.append("\n");
		}
		if (result.length() > 0) {
			return result.substring(0, result.length() - 1);
		}
		else {
			return "";
		}
	}

	private String renderBean(final IBeanProxy<BEAN_TYPE> bean) {
		final StringBuilder result = new StringBuilder();
		for (final IAttribute<Object> attribute : bean.getAttributes()) {
			if (attribute.isVisible()) {
				final String propertyName = attribute.getPropertyName();
				final Object value = bean.getValue(propertyName);
				if (value != null) {
					result.append(attribute.getValueAsString(value));
					result.append(" ");
				}
			}
		}
		if (result.length() > 0) {
			return result.substring(0, result.length() - 1);
		}
		else {
			return "";
		}
	}

}
