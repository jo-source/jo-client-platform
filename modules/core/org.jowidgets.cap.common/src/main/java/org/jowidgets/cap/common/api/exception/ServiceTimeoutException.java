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

package org.jowidgets.cap.common.api.exception;

/**
 * Will be invoked if a service execution was terminated because a timeout reached
 */
public class ServiceTimeoutException extends ServiceException {

	private static final long serialVersionUID = 3201710463275013482L;

	/**
	 * Creates a new instance.
	 */
	public ServiceTimeoutException() {
		super();
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param cause The cause of the exception. Remark: The cause will not be returned with getCause(), instead the cause
	 *            will be used to create a complete stack trace for this exception on creation time.
	 */
	public ServiceTimeoutException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param message The message of the exception
	 */
	public ServiceTimeoutException(final String message) {
		super(message);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param message The message of the exception
	 * 
	 * @param cause The cause of the exception. Remark: The cause will not be returned with getCause(), instead the cause
	 *            will be used to create a complete stack trace for this exception on creation time.
	 */
	public ServiceTimeoutException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param message The (technical) message of the exception
	 * 
	 * @param userMessage The (non technical, user domain specific) message that can be presented to the end user
	 */
	public ServiceTimeoutException(final String message, final String userMessage) {
		super(message, userMessage);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param message The (technical) message of the exception
	 * 
	 * @param userMessage The (non technical, user domain specific) message that can be presented to the end user
	 * 
	 * @param cause The cause of the exception. Remark: The cause will not be returned with getCause(), instead the cause
	 *            will be used to create a complete stack trace for this exception on creation time.
	 */
	public ServiceTimeoutException(final String message, final String userMessage, final Throwable cause) {
		super(message, userMessage, cause);
	}

}
