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

package org.jowidgets.cap.ui.tools.tree;

import org.jowidgets.cap.ui.api.command.ICopyActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkCreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.ILinkDeleterActionBuilder;
import org.jowidgets.cap.ui.api.command.IPasteLinkActionBuilder;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeDetailMenuInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;

public class BeanRelationTreeDetailMenuInterceptorAdapter implements IBeanRelationTreeDetailMenuInterceptor {

	@Override
	public ILinkCreatorActionBuilder<Object, Object, Object> linkCreatorActionBuilder(
		final IBeanTable<Object> table,
		final ILinkCreatorActionBuilder<Object, Object, Object> builder) {
		return builder;
	}

	@Override
	public ILinkDeleterActionBuilder<Object, Object> linkDeleterActionBuilder(
		final IBeanTable<Object> table,
		final ILinkDeleterActionBuilder<Object, Object> builder) {
		return builder;
	}

	@Override
	public IPasteLinkActionBuilder<Object, Object, Object> pasteLinkActionBuilder(
		final IBeanTable<Object> table,
		final IPasteLinkActionBuilder<Object, Object, Object> builder) {
		return builder;
	}

	@Override
	public IDeleterActionBuilder<Object> deleterActionBuilder(
		final IBeanTable<Object> table,
		final IDeleterActionBuilder<Object> builder) {
		return builder;
	}

	@Override
	public ICopyActionBuilder<Object> copyActionBuilder(final IBeanTable<Object> table, final ICopyActionBuilder<Object> builder) {
		return builder;
	}

}
