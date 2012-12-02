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

package org.jowidgets.cap.ui.api.image;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.util.Assert;

public final class ImageResolver {

	private static final Map<Class<?>, List<IImageResolver<?>>> RESOLVERS = new HashMap<Class<?>, List<IImageResolver<?>>>();
	private static final IImageResolver<Object> IMAGE_RESOLVER_COMPOSITE = new ImageResolverComposite();

	private ImageResolver() {}

	public static <DESCRIPTOR_TYPE> void register(
		final Class<DESCRIPTOR_TYPE> descriptorType,
		final IImageResolver<DESCRIPTOR_TYPE> resolver) {
		Assert.paramNotNull(descriptorType, "descriptorType");
		Assert.paramNotNull(resolver, "resolver");

		List<IImageResolver<?>> resolvers = RESOLVERS.get(descriptorType);
		if (resolvers == null) {
			resolvers = new LinkedList<IImageResolver<?>>();
			RESOLVERS.put(descriptorType, resolvers);
		}
		resolvers.add(resolver);
	}

	public static IImageResolver<Object> getInstance() {
		return IMAGE_RESOLVER_COMPOSITE;
	}

	public static IImageConstant resolve(final Object imageDescriptor) {
		return getInstance().resolve(imageDescriptor);
	}

	private static final class ImageResolverComposite implements IImageResolver<Object> {

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		public IImageConstant resolve(final Object imageDescriptor) {
			Assert.paramNotNull(imageDescriptor, "imageDescriptor");

			final List<IImageResolver<?>> resolvers = RESOLVERS.get(imageDescriptor.getClass());
			if (resolvers != null) {
				for (final IImageResolver resolver : resolvers) {
					final IImageConstant imageConstant = resolver.resolve(imageDescriptor);
					if (imageConstant != null) {
						return imageConstant;
					}
				}
			}
			return null;
		}

	}
}
