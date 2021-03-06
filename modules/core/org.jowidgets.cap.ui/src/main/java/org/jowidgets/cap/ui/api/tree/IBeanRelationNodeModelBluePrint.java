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

package org.jowidgets.cap.ui.api.tree;

import java.util.Collection;

import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.IBeanModelBuilder;
import org.jowidgets.cap.ui.api.types.IEntityTypeId;
import org.jowidgets.common.image.IImageConstant;

public interface IBeanRelationNodeModelBluePrint<CHILD_BEAN_TYPE, INSTANCE_TYPE> extends
		IBeanModelBuilder<CHILD_BEAN_TYPE, INSTANCE_TYPE> {

	INSTANCE_TYPE setText(String text);

	INSTANCE_TYPE setDescription(String description);

	INSTANCE_TYPE setIcon(IImageConstant icon);

	INSTANCE_TYPE setChildRenderer(IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> renderer);

	INSTANCE_TYPE addChildRelation(IEntityTypeId<?> entityTypeId);

	INSTANCE_TYPE addChildRelation(Object entityId, Class<?> beanType);

	INSTANCE_TYPE addChildRelation(Class<?> beanType);

	INSTANCE_TYPE addDefaultSort(ISort sort);

	INSTANCE_TYPE setDefaultSort(ISort... defaultSort);

	INSTANCE_TYPE setDefaultSort(Collection<? extends ISort> defaultSort);

	INSTANCE_TYPE setPageSize(int pageSize);

	/**
	 * If this option is set, data will only be loaded if any parent bean is persistent.
	 * Otherwise data will be cleared on load.
	 * The default value is false for the root node of a tree and false for all inner nodes
	 * 
	 * @param clearOnTransientParent
	 * @return This builder
	 */
	INSTANCE_TYPE setClearOnTransientParent(boolean clearOnTransientParent);

	IBeanProxyLabelRenderer<CHILD_BEAN_TYPE> getChildRenderer();

}
