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

package org.jowidgets.cap.remoting.client;

import java.io.IOException;
import java.io.InputStream;

import org.jowidgets.cap.remoting.common.IInputStreamRequest;
import org.jowidgets.cap.remoting.common.IInputStreamResponse;
import org.jowidgets.cap.remoting.common.InputStreamAvailableRequest;
import org.jowidgets.cap.remoting.common.InputStreamAvailableResponse;
import org.jowidgets.cap.remoting.common.InputStreamCloseRequest;
import org.jowidgets.cap.remoting.common.InputStreamCloseResponse;
import org.jowidgets.cap.remoting.common.InputStreamMarkRequest;
import org.jowidgets.cap.remoting.common.InputStreamMarkResponse;
import org.jowidgets.cap.remoting.common.InputStreamMarkSupportedRequest;
import org.jowidgets.cap.remoting.common.InputStreamMarkSupportedResponse;
import org.jowidgets.cap.remoting.common.InputStreamReadRequest;
import org.jowidgets.cap.remoting.common.InputStreamReadResponse;
import org.jowidgets.cap.remoting.common.InputStreamResetRequest;
import org.jowidgets.cap.remoting.common.InputStreamResetResponse;
import org.jowidgets.cap.remoting.common.InputStreamSkipRequest;
import org.jowidgets.cap.remoting.common.InputStreamSkipResponse;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.util.Assert;

final class InputStreamRequestCallback implements IInterimRequestCallback<IInputStreamRequest, IInputStreamResponse> {

	private final InputStream inputStream;

	InputStreamRequestCallback(final InputStream inputStream) {
		Assert.paramNotNull(inputStream, "inputStream");
		this.inputStream = inputStream;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void request(final IInterimResponseCallback callback, final IInputStreamRequest request) {
		Assert.paramNotNull(callback, "callback");
		Assert.paramNotNull(request, "request");

		if (request instanceof InputStreamReadRequest) {
			inputStreamReadRequest(callback, (InputStreamReadRequest) request);
		}
		else if (request instanceof InputStreamSkipRequest) {
			inputStreamSkipRequest(callback, (InputStreamSkipRequest) request);
		}
		else if (request instanceof InputStreamAvailableRequest) {
			inputStreamAvailableRequest(callback, (InputStreamAvailableRequest) request);
		}
		else if (request instanceof InputStreamCloseRequest) {
			inputStreamCloseRequest(callback, (InputStreamCloseRequest) request);
		}
		else if (request instanceof InputStreamMarkRequest) {
			inputStreamMarkRequest(callback, (InputStreamMarkRequest) request);
		}
		else if (request instanceof InputStreamResetRequest) {
			inputStreamResetRequest(callback, (InputStreamResetRequest) request);
		}
		else if (request instanceof InputStreamMarkSupportedRequest) {
			inputStreamMarkSupportedRequest(callback, (InputStreamMarkSupportedRequest) request);
		}
		else {
			throw new IllegalArgumentException("Input stream request type '"
				+ request.getClass().getName()
				+ "' is not supported.");
		}
	}

	private void inputStreamAvailableRequest(
		final IInterimResponseCallback<InputStreamAvailableResponse> responseCallback,
		final InputStreamAvailableRequest request) {
		try {
			final int available = inputStream.available();
			responseCallback.response(new InputStreamAvailableResponse(available));
		}
		catch (final IOException e) {
			responseCallback.response(new InputStreamAvailableResponse(e));
		}
	}

	private void inputStreamCloseRequest(
		final IInterimResponseCallback<InputStreamCloseResponse> responseCallback,
		final InputStreamCloseRequest request) {
		try {
			inputStream.close();
			responseCallback.response(new InputStreamCloseResponse());
		}
		catch (final IOException e) {
			responseCallback.response(new InputStreamCloseResponse(e));
		}
	}

	private void inputStreamMarkRequest(
		final IInterimResponseCallback<InputStreamMarkResponse> responseCallback,
		final InputStreamMarkRequest request) {
		inputStream.mark(request.getReadlimit());
		responseCallback.response(new InputStreamMarkResponse());
	}

	private void inputStreamMarkSupportedRequest(
		final IInterimResponseCallback<InputStreamMarkSupportedResponse> responseCallback,
		final InputStreamMarkSupportedRequest request) {
		responseCallback.response(new InputStreamMarkSupportedResponse(inputStream.markSupported()));
	}

	private void inputStreamReadRequest(
		final IInterimResponseCallback<InputStreamReadResponse> responseCallback,
		final InputStreamReadRequest request) {
		final byte[] bytes = new byte[request.getLength()];
		try {
			final int readBytes = inputStream.read(bytes);
			if (readBytes == -1) {
				responseCallback.response(new InputStreamReadResponse());
			}
			else if (readBytes == bytes.length) {
				responseCallback.response(new InputStreamReadResponse(bytes, readBytes));
			}
			else {//readBytes < bytes.length
				final byte[] trimmedBytes = new byte[readBytes];
				for (int i = 0; i < trimmedBytes.length; i++) {
					trimmedBytes[i] = bytes[i];
				}
				responseCallback.response(new InputStreamReadResponse(trimmedBytes, readBytes));
			}
		}
		catch (final IOException e) {
			responseCallback.response(new InputStreamReadResponse(e));
		}
	}

	private void inputStreamResetRequest(
		final IInterimResponseCallback<InputStreamResetResponse> responseCallback,
		final InputStreamResetRequest request) {
		try {
			inputStream.reset();
			responseCallback.response(new InputStreamResetResponse());
		}
		catch (final IOException e) {
			responseCallback.response(new InputStreamResetResponse(e));
		}
	}

	private void inputStreamSkipRequest(
		final IInterimResponseCallback<InputStreamSkipResponse> responseCallback,
		final InputStreamSkipRequest request) {
		try {
			responseCallback.response(new InputStreamSkipResponse(inputStream.skip(request.getSkip())));
		}
		catch (final IOException e) {
			responseCallback.response(new InputStreamSkipResponse(e));
		}
	}
}
