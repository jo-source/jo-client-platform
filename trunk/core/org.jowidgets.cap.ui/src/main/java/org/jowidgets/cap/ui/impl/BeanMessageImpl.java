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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFix;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanMessageImpl implements IBeanMessage {

	private final BeanMessageType type;

	private final String actionText;
	private final String message;
	private final String description;
	private final Throwable exception;
	private final Date timestamp;
	private final boolean fixMandatory;
	private final List<IBeanMessageFix> fixes;

	private String lazyLabel;

	BeanMessageImpl(final BeanMessageType type, final String actionText, final String message) {
		this(type, actionText, message, null, null, new LinkedList<IBeanMessageFix>(), false);
	}

	BeanMessageImpl(final BeanMessageType type, final String actionText, final String message, final Throwable exception) {
		this(type, actionText, message, null, exception, new LinkedList<IBeanMessageFix>(), false);
	}

	BeanMessageImpl(
		final BeanMessageType type,
		final String actionText,
		final String message,
		final String description,
		final Throwable exception,
		final List<IBeanMessageFix> fixes,
		final boolean fixMandatory) {
		Assert.paramNotNull(type, "type");
		Assert.paramNotNull(fixes, "fixes");
		this.type = type;
		this.actionText = actionText;
		this.message = message;
		this.description = description;
		this.exception = exception;
		this.fixes = Collections.unmodifiableList(new LinkedList<IBeanMessageFix>(fixes));
		this.fixMandatory = fixMandatory;
		this.timestamp = new Date();
	}

	@Override
	public BeanMessageType getType() {
		return type;
	}

	@Override
	public String getLabel() {
		if (lazyLabel == null) {
			lazyLabel = createLabel();
		}
		return lazyLabel;
	}

	private String createLabel() {
		if (!EmptyCheck.isEmpty(actionText)) {
			return actionText + ": " + message;
		}
		else {
			return message;
		}
	}

	@Override
	public String getActionText() {
		return actionText;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return type + ": " + message;
	}

	@Override
	public boolean isFixMandatory() {
		return fixMandatory;
	}

	@Override
	public List<IBeanMessageFix> getFixes() {
		return fixes;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
