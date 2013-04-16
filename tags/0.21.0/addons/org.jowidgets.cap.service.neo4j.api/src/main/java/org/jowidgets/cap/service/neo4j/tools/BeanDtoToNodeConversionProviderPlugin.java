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

package org.jowidgets.cap.service.neo4j.tools;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.plugin.IBeanDtoConversionProviderPlugin;
import org.jowidgets.cap.service.neo4j.api.INodeBean;
import org.jowidgets.cap.service.neo4j.api.IRelationshipBean;

final class BeanDtoToNodeConversionProviderPlugin implements IBeanDtoConversionProviderPlugin {

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE extends IBean> IBeanDtoFactory<BEAN_TYPE> dtoFactory(
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<String> propertyNames,
		final IBeanDtoFactory<BEAN_TYPE> original) {
		if (INodeBean.class.isAssignableFrom(beanType) || IRelationshipBean.class.isAssignableFrom(beanType)) {
			return (IBeanDtoFactory<BEAN_TYPE>) CapServiceToolkit.beanPropertyMapDtoFactory(propertyNames);
		}
		else {
			return original;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE extends IBean> IBeanInitializer<BEAN_TYPE> beanInitializer(
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<String> propertyNames,
		final IBeanInitializer<BEAN_TYPE> original) {
		if (INodeBean.class.isAssignableFrom(beanType) || IRelationshipBean.class.isAssignableFrom(beanType)) {
			return (IBeanInitializer<BEAN_TYPE>) CapServiceToolkit.beanPropertyMapInitializer(propertyNames);
		}
		else {
			return original;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <BEAN_TYPE extends IBean> IBeanModifier<BEAN_TYPE> beanModifier(
		final Class<? extends BEAN_TYPE> beanType,
		final Collection<String> propertyNames,
		final IBeanModifier<BEAN_TYPE> original) {
		if (INodeBean.class.isAssignableFrom(beanType) || IRelationshipBean.class.isAssignableFrom(beanType)) {
			return (IBeanModifier<BEAN_TYPE>) CapServiceToolkit.beanPropertyMapModifier();
		}
		else {
			return original;
		}
	}

}
