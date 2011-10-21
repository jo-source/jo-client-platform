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

package org.jowidgets.cap.ui.impl;

import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFix;
import org.jowidgets.util.Assert;

final class BeanMessageBuilderImpl implements IBeanMessageBuilder {

	private final BeanMessageType type;
	private final List<IBeanMessageFix> fixes;

	private String message;
	private String description;
	private Throwable exception;
	private boolean fixMandatory;

	BeanMessageBuilderImpl(final BeanMessageType type) {
		Assert.paramNotNull(type, "type");
		this.type = type;
		this.fixes = new LinkedList<IBeanMessageFix>();
	}

	@Override
	public IBeanMessageBuilder setMessage(final String message) {
		this.message = message;
		return this;
	}

	@Override
	public IBeanMessageBuilder setDescription(final String description) {
		this.description = description;
		return this;
	}

	@Override
	public IBeanMessageBuilder setException(final Throwable exception) {
		this.exception = exception;
		return this;
	}

	@Override
	public IBeanMessageBuilder addFix(final IBeanMessageFix fix) {
		Assert.paramNotNull(fix, "fix");
		fixes.add(fix);
		return this;
	}

	@Override
	public IBeanMessageBuilder setFixMandatory(final boolean mandatory) {
		this.fixMandatory = mandatory;
		return this;
	}

	@Override
	public IBeanMessage build() {
		return new BeanMessageImpl(type, message, description, exception, fixes, fixMandatory);
	}

}
