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

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.cap.ui.api.command.ICopyActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkCreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IPasteLinkActionBuilder;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeDetailMenuInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;

final class BeanRelationTreeDetailMenuInterceptorComposite {

	private BeanRelationTreeDetailMenuInterceptorComposite() {}

	static IBeanRelationTreeDetailMenuInterceptor create(
		final IBeanRelationTreeDetailMenuInterceptor interceptor1,
		final IBeanRelationTreeDetailMenuInterceptor interceptor2) {

		if (interceptor1 != null && interceptor2 != null) {
			return new BeanRelationTreeDetailMenuInterceptorImpl(interceptor1, interceptor2);
		}
		else if (interceptor1 != null) {
			return interceptor1;
		}
		else if (interceptor2 != null) {
			return interceptor2;
		}
		else {
			return null;
		}
	}

	private static final class BeanRelationTreeDetailMenuInterceptorImpl implements IBeanRelationTreeDetailMenuInterceptor {

		private final IBeanRelationTreeDetailMenuInterceptor interceptor1;
		private final IBeanRelationTreeDetailMenuInterceptor interceptor2;

		private BeanRelationTreeDetailMenuInterceptorImpl(
			final IBeanRelationTreeDetailMenuInterceptor interceptor1,
			final IBeanRelationTreeDetailMenuInterceptor interceptor2) {
			this.interceptor1 = interceptor1;
			this.interceptor2 = interceptor2;
		}

		@Override
		public ILinkCreatorActionBuilder<Object, Object, Object> linkCreatorActionBuilder(
			final IBeanTable<Object> table,
			final ILinkCreatorActionBuilder<Object, Object, Object> builder) {
			ILinkCreatorActionBuilder<Object, Object, Object> result = interceptor1.linkCreatorActionBuilder(table, builder);
			result = interceptor2.linkCreatorActionBuilder(table, result);
			return result;
		}

		@Override
		public ILinkDeleterActionBuilder<Object, Object> linkDeleterActionBuilder(
			final IBeanTable<Object> table,
			final ILinkDeleterActionBuilder<Object, Object> builder) {
			ILinkDeleterActionBuilder<Object, Object> result = interceptor1.linkDeleterActionBuilder(table, builder);
			result = interceptor2.linkDeleterActionBuilder(table, result);
			return result;
		}

		@Override
		public IPasteLinkActionBuilder<Object, Object, Object> pasteLinkActionBuilder(
			final IBeanTable<Object> table,
			final IPasteLinkActionBuilder<Object, Object, Object> builder) {
			IPasteLinkActionBuilder<Object, Object, Object> result = interceptor1.pasteLinkActionBuilder(table, builder);
			result = interceptor2.pasteLinkActionBuilder(table, result);
			return result;
		}

		@Override
		public IDeleterActionBuilder<Object> deleterActionBuilder(
			final IBeanTable<Object> table,
			final IDeleterActionBuilder<Object> builder) {
			IDeleterActionBuilder<Object> result = interceptor1.deleterActionBuilder(table, builder);
			result = interceptor2.deleterActionBuilder(table, result);
			return result;
		}

		@Override
		public ICopyActionBuilder<Object> copyActionBuilder(
			final IBeanTable<Object> table,
			final ICopyActionBuilder<Object> builder) {
			ICopyActionBuilder<Object> result = interceptor1.copyActionBuilder(table, builder);
			result = interceptor2.copyActionBuilder(table, result);
			return result;
		}

	}

}
