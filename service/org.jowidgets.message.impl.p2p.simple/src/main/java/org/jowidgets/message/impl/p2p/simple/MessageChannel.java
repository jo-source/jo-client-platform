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

package org.jowidgets.message.impl.p2p.simple;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Executor;

import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.IMessageChannel;
import org.jowidgets.util.Assert;

final class MessageChannel implements IMessageChannel {

	private final Peer clientPeer;
	private final Peer serverPeer;
	private final Executor sendExecutor;

	MessageChannel(final Peer clientPeer, final Peer serverPeer, final Executor sendExecutor) {
		Assert.paramNotNull(clientPeer, "clientPeer");
		Assert.paramNotNull(serverPeer, "serverPeer");
		Assert.paramNotNull(sendExecutor, "sendExecutor");
		Assert.paramNotNull(serverPeer.getHost(), "serverPeer.getHost()");
		if (serverPeer.getPort() == -1) {
			throw new IllegalArgumentException("The parameter 'serverPeer.getPort()' must not be undefined");
		}
		this.clientPeer = clientPeer;
		this.serverPeer = serverPeer;
		this.sendExecutor = sendExecutor;
	}

	@Override
	public void send(final Object message, final IExceptionCallback exceptionCallback) {
		sendExecutor.execute(new Runnable() {
			@Override
			public void run() {
				final Message p2pMessage = new Message(clientPeer, message);
				Socket socket = null;
				ObjectOutputStream oos = null;
				try {
					socket = new Socket(serverPeer.getHost(), serverPeer.getPort());
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(p2pMessage);
				}
				catch (final Exception e) {
					if (exceptionCallback != null) {
						exceptionCallback.exception(e);
					}
				}
				finally {
					try {
						socket.close();
					}
					catch (final Exception e) {
					}
					try {
						oos.close();
					}
					catch (final Exception e) {
					}
				}
			}
		});

	}
}
