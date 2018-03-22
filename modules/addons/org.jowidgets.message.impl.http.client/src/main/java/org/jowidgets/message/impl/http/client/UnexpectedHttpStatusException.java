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

import java.io.IOException;

import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.exception.IUserCapableMessageException;

/**
 * Will be thrown if an http status occurred that was not expected
 */
public final class UnexpectedHttpStatusException extends IOException implements IUserCapableMessageException {

	private static final long serialVersionUID = -7463246491912931221L;

	private static final IMessage INTERNAL_SERVER_ERROR_500 = Messages.getMessage(
			"UnexpectedHttpStatusException.internalServerError_500");

	private static final IMessage SERVICE_UNAVAILABLE_ERROR_503 = Messages.getMessage(
			"UnexpectedHttpStatusException.serviceUnavailable_503");

	private static final IMessage SERVICE_TIMEOUT_504 = Messages.getMessage("UnexpectedHttpStatusException.serviceTimeout_504");

	private static final IMessage UNEXPECTED_SERVER_ERROR_5XX = Messages.getMessage(
			"UnexpectedHttpStatusException.unexpectedServerError_5xx");

	private static final IMessage UNEXPECTED_CLIENT_ERROR_4XX = Messages.getMessage(
			"UnexpectedHttpStatusException.unexpectedClientError_4xx");

	private static final IMessage UNEXPECTED_STATUS_XXX = Messages.getMessage(
			"UnexpectedHttpStatusException.unexpectedStatus_XXX");

	private final int statusCode;

	UnexpectedHttpStatusException(final int statusCode, final String message) {
		super(message);
		this.statusCode = statusCode;
	}

	@Override
	public String getUserMessage() {
		if (statusCode == 500) {
			return MessageReplacer.replace(INTERNAL_SERVER_ERROR_500, "" + statusCode);
		}
		else if (statusCode == 503) {
			return MessageReplacer.replace(SERVICE_UNAVAILABLE_ERROR_503, "" + statusCode);
		}
		else if (statusCode == 504) {
			return MessageReplacer.replace(SERVICE_TIMEOUT_504, "" + statusCode);
		}
		else if (statusCode > 500) {
			return MessageReplacer.replace(UNEXPECTED_SERVER_ERROR_5XX, "" + statusCode);
		}
		else if (statusCode >= 400) {
			return MessageReplacer.replace(UNEXPECTED_CLIENT_ERROR_4XX, "" + statusCode);
		}
		else {
			return MessageReplacer.replace(UNEXPECTED_STATUS_XXX, "" + statusCode);
		}
	}

	/**
	 * Gets the http status code
	 * 
	 * @return The status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

}
