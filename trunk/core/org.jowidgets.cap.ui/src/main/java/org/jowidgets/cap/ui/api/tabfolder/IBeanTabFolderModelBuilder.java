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

package org.jowidgets.cap.ui.api.tabfolder;

import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxyLabelRenderer;
import org.jowidgets.cap.ui.api.model.IBeanModelBuilder;
import org.jowidgets.cap.ui.api.sort.ISortModelConfig;

public interface IBeanTabFolderModelBuilder<BEAN_TYPE> extends
		IBeanModelBuilder<BEAN_TYPE, IBeanTabFolderModelBuilder<BEAN_TYPE>> {

	IBeanTabFolderModelBuilder<BEAN_TYPE> setExceptionConverter(IBeanExceptionConverter exceptionConverter);

	IBeanTabFolderModelBuilder<BEAN_TYPE> setSorting(ISortModelConfig sorting);

	IBeanTabFolderModelBuilder<BEAN_TYPE> setLabelRenderer(IBeanProxyLabelRenderer<BEAN_TYPE> renderer);

	IBeanTabFolderModelBuilder<BEAN_TYPE> addInterceptor(IBeanTabFolderModelInterceptor<BEAN_TYPE> interceptor);

	IBeanTabFolderModelBuilder<BEAN_TYPE> setClearOnEmptyFilter(boolean clearOnEmptyFilter);

	/**
	 * If this option is set, data will only be loaded if any parent bean is selected.
	 * Otherwise data will be cleared on load.
	 * The default value is false, if no parent is set and true if a parent is set
	 * 
	 * @param clearOnEmptyFilter
	 * @return This builder
	 */
	IBeanTabFolderModelBuilder<BEAN_TYPE> setClearOnEmptyParentBeans(boolean clearOnEmptyParentBeans);

	IBeanTabFolderModel<BEAN_TYPE> build();

}
