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

package org.jowidgets.cap.ui.api.widgets;

import java.util.List;

import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanel.IBeanLink;

public interface IBeanLinkPanel<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> extends
		IInputControl<IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE>> {

	public interface IBeanLink<LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> {

		/**
		 * Gets the link bean. May be null, if the link bean has no additional properties
		 * 
		 * @return The link bean or null
		 */
		IBeanProxy<LINK_BEAN_TYPE> getLinkBean();

		/**
		 * Gets the linked beans. The beans that was selected or created by the user.
		 * 
		 * Remark: The linked beans can be transient or not.
		 * 
		 * @return The linked beans. This may also be an empty list, if nothing was selected or created.
		 */
		List<IBeanProxy<LINKABLE_BEAN_TYPE>> getLinkedBeans();

	}

}
