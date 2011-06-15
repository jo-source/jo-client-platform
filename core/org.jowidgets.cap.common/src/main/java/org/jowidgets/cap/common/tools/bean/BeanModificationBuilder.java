/*
 * Copyright (c) 2011, grossmann
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

package org.jowidgets.cap.common.tools.bean;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.bean.IBeanModificationBuilder;

public final class BeanModificationBuilder implements IBeanModificationBuilder {

	private final IBeanModificationBuilder builder;

	private BeanModificationBuilder() {
		this.builder = CapCommonToolkit.beanModificationBuilder();
	}

	@Override
	public IBeanModificationBuilder setId(final Object id) {
		builder.setId(id);
		return this;
	}

	@Override
	public IBeanModificationBuilder setVersion(final long version) {
		builder.setVersion(version);
		return this;
	}

	@Override
	public IBeanModificationBuilder setBeanDto(final IBeanDto beanDto) {
		builder.setBeanDto(beanDto);
		return this;
	}

	@Override
	public IBeanModificationBuilder setPropertyName(final String propertyName) {
		builder.setPropertyName(propertyName);
		return this;
	}

	@Override
	public IBeanModificationBuilder setNewValue(final Object newValue) {
		builder.setNewValue(newValue);
		return this;
	}

	@Override
	public IBeanModification build() {
		return builder.build();
	}

}
