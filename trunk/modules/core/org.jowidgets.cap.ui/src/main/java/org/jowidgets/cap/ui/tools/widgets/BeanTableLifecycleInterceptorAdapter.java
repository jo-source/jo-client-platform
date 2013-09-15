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

package org.jowidgets.cap.ui.tools.widgets;

import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableLifecycleInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;

public class BeanTableLifecycleInterceptorAdapter<BEAN_TYPE> implements IBeanTableLifecycleInterceptor<BEAN_TYPE> {

	@Override
	public void onModelCreate(
		final IBeanRelationNodeModel<Object, Object> relationNode,
		final IBeanTableModelBuilder<BEAN_TYPE> builder) {}

	@Override
	public void afterModelCreated(
		final IBeanRelationNodeModel<Object, Object> relationNode,
		final IBeanTableModel<BEAN_TYPE> model) {}

	@Override
	public void onTableCreate(
		final IBeanRelationNodeModel<Object, Object> relationNode,
		final IBeanTableSetupBuilder<?, BEAN_TYPE> builder) {}

	@Override
	public void afterTableCreated(final IBeanRelationNodeModel<Object, Object> relationNode, final IBeanTable<BEAN_TYPE> table) {}

	@Override
	public void beforeTableDispose(final IBeanTable<BEAN_TYPE> table) {}

	@Override
	public void beforeModelDispose(final IBeanTableModel<BEAN_TYPE> model) {}

}
