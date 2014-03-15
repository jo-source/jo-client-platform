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

package org.jowidgets.cap.ui.api.clipboard;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelection;

public final class BeanSelectionClipboard {

	private BeanSelectionClipboard() {}

	public static IBeanSelectionClipboardBuilder builder() {
		return CapUiToolkit.beanSelectionClipboardBuilder();
	}

	public static IBeanSelectionClipboard create(final IBeanSelection<?> beanSelection) {
		final IBeanSelectionClipboardBuilder builder = builder();
		builder.setEntityId(beanSelection.getEntityId());
		builder.setBeanTypeId(beanSelection.getBeanTypeId());
		builder.setBeanType(beanSelection.getBeanType());
		final List<IBeanDto> beans = new LinkedList<IBeanDto>();
		for (final IBeanProxy<?> beanProxy : beanSelection.getSelection()) {
			final IBeanDto beanDto = beanProxy.createUnmodifiedCopy().getBeanDto();
			if (beanProxy.isTransient()) {
				beans.add(new TransientBeanDto(beanDto));
			}
			else {
				beans.add(beanDto);
			}

		}
		builder.setBeans(beans);
		return builder.build();
	}

	private static final class TransientBeanDto implements IBeanDto, Serializable {

		private static final long serialVersionUID = 7351267002806767597L;

		private final IBeanDto original;

		private TransientBeanDto(final IBeanDto original) {
			this.original = original;
		}

		@Override
		public Object getValue(final String propertyName) {
			if (IBean.ID_PROPERTY.equals(propertyName) || IBean.VERSION_PROPERTY.equals(propertyName)) {
				return null;
			}
			else {
				return original.getValue(propertyName);
			}
		}

		@Override
		public Object getId() {
			return null;
		}

		@Override
		public long getVersion() {
			return 0;
		}

	}

}
