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

import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeMenuInterceptor;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeSetupConvenience;
import org.jowidgets.tools.widgets.blueprint.convenience.AbstractSetupBuilderConvenience;
import org.jowidgets.util.Assert;
import org.jowidgets.util.FilterComposite;
import org.jowidgets.util.IFilter;

final class BeanRelationTreeSetupConvenience extends
		AbstractSetupBuilderConvenience<IBeanRelationTreeSetupBuilder<IBeanRelationTreeSetupBuilder<?>>> implements
		IBeanRelationTreeSetupConvenience<IBeanRelationTreeSetupBuilder<IBeanRelationTreeSetupBuilder<?>>> {

	@Override
	public IBeanRelationTreeSetupBuilder<IBeanRelationTreeSetupBuilder<?>> addChildRelationFilter(
		final IFilter<IBeanRelationNodeModel<Object, Object>> filter) {
		Assert.paramNotNull(filter, "filter");
		final IBeanRelationTreeSetupBuilder<IBeanRelationTreeSetupBuilder<?>> builder = getBuilder();
		final IFilter<IBeanRelationNodeModel<Object, Object>> currentFilter = builder.getChildRelationFilter();
		builder.setChildRelationFilter(FilterComposite.create(currentFilter, filter));
		return builder;
	}

	@Override
	public IBeanRelationTreeSetupBuilder<IBeanRelationTreeSetupBuilder<?>> addMenuInterceptor(
		final IBeanRelationTreeMenuInterceptor addedInterceptor) {
		Assert.paramNotNull(addedInterceptor, "addedInterceptor");
		final IBeanRelationTreeSetupBuilder<IBeanRelationTreeSetupBuilder<?>> builder = getBuilder();
		final IBeanRelationTreeMenuInterceptor currentInterceptor = builder.getMenuInterceptor();
		builder.setMenuInterceptor(BeanRelationTreeMenuInterceptorComposite.create(currentInterceptor, addedInterceptor));
		return builder;
	}
}
