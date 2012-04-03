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

package org.jowidgets.cap.sample1.plugins.ui.table;

import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.ui.api.command.ICreatorActionBuilder;
import org.jowidgets.cap.ui.api.command.IDeleterActionBuilder;
import org.jowidgets.cap.ui.api.plugin.IBeanTableMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.tools.table.BeanTableMenuInterceptorAdapter;
import org.jowidgets.examples.common.icons.SilkIcons;
import org.jowidgets.plugin.api.IPluginProperties;

public class UserMenuInterceptorPlugin extends BeanTableMenuInterceptorAdapter<IUser> implements
		IBeanTableMenuInterceptorPlugin<IUser> {

	@Override
	public IBeanTableMenuInterceptor<IUser> getMenuInterceptor(final IPluginProperties properties, final IBeanTable<IUser> table) {
		return this;
	}

	@Override
	public ICreatorActionBuilder creatorActionBuilder(final IBeanTable<IUser> table, final ICreatorActionBuilder builder) {
		builder.setIcon(SilkIcons.USER_ADD);
		return builder;
	}

	@Override
	public IDeleterActionBuilder<IUser> deleterActionBuilder(
		final IBeanTable<IUser> table,
		final IDeleterActionBuilder<IUser> builder) {
		builder.setIcon(SilkIcons.USER_DELETE);
		return builder;
	}

}