/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.service.api.ordered;

import java.util.Set;

import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;

public interface IOrderedBeanGroupMapper<BEAN_TYPE extends IOrderedBean, GROUP_TYPE> {

	/**
	 * Gets the group a the ordered bean
	 * 
	 * @param groupMember The group member to get the group for
	 * @return The group for the bean, may be null
	 */
	GROUP_TYPE getGroup(BEAN_TYPE groupMember);

	/**
	 * Creates a filter for all members of of a given group
	 * 
	 * @param group The group to get the beans for, may be null
	 * 
	 * @return The filter to filter group element, may be null
	 */
	IFilter createGroupFilter(GROUP_TYPE group);

	/**
	 * Gets the attributes that may change the group when modified
	 * 
	 * @return The group changing attributes, may be empty but never null
	 */
	Set<String> getGroupChangingAttributes();
}
