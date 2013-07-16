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

package org.jowidgets.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.IServicesDecoratorProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

@SuppressWarnings({"rawtypes", "unchecked"})
class ServicesDecoratorProviderImpl implements IServicesDecoratorProvider {

	private final IDecorator<Object> defaultDecorator;
	private final Map<Class, IDecorator> decorators;
	private final int order;

	ServicesDecoratorProviderImpl(
		final IDecorator<Object> defaultDecorator,
		final Map<Class<?>, IDecorator<?>> decorators,
		final int order) {
		this.defaultDecorator = defaultDecorator;
		this.decorators = new HashMap<Class, IDecorator>(decorators);
		this.order = order;
	}

	@Override
	public <SERVICE_TYPE> IDecorator<SERVICE_TYPE> getDecorator(final IServiceId<SERVICE_TYPE> id) {
		Assert.paramNotNull(id, "id");
		final IDecorator typeDecorator = decorators.get(id);
		if (defaultDecorator != null || typeDecorator != null) {
			return new IDecorator<SERVICE_TYPE>() {
				@Override
				public SERVICE_TYPE decorate(final SERVICE_TYPE original) {
					SERVICE_TYPE result = original;
					if (defaultDecorator != null) {
						result = (SERVICE_TYPE) defaultDecorator.decorate(result);
					}
					if (typeDecorator != null) {
						result = (SERVICE_TYPE) typeDecorator.decorate(result);
					}
					return result;
				}
			};
		}
		else {
			return null;
		}
	}

	@Override
	public int getOrder() {
		return order;
	}

}
