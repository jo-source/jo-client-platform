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

package org.jowidgets.remoting.service.client.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jowidgets.remoting.client.api.IRemoteClient;
import org.jowidgets.remoting.client.api.RemoteClientToolkit;
import org.jowidgets.remoting.common.api.ICancelService;
import org.jowidgets.remoting.common.api.IInvocationCallbackService;
import org.jowidgets.remoting.service.common.api.ICancelListener;
import org.jowidgets.remoting.service.common.api.IInvocationResultCallback;
import org.jowidgets.remoting.service.common.api.IProgressCallback;
import org.jowidgets.remoting.service.common.api.IUserQuestionCallback;
import org.jowidgets.remoting.service.common.api.IUserQuestionResultCallback;

final class InvocationCallbackService implements IInvocationCallbackService {

	private final Map<Object, MethodInvocationContext> invocationContexts;
	private final ExecutorService executorService;

	InvocationCallbackService() {
		this.invocationContexts = new ConcurrentHashMap<Object, MethodInvocationContext>();
		this.executorService = Executors.newFixedThreadPool(100);
	}

	@Override
	public void setProgress(final Object invocationId, final Object progress) {
		executorService.execute(getProgressRunnable(invocationId, progress));
	}

	@Override
	public void userQuestion(final Object invocationId, final Object questionId, final Object question) {
		executorService.execute(getQuestionRunnable(invocationId, questionId, question));
	}

	@Override
	public void finished(final Object invocationId, final Object result) {
		executorService.execute(getFinishedRunnable(invocationId, result));
	}

	@Override
	public void exeption(final Object invocationId, final Throwable exception) {
		executorService.execute(getExceptionRunnable(invocationId, exception));
	}

	private Runnable getProgressRunnable(final Object invocationId, final Object progress) {
		return new Runnable() {
			@Override
			public void run() {
				final MethodInvocationContext context = invocationContexts.get(invocationId);
				if (context != null && context.getProgressCallback() != null) {
					context.getProgressCallback().setProgress(progress);
				}
			}
		};
	}

	private Runnable getFinishedRunnable(final Object invocationId, final Object result) {
		return new Runnable() {
			@Override
			public void run() {
				final MethodInvocationContext context = invocationContexts.get(invocationId);
				if (context != null && context.getResultCallback() != null) {
					context.getResultCallback().finished(result);
					invocationContexts.remove(invocationId);
				}
			}
		};
	}

	private Runnable getExceptionRunnable(final Object invocationId, final Throwable exception) {
		return new Runnable() {
			@Override
			public void run() {
				final MethodInvocationContext context = invocationContexts.get(invocationId);
				if (context != null && context.getResultCallback() != null) {
					context.getResultCallback().exeption(exception);
					invocationContexts.remove(invocationId);
				}
			}
		};
	}

	private Runnable getQuestionRunnable(final Object invocationId, final Object questionId, final Object question) {
		return new Runnable() {
			@Override
			public void run() {
				final MethodInvocationContext context = invocationContexts.get(invocationId);
				if (context != null && context.getUserQuestionCallback() != null) {
					final IUserQuestionResultCallback<Object> resultCallback = new IUserQuestionResultCallback<Object>() {
						@Override
						public void setResult(final Object result) {
							RemoteClientToolkit.getClient().getQuestionResultService(context.getServerId());
						}
					};
					context.getUserQuestionCallback().userQuestion(resultCallback, question);
				}
			}
		};
	}

	Object registerInvocation(
		final IInvocationResultCallback<?> resultCallback,
		final IProgressCallback<?> progressCallback,
		final IUserQuestionCallback<?, ?> userQuestionCallback,
		final long timeout,
		final Object serverId,
		final IRemoteClient remoteClient) {

		final Object invocationId = UUID.randomUUID();

		final MethodInvocationContext methodInvocation = new MethodInvocationContext(
			serverId,
			resultCallback,
			progressCallback,
			userQuestionCallback,
			timeout,
			System.currentTimeMillis());

		if (progressCallback != null) {
			progressCallback.addCancelListener(new ICancelListener() {
				@Override
				public void canceled() {
					executorService.execute(new Runnable() {
						@Override
						public void run() {
							final ICancelService cancelService = remoteClient.getCancelService(serverId);
							if (cancelService != null) {
								cancelService.canceled(invocationId);
							}
						}
					});
				}
			});
		}

		invocationContexts.put(invocationId, methodInvocation);

		return invocationId;
	}
}
