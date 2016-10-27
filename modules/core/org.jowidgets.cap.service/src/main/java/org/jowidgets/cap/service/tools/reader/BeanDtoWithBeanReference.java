/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.service.tools.reader;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.service.api.bean.IBeanPropertyAccessor;
import org.jowidgets.util.Assert;

final class BeanDtoWithBeanReference<BEAN_TYPE> implements IBeanDto {

	private final BEAN_TYPE beanReference;
	private final IBeanPropertyAccessor<BEAN_TYPE> propertyAccessor;

	BeanDtoWithBeanReference(final BEAN_TYPE beanReference, final IBeanPropertyAccessor<BEAN_TYPE> propertyAccessor) {
		Assert.paramNotNull(beanReference, "beanReference");
		Assert.paramNotNull(propertyAccessor, "propertyAccessor");
		this.beanReference = beanReference;
		this.propertyAccessor = propertyAccessor;
	}

	public BEAN_TYPE getBeanReference() {
		return beanReference;
	}

	@Override
	public Object getValue(final String propertyName) {
		return propertyAccessor.getValue(beanReference, propertyName);
	}

	@Override
	public Object getId() {
		return propertyAccessor.getId(beanReference);
	}

	@Override
	public long getVersion() {
		return propertyAccessor.getVersion(beanReference);
	}

}
