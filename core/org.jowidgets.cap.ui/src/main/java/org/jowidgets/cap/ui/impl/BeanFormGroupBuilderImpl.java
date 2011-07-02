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

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.form.BeanFormGroupRendering;
import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormGroupBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.cap.ui.api.form.IBeanFormPropertyBuilder;
import org.jowidgets.util.Assert;

final class BeanFormGroupBuilderImpl implements IBeanFormGroupBuilder {

	private final List<IBeanFormProperty> properties;

	private String label;
	private BeanFormGroupRendering rendering;

	BeanFormGroupBuilderImpl() {
		this.properties = new LinkedList<IBeanFormProperty>();
	}

	@Override
	public IBeanFormGroupBuilder setLabel(final String label) {
		this.label = label;
		return this;
	}

	@Override
	public IBeanFormGroupBuilder setRendering(final BeanFormGroupRendering rendering) {
		this.rendering = rendering;
		return this;
	}

	@Override
	public IBeanFormGroupBuilder addProperty(final String property) {
		this.properties.add(getPropertyBuilder().setPropertyName(property).build());
		return this;
	}

	@Override
	public IBeanFormGroupBuilder addProperty(final String property, final int rowSpan, final int columnSpan) {
		final IBeanFormPropertyBuilder builder = getPropertyBuilder().setPropertyName(property);
		builder.setRowSpan(rowSpan).setColumnSpan(columnSpan);
		this.properties.add(builder.build());
		return this;
	}

	@Override
	public IBeanFormGroupBuilder addProperty(final IBeanFormProperty property) {
		Assert.paramNotNull(property, "property");
		this.properties.add(property);
		return this;
	}

	@Override
	public IBeanFormGroupBuilder addProperty(final IBeanFormPropertyBuilder propertyBuilder) {
		Assert.paramNotNull(propertyBuilder, "propertyBuilder");
		this.properties.add(propertyBuilder.build());
		return this;
	}

	@Override
	public IBeanFormGroupBuilder addProperties(final Collection<IAttribute<?>> attributes) {
		Assert.paramNotNull(attributes, "attributes");
		for (final IAttribute<?> attribute : attributes) {
			addProperty(attribute.getPropertyName());
		}
		return this;
	}

	@Override
	public IBeanFormGroupBuilder addProperties(
		final Collection<IAttribute<?>> attributes,
		final IBeanFormPropertyBuilder defaultBuilder) {
		Assert.paramNotNull(attributes, "attributes");
		for (final IAttribute<?> attribute : attributes) {
			addProperty(defaultBuilder.setPropertyName(attribute.getPropertyName()).build());
		}
		return this;
	}

	@Override
	public IBeanFormGroupBuilder changeProperty(final String oldProperty, final IBeanFormProperty newProperty) {
		Assert.paramNotEmpty(oldProperty, "oldProperty");
		Assert.paramNotNull(newProperty, "newProperty");
		final int index = findProperty(oldProperty);
		if (index == -1) {
			throw new IllegalArgumentException("The property with the name '" + oldProperty + "' is not set.");
		}
		properties.remove(index);
		properties.add(index, newProperty);
		return this;
	}

	@Override
	public IBeanFormGroup build() {
		return new BeanFormGroupImpl(properties, label, rendering);
	}

	private int findProperty(final String propertyName) {
		int index = 0;
		for (final IBeanFormProperty property : properties) {
			if (property.getPropertyName().equals(propertyName)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private IBeanFormPropertyBuilder getPropertyBuilder() {
		return CapUiToolkit.beanFormToolkit().propertyBuilder();
	}
}
