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

package org.jowidgets.cap.sample1.starter.client.common;

import org.jowidgets.cap.sample1.ui.workbench.SampleWorkbench;
import org.jowidgets.invocation.common.impl.MessageBrokerId;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.message.impl.http.client.IMessageBroker;
import org.jowidgets.message.impl.http.client.MessageBrokerBuilder;
import org.jowidgets.workbench.impl.WorkbenchRunner;

public final class Sample1StarterClient {

	private Sample1StarterClient() {}

	public static void main(final String[] args) throws Exception {
		final MessageBrokerBuilder builder = new MessageBrokerBuilder(MessageBrokerId.INVOCATION_IMPL_BROKER_ID);
		builder.setUrl("http://localhost:8080/").setHttpRequestInitializer(BasicAuthenticationInitializer.getInstance());
		final IMessageBroker messageBroker = builder.build();
		MessageToolkit.addChannelBroker(messageBroker);
		MessageToolkit.addReceiverBroker(messageBroker);

		new WorkbenchRunner().run(new SampleWorkbench());
	}

}
