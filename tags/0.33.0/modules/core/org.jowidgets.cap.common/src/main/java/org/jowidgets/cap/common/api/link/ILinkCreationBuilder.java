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

package org.jowidgets.cap.common.api.link;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanKey;

public interface ILinkCreationBuilder {

	/**
	 * Sets the source beans that should be linked.
	 * 
	 * @param beans The beans to link, may be empty but not null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder setSourceBeans(Collection<? extends IBeanKey> beans);

	/**
	 * Adds a source bean that should be linked.
	 * 
	 * @param beans The bean to link, must not be null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder addSourceBean(IBeanKey bean);

	/**
	 * Sets the transient source beans that should be created and linked.
	 * 
	 * @param beans The beans to create and link, may be empty but not null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder setTransientSourceBeans(Collection<? extends IBeanData> beans);

	/**
	 * Adds a transient source bean that should be created and linked.
	 * 
	 * @param beans The bean to link, must not be null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder addTransientSourceBean(IBeanData bean);

	/**
	 * Sets the additional properties if the link itself has properties
	 * 
	 * @param properties The properties to set
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder setAdditionalLinkProperties(IBeanData properties);

	/**
	 * Sets the linkable beans that should be linked.
	 * 
	 * @param beans The beans to link, may be empty but not null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder setLinkableBeans(Collection<? extends IBeanKey> beans);

	/**
	 * Adds a linkable bean that should be linked.
	 * 
	 * @param beans The bean to link, must not be null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder addLinkableBean(IBeanKey bean);

	/**
	 * Sets the transient linkable beans that should be created and linked.
	 * 
	 * @param beans The beans to create and link, may be empty but not null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder setTransientLinkableBeans(Collection<? extends IBeanData> beans);

	/**
	 * Adds a transient linkable bean that should be created and linked.
	 * 
	 * @param beans The bean to link, must not be null
	 * 
	 * @return This builder
	 */
	ILinkCreationBuilder addTransientLinkableBean(IBeanData bean);

	/**
	 * @return The link creation
	 */
	ILinkCreation build();

}
