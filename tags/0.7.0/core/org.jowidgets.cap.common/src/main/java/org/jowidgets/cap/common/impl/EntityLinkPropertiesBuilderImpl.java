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

package org.jowidgets.cap.common.impl;

import org.jowidgets.cap.common.api.entity.IEntityLinkProperties;
import org.jowidgets.cap.common.api.entity.IEntityLinkPropertiesBuilder;
import org.jowidgets.util.Assert;

final class EntityLinkPropertiesBuilderImpl implements IEntityLinkPropertiesBuilder {

	private String keyPropertyName;
	private String foreignKeyPropertyName;

	EntityLinkPropertiesBuilderImpl() {}

	@Override
	public IEntityLinkPropertiesBuilder setKeyPropertyName(final String keyPropertyName) {
		Assert.paramNotEmpty(keyPropertyName, "keyPropertyName");
		this.keyPropertyName = keyPropertyName;
		return this;
	}

	@Override
	public IEntityLinkPropertiesBuilder setForeignKeyPropertyName(final String foreignKeyPropertyName) {
		Assert.paramNotEmpty(foreignKeyPropertyName, "foreignKeyPropertyName");
		this.foreignKeyPropertyName = foreignKeyPropertyName;
		return this;
	}

	@Override
	public IEntityLinkProperties build() {
		return new EntityLinkPropertiesImpl(keyPropertyName, foreignKeyPropertyName);
	}

}
