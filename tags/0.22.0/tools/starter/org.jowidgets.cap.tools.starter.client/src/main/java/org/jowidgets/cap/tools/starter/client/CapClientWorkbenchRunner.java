/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.cap.tools.starter.client;

import org.jowidgets.cap.remoting.common.RemotingBrokerId;
import org.jowidgets.message.api.IExceptionCallback;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.message.impl.http.client.IMessageBroker;
import org.jowidgets.message.impl.http.client.MessageBrokerBuilder;
import org.jowidgets.security.impl.http.client.BasicAuthenticationInitializer;
import org.jowidgets.workbench.api.IWorkbenchConfigurationService;
import org.jowidgets.workbench.api.IWorkbenchFactory;
import org.jowidgets.workbench.api.IWorkbenchRunner;
import org.jowidgets.workbench.impl.WorkbenchRunner;

public class CapClientWorkbenchRunner implements IWorkbenchRunner {

	private final IWorkbenchRunner workbenchRunner;
	private final String serverDefaultHost;

	private boolean messagingInitialized;

	public CapClientWorkbenchRunner(final String serverDefaultHost) {
		this.workbenchRunner = new WorkbenchRunner();
		this.serverDefaultHost = serverDefaultHost;
		this.messagingInitialized = false;
	}

	@Override
	public final void run(final IWorkbenchFactory workbenchFactory) {
		initializeMessaging();
		workbenchRunner.run(workbenchFactory);
	}

	@Override
	public final void run(final IWorkbenchFactory workbenchFactory, final IWorkbenchConfigurationService configurationService) {
		initializeMessaging();
		workbenchRunner.run(workbenchFactory, configurationService);
	}

	private void initializeMessaging() {
		if (!messagingInitialized) {
			final MessageBrokerBuilder builder = new MessageBrokerBuilder(RemotingBrokerId.DEFAULT_BROKER_ID);
			builder.setUrl(System.getProperty("server.url", serverDefaultHost));
			builder.setHttpRequestInitializer(BasicAuthenticationInitializer.getInstance());
			final IMessageBroker messageBroker = builder.build();
			MessageToolkit.addChannelBroker(messageBroker);
			MessageToolkit.addReceiverBroker(messageBroker);
			MessageToolkit.addExceptionCallback(RemotingBrokerId.DEFAULT_BROKER_ID, new IExceptionCallback() {
				@Override
				public void exception(final Throwable throwable) {
					//CHECKSTYLE:OFF
					throwable.printStackTrace();
					//CHECKSTYLE:ON
				}
			});
			messagingInitialized = true;
		}
	}
}