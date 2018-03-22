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

package org.jowidgets.cap.common.api.exception;

/**
 * Will be thrown of the user has no authorization to execute a service or if the authorization can not be
 * determined, e.g. because a service or db that provides the authorization is not available.
 */
public class AuthorizationFailedException extends ServiceException {

	private static final long serialVersionUID = -7579908469741974763L;

	private final Object authorisation;

	/**
	 * Creates a new exception with given authorization
	 * 
	 * @param authorization The authorization that failed, may be null (e.g. in case of an unavailable authorization service)
	 */
	public AuthorizationFailedException(final Object authorization) {
		this(
			authorization,
			authorization != null
					? "User is not authorized for the authorization '" + authorization + "'" : "User is not authorized",
			null,
			null);
	}

	/**
	 * Creates a new exception with given message and user message and no specific authorization
	 * 
	 * @param message The (technical) message of the exception
	 * @param userMessage The (non technical, user domain specific) message that can be presented to the end user
	 */
	public AuthorizationFailedException(final String message, final String userMessage) {
		this(null, message, userMessage, null);
	}

	/**
	 * Creates a new exception with given message, user message and authorization
	 * 
	 * @param authorization The authorization that failed, may be null (e.g. in case of an unavailable authorization service)
	 * @param message The (technical) message of the exception
	 * @param userMessage The (non technical, user domain specific) message that can be presented to the end user
	 */
	public AuthorizationFailedException(final Object authorization, final String message, final String userMessage) {
		this(authorization, message, userMessage, null);
	}

	/**
	 * Creates a new exception with given message, user message,authorization and cause
	 * 
	 * @param authorization The authorization that failed, may be null (e.g. in case of an unavailable authorization service)
	 * @param message The (technical) message of the exception
	 * @param userMessage The (non technical, user domain specific) message that can be presented to the end user
	 * @param cause The cause of the exception. Remark: The cause will not be returned with getCause(), instead the cause
	 *            will be used to create a complete stack trace for this exception on creation time.
	 */
	public AuthorizationFailedException(
		final Object authorization,
		final String message,
		final String userMessage,
		final Throwable cause) {
		super(message, userMessage, cause);
		this.authorisation = authorization;
	}

	/**
	 * Gets the authorization that has been failed or null if unspecific
	 * 
	 * @return The authorization or null
	 */
	public Object getAuthorisation() {
		return authorisation;
	}

}
