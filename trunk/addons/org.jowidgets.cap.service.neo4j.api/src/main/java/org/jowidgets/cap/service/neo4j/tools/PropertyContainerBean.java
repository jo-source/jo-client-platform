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

import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.cap.service.neo4j.api.IPropertyContainerBean;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.PropertyContainer;

public class PropertyContainerBean implements IPropertyContainerBean {

	private final String beanTypePropertyName;

	private PropertyContainer propertyContainer;

	public PropertyContainerBean(final PropertyContainer propertyContainer) {
		Assert.paramNotNull(propertyContainer, "propertyContainer");
		this.propertyContainer = propertyContainer;
		this.beanTypePropertyName = GraphDBConfig.getBeanTypePropertyName();
	}

	@Override
	public Object getId() {
		return getProperty(ID_PROPERTY);
	}

	@Override
	public long getVersion() {
		final Object version = getProperty(VERSION_PROPERTY);
		if (version instanceof Long) {
			return ((Long) version).longValue();
		}
		else {
			return -1;
		}
	}

	@Override
	public String getBeanType() {
		return (String) getProperty(beanTypePropertyName);
	}

	@SuppressWarnings("unchecked")
	protected <RESULT_TYPE> RESULT_TYPE getProperty(final String propertyName) {
		if (propertyContainer.hasProperty(propertyName)) {
			return (RESULT_TYPE) propertyContainer.getProperty(propertyName);
		}
		else {
			return null;
		}
	}

	protected void setProperty(final String propertyName, final Object value) {
		if (value != null) {
			propertyContainer.setProperty(propertyName, value);
		}
		else {
			propertyContainer.removeProperty(propertyName);
		}
	}

	protected PropertyContainer getPropertyContainer() {
		return propertyContainer;
	}

	protected void setPropertyContainer(final PropertyContainer propertyContainer) {
		Assert.paramNotNull(propertyContainer, "propertyContainer");
		this.propertyContainer = propertyContainer;
	}

}
