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

package org.jowidgets.cap.remoting.server;

import java.io.IOException;
import java.io.InputStream;

import org.jowidgets.cap.remoting.common.InputStreamAvailableRequest;
import org.jowidgets.cap.remoting.common.InputStreamAvailableResponse;
import org.jowidgets.cap.remoting.common.InputStreamCloseRequest;
import org.jowidgets.cap.remoting.common.InputStreamCloseResponse;
import org.jowidgets.cap.remoting.common.InputStreamMarkRequest;
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
import org.jowidgets.invocation.service.common.tools.SyncInterimResponseCallback;
import org.jowidgets.util.Assert;

final class ServerInputStream extends InputStream {

	private final int index;
	private final IInterimRequestCallback<Object, Object> interimRequestCallback;

	ServerInputStream(final int index, final IInterimRequestCallback<Object, Object> interimRequestCallback) {
		Assert.paramNotNull(interimRequestCallback, "interimRequestCallback");
		this.index = index;
		this.interimRequestCallback = interimRequestCallback;
	}

	@Override
	public int read() throws IOException {
		final byte[] buffer = new byte[1];
		final int byteRead = read(buffer, 0, 1);
		if (byteRead == -1 || byteRead == 0) {
			return -1;
		}
		else {
			return buffer[0];
		}
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamReadRequest(index, len));
		final InputStreamReadResponse response = (InputStreamReadResponse) responseCallback.getResponseSynchronious();
		final byte[] resultBytes = response.getBytes();
		for (int i = 0; i < resultBytes.length; i++) {
			b[off + i] = resultBytes[i];
		}
		return response.getBytesRead();
	}

	@Override
	public long skip(final long n) throws IOException {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamSkipRequest(index, n));
		final InputStreamSkipResponse response = (InputStreamSkipResponse) responseCallback.getResponseSynchronious();
		return response.getSkipped();
	}

	@Override
	public int available() throws IOException {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamAvailableRequest(index));
		final InputStreamAvailableResponse response = (InputStreamAvailableResponse) responseCallback.getResponseSynchronious();
		return response.getAvailable();
	}

	@Override
	public void close() throws IOException {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamCloseRequest(index));
		final InputStreamCloseResponse response = (InputStreamCloseResponse) responseCallback.getResponseSynchronious();
		if (response.hasExcpetion()) {
			throw response.getException();
		}
	}

	@Override
	public synchronized void mark(final int readlimit) {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamMarkRequest(index, readlimit));
		try {
			responseCallback.getResponseSynchronious();
		}
		catch (final IOException e) {
			if (!(e.getCause() instanceof InterruptedException)) {
				throw new RuntimeException(e);
			}
			//else {
			//ignore, because work was interrupted 
			//}
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamResetRequest(index));
		final InputStreamResetResponse response = (InputStreamResetResponse) responseCallback.getResponseSynchronious();
		if (response.hasExcpetion()) {
			throw response.getException();
		}
	}

	@Override
	public boolean markSupported() {
		final InputStreamInterimResponseCallback<Object> responseCallback = new InputStreamInterimResponseCallback<Object>();
		interimRequestCallback.request(responseCallback, new InputStreamMarkSupportedRequest(index));
		final InputStreamMarkSupportedResponse response;
		try {
			response = (InputStreamMarkSupportedResponse) responseCallback.getResponseSynchronious();
		}
		catch (final IOException e) {
			if (e.getCause() instanceof InterruptedException) {
				//ignore, because work was interrupted 
				return false;
			}
			else {
				throw new RuntimeException(e);
			}

		}
		return response.isMarkSupported();
	}

	private class InputStreamInterimResponseCallback<RESPONSE_TYPE> implements IInterimResponseCallback<RESPONSE_TYPE> {

		private final SyncInterimResponseCallback<RESPONSE_TYPE> original;

		InputStreamInterimResponseCallback() {
			this.original = new SyncInterimResponseCallback<RESPONSE_TYPE>();
		}

		@Override
		public void response(final RESPONSE_TYPE response) {
			original.response(response);
		}

		public RESPONSE_TYPE getResponseSynchronious() throws IOException {
			try {
				return original.getResponseSynchronious();
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException(e);
			}
		}
	}
}
