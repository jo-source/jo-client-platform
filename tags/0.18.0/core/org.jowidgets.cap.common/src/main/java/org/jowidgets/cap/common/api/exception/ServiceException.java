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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.jowidgets.util.Assert;

/**
 * The base exception of all service exceptions.
 * 
 * Remark: Service exception has no cause exception because they will
 * potentially be used to notify the client layer about exceptions, and
 * not all exception classes may be available in the client layer (e.g. PersistenceException).
 * To make the information of the cause available in the client layer, the stack trace will
 * be created from this exception and its cause when this exception will be created.
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = -7579908469741974763L;

	private final String userMessage;
	private final String stackTrace;

	public ServiceException() {
		this(null, null, null);
	}

	public ServiceException(final Throwable cause) {
		this(null, null, cause);
	}

	public ServiceException(final String message) {
		this(message, null, null);
	}

	public ServiceException(final String message, final Throwable cause) {
		this(message, null, cause);
	}

	public ServiceException(final String message, final String userMessage) {
		this(message, userMessage, null);
	}

	/**
	 * Creates a new ServiceException.
	 * 
	 * @param message The (technical) message of the exception
	 * 
	 * @param userMessage The (non technical, user domain specific) message that can be presented to the end user
	 * 
	 * @param cause The cause of the exception. Remark: The cause will not be returned with getCause(), instead the cause
	 *            will be used to create a complete stack trace for this exception on creation time.
	 */
	public ServiceException(final String message, final String userMessage, final Throwable cause) {
		super(message);
		this.userMessage = userMessage;

		final StringBuilder stackTraceBuilder = new StringBuilder(getStackTrace(this));

		if (cause != null) {
			if (cause instanceof ServiceException) {
				stackTraceBuilder.append(((ServiceException) cause).getStackTraceString());
			}
			else {
				stackTraceBuilder.append("\n" + getStackTrace(cause));
			}
		}

		stackTrace = stackTraceBuilder.toString();
	}

	/**
	 * Gets the user message of the exception.
	 * The user message is a message that could be presented to the end user.
	 * User messages should normally not presume technical / programming background
	 * of the user. Instead they should describe the problem (and possible solution)
	 * in the with words of the domain the application is designed for.
	 * 
	 * @return The user message or null, if no user message was set
	 */
	public final String getUserMessage() {
		return userMessage;
	}

	/**
	 * Gets the stack trace.
	 * 
	 * Remark: A ServiceException has no cause. When setting a cause on creation of a service exception,
	 * the cause will be used to create the stack trace from.
	 * 
	 * @return The stack trace
	 */
	public final String getStackTraceString() {
		return stackTrace;
	}

	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(final PrintStream stream) {
		Assert.paramNotNull(stream, "stream");
		stream.println(getStackTraceString());
	}

	@Override
	public void printStackTrace(final PrintWriter writer) {
		Assert.paramNotNull(writer, "writer");
		writer.println(getStackTraceString());
	}

	private static String getStackTrace(final Throwable throwable) {
		final StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
}
