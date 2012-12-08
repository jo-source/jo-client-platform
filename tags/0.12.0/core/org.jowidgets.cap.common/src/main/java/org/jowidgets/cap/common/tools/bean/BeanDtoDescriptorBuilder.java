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

import java.util.Collection;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptorBuilder;
import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.util.Assert;

public class BeanDtoDescriptorBuilder implements IBeanDtoDescriptorBuilder {

	private final IBeanDtoDescriptorBuilder builder;

	public BeanDtoDescriptorBuilder(final Class<?> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		this.builder = CapCommonToolkit.dtoDescriptorBuilder(beanType);
	}

	@Override
	public IBeanDtoDescriptorBuilder setLabelSingular(final String label) {
		return this.builder.setLabelSingular(label);
	}

	@Override
	public IBeanDtoDescriptorBuilder setLabelPlural(final String label) {
		return this.builder.setLabelPlural(label);
	}

	@Override
	public IBeanDtoDescriptorBuilder setDescription(final String description) {
		return this.builder.setDescription(description);
	}

	@Override
	public IBeanDtoDescriptorBuilder setRenderingPattern(final String pattern) {
		return this.builder.setRenderingPattern(pattern);
	}

	@Override
	public IBeanDtoDescriptorBuilder addValidator(final IBeanValidator<?> validator) {
		return this.builder.addValidator(validator);
	}

	@Override
	public IBeanDtoDescriptorBuilder setDefaultSorting(final ISort... defaultSorting) {
		return this.builder.setDefaultSorting(defaultSorting);
	}

	@Override
	public IBeanDtoDescriptorBuilder setDefaultSorting(final Collection<ISort> defaultSorting) {
		return this.builder.setDefaultSorting(defaultSorting);
	}

	@Override
	public final IBeanPropertyBluePrint addProperty(final String propertyName) {
		return this.builder.addProperty(propertyName);
	}

	@Override
	public final IBeanDtoDescriptor build() {
		return builder.build();
	}

}