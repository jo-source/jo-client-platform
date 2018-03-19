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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

final class HttpEntityStub implements HttpEntity {

	private final byte[] data;
	private final int consumeCount;
	private final CountDownLatch consumeLatch;
	private final AtomicBoolean consumed;

	/**
	 * Created a new instance
	 * 
	 * @param data The data the entity represents and that will be returned from getContent() as a {@link ByteArrayInputStream}
	 * @param consumeCount The number the latch will be count down after stream was consumed
	 * @param consumeLatch The latch that will be count down after stream was consumed
	 */
	HttpEntityStub(final byte[] data, final int consumeCount, final CountDownLatch consumeLatch) {
		this.data = data;
		this.consumeCount = consumeCount;
		this.consumeLatch = consumeLatch;

		this.consumed = new AtomicBoolean(false);
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public long getContentLength() {
		return data.length;
	}

	@Override
	public Header getContentType() {
		return null;
	}

	@Override
	public Header getContentEncoding() {
		return null;
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return new ByteArrayInputStream(data) {
			@Override
			public void close() throws IOException {
				consumeContent();
			}
		};
	}

	@Override
	public void writeTo(final OutputStream outstream) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean isStreaming() {
		return true;
	}

	@Override
	public void consumeContent() throws IOException {
		if (consumed.compareAndSet(false, true)) {
			for (int i = 0; i < consumeCount; i++) {
				consumeLatch.countDown();
			}
		}
	}

}
