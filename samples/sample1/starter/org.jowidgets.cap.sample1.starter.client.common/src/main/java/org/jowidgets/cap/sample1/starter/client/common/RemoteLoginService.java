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

package org.jowidgets.cap.sample1.starter.client.common;

import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.login.ILoginResultCallback;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.login.ILoginService;
import org.jowidgets.invocation.common.impl.MessageBrokerId;
import org.jowidgets.message.api.MessageToolkit;
import org.jowidgets.message.impl.http.client.IMessageBroker;
import org.jowidgets.message.impl.http.client.MessageBrokerBuilder;

public class RemoteLoginService implements ILoginService {

	@Override
	public boolean doLogin() {
		final ILoginInterceptor loginInterceptor = new ILoginInterceptor() {
			@Override
			public void login(final ILoginResultCallback resultCallback, final String username, final String password) {

				final MessageBrokerBuilder builder = new MessageBrokerBuilder(MessageBrokerId.INVOCATION_IMPL_BROKER_ID);
				builder.setUrl("http://localhost:8080/").setUsername(username).setPassword(password);
				final IMessageBroker messageBroker = builder.build();

				MessageToolkit.addChannelBroker(messageBroker);
				MessageToolkit.addReceiverBroker(messageBroker);

				//TODO MG why does this not work
				//				IAuthorizationProviderService<DefaultPrincipal> authorizationService;
				//				try {
				//					authorizationService = ServiceProvider.getService(AuthorizationProviderServiceId.ID);
				//				}
				//				catch (final Throwable e) {
				//					resultCallback.denied("Not authorized");
				//					return;
				//				}
				//				if (authorizationService == null) {
				//					resultCallback.denied("Authorization service not available");
				//					return;
				//				}
				//
				//				final IResultCallback<DefaultPrincipal> authorizationResult = new IResultCallback<DefaultPrincipal>() {
				//
				//					@Override
				//					public void finished(final DefaultPrincipal result) {
				//						if (result != null) {
				//							SecurityContextHolder.setSecurityContext(result);
				//							//CHECKSTYLE:OFF
				//							System.out.println("AUTHORIZED AS: " + result);
				//							//CHECKSTYLE:ON
				//							resultCallback.granted();
				//						}
				//					}
				//
				//					@Override
				//					public void exception(final Throwable exception) {
				//						resultCallback.denied(exception.getLocalizedMessage());
				//					}
				//
				//					@Override
				//					public void timeout() {
				//						resultCallback.denied("Timeout");
				//					}
				//				};
				//
				//				final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
				//				resultCallback.addCancelListener(new ILoginCancelListener() {
				//					@Override
				//					public void canceled() {
				//						executionTask.cancel();
				//					}
				//				});
				//
				//				authorizationService.getPrincipal(authorizationResult, executionTask);

				resultCallback.granted();

			}
		};
		if (Toolkit.getLoginPane().login("Application1 ", loginInterceptor).isLoggedOn()) {
			return true;
		}
		else {
			return false;
		}
	}
}
