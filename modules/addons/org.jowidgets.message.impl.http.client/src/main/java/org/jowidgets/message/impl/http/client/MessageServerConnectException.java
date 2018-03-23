/*
 * Copyright (c) 2018, grossmann
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

package org.jowidgets.message.impl.http.client;

import java.net.ConnectException;

import org.apache.http.HttpHost;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.exception.IUserCapableMessageException;

/**
 * Will be thrown if there is some problem with the connection to the messaging server.
 */
public final class MessageServerConnectException extends ConnectException implements IUserCapableMessageException {

	private static final long serialVersionUID = -7463246491912931221L;

	private static final IMessage MESSAGE_WITH_HOST = Messages.getMessage("MessageServerConnectException.messageWithHost");
	private static final IMessage MESSAGE_WITHOUT_HOST = Messages.getMessage("MessageServerConnectException.messageWithoutHost");

	private final HttpHost host;

	MessageServerConnectException() {
		this(null);
	}

	MessageServerConnectException(final ConnectException cause) {
		this(null, cause);
	}

	MessageServerConnectException(final HttpHost host, final ConnectException cause) {
		super(getMessage(host, cause));
		this.host = host;
		initCause(cause);
	}

	private static String getMessage(final HttpHost host, final ConnectException cause) {
		if (host != null) {
			return "Connection to host '" + host + "' refused";
		}
		else {
			return "Connection refused";
		}
	}

	@Override
	public String getUserMessage() {
		if (host != null) {
			return MessageReplacer.replace(MESSAGE_WITH_HOST, host.toString());
		}
		else {
			return MESSAGE_WITHOUT_HOST.get();
		}
	}

}
