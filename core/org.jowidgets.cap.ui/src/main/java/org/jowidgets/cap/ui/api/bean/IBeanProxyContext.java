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

package org.jowidgets.cap.ui.api.bean;

public interface IBeanProxyContext {

	/**
	 * Registers a bean to the context for a specific owner. If the bean already exists in the context,
	 * the existing bean will be updated and returned, else the given bean will be returned.
	 * 
	 * @param bean The bean to add
	 * @param owner The owner of the bean
	 * 
	 * @return The added bean, if the bean not exists in the context before. If the bean was already added, the previous added and
	 *         updated bean will be returned
	 */
	<BEAN_TYPE> IBeanProxy<BEAN_TYPE> registerBean(IBeanProxy<BEAN_TYPE> bean, Object owner);

	/**
	 * Unregisters a bean for a specific owner. If the owner was the last owner that has been registered the bean, the
	 * bean will be removed from the context
	 * 
	 * @param bean The bean to unregister
	 * @param owner The owner of the bean
	 */
	void unregisterBean(IBeanProxy<?> bean, Object owner);

	/**
	 * Clears the context, so all cached beans will not be referenced any more
	 */
	void clear();
}
