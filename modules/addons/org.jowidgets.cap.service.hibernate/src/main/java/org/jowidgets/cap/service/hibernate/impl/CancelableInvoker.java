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

package org.jowidgets.cap.service.hibernate.impl;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.IUpdatableResultCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.hibernate.api.IKillSessionSupport;
import org.jowidgets.cap.service.hibernate.api.KillSessionSupport;
import org.jowidgets.cap.service.jpa.api.EntityManagerHolder;
import org.jowidgets.util.Assert;
import org.jowidgets.util.DefaultSystemTimeProvider;
import org.jowidgets.util.ISystemTimeProvider;
import org.jowidgets.util.concurrent.IThreadInterruptListener;
import org.jowidgets.util.concurrent.IThreadInterruptObservable;

final class CancelableInvoker {

	private final ISystemTimeProvider systemTimeProvider;
	private final IThreadInterruptObservable threadInterruptObservable;

	private final Object original;
	private final Method method;
	private final Object[] args;
	private final int resultCallbackIndex;
	private final IResultCallback<Object> resultCallback;
	private final IExecutionCallback executionCallback;

	private final CountDownLatch queryFinishedLatch;
	private final CountDownLatch queryStartedLatch;
	private final AtomicLong queryStartedTimestamp;

	private final String clientIdentifier;
	private final long waitForCancelSleepMillis;
	private final IKillSessionSupport killSessionSupport;
	private final QueryTerminator queryTerminator;

	CancelableInvoker(
		final IThreadInterruptObservable threadInterruptObservable,
		final EntityManagerFactory entityManagerFactory,
		final Object original,
		final Method method,
		final Object[] args,
		final int resultCallbackIndex,
		final IResultCallback<Object> resultCallback,
		final IExecutionCallback executionCallback,
		final Long minQueryRuntimeMillis,
		final Long killAfterMillis,
		final long waitForCancelSleepMillis) {
		this(
			DefaultSystemTimeProvider.getInstance(),
			threadInterruptObservable,
			entityManagerFactory,
			original,
			method,
			args,
			resultCallbackIndex,
			resultCallback,
			executionCallback,
			minQueryRuntimeMillis,
			KillSessionSupport.getInstance(),
			killAfterMillis,
			waitForCancelSleepMillis);
	}

	CancelableInvoker(
		final ISystemTimeProvider systemTimeProvider,
		final IThreadInterruptObservable threadInterruptObservable,
		final EntityManagerFactory entityManagerFactory,
		final Object original,
		final Method method,
		final Object[] args,
		final int resultCallbackIndex,
		final IResultCallback<Object> resultCallback,
		final IExecutionCallback executionCallback,
		final Long minQueryRuntimeMillis,
		final IKillSessionSupport killSessionSupport,
		final Long killAfterMillis,
		final long waitForCancelSleepMillis) {

		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");
		Assert.paramNotNull(threadInterruptObservable, "threadInterruptObservable");
		Assert.paramNotNull(entityManagerFactory, "entityManagerFactory");
		Assert.paramNotNull(original, "original");

		this.systemTimeProvider = systemTimeProvider;
		this.threadInterruptObservable = threadInterruptObservable;
		this.original = original;
		this.method = method;
		this.args = args;
		this.resultCallbackIndex = resultCallbackIndex;
		this.resultCallback = resultCallback;
		this.executionCallback = executionCallback;

		this.queryFinishedLatch = new CountDownLatch(1);
		this.queryStartedLatch = new CountDownLatch(1);
		this.queryStartedTimestamp = new AtomicLong(0);

		this.clientIdentifier = UUID.randomUUID().toString();
		this.killSessionSupport = killSessionSupport;
		this.waitForCancelSleepMillis = waitForCancelSleepMillis;

		final EntityManager entityManager = EntityManagerHolder.get();
		if (entityManager != null) {
			this.queryTerminator = new QueryTerminator(
				entityManagerFactory,
				entityManager,
				queryStartedLatch,
				queryStartedTimestamp,
				queryFinishedLatch,
				clientIdentifier,
				minQueryRuntimeMillis,
				killSessionSupport,
				killAfterMillis);
		}
		else {
			this.queryTerminator = null;
		}

	}

	Object invoke() {
		final IResultCallback<Object> decoratedResultCallback = createDecoratedResultCallback();

		args[resultCallbackIndex] = decoratedResultCallback;

		try {
			CapServiceToolkit.checkCanceled(executionCallback);
			if (Thread.interrupted()) {
				throw new InterruptedException("Query invocation was interrupted.");
			}
			final IThreadInterruptListener interruptListener;
			if (queryTerminator != null) {
				addCancelListener(executionCallback);
				interruptListener = new ThreadInterruptListener();
				threadInterruptObservable.addInterruptListener(interruptListener);
			}
			else {
				interruptListener = null;
			}
			try {
				setClientIdentifierOnConnectionForThread(clientIdentifier);
				queryStartedTimestamp.set(systemTimeProvider.currentTimeMillis());
				queryStartedLatch.countDown();
				return method.invoke(original, args);
			}
			finally {
				if (interruptListener != null) {
					threadInterruptObservable.removeInterruptListener(interruptListener);
				}
			}
		}
		catch (final Exception e) {
			decoratedResultCallback.exception(e);
			return null;
		}

	}

	private void setClientIdentifierOnConnectionForThread(final String clientIndetifier) {
		if (queryTerminator == null || killSessionSupport == null) {
			return;
		}
		final Connection connection = queryTerminator.getConnection();
		if (connection == null) {
			return;
		}

		killSessionSupport.setClientIdentifier(clientIndetifier, connection);
	}

	private IResultCallback<Object> createDecoratedResultCallback() {
		if (resultCallback instanceof IUpdatableResultCallback<?, ?>) {
			return new DecoratedUpdateCallback();
		}
		else {
			return new DecoratedResultCallback();
		}
	}

	private void afterInvocationTerminated() {
		if (queryTerminator != null) {
			queryTerminator.afterQueryTerminated();
		}
	}

	private void addCancelListener(final IExecutionCallback executionCallback) {
		if (executionCallback != null && queryTerminator != null) {
			executionCallback.addExecutionCallbackListener(new CancelListener());
		}
	}

	private class ThreadInterruptListener implements IThreadInterruptListener {
		@Override
		public void interrupted(final Thread thread) {
			queryTerminator.terminateQuery();
		}
	}

	private class CancelListener implements IExecutionCallbackListener {

		@Override
		public void canceled() {
			try {
				queryTerminator.terminateQuery();
			}
			finally {
				try {
					awaitSessionCanceled();
				}
				catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		private void awaitSessionCanceled() throws InterruptedException {
			while (!queryFinishedLatch.await(waitForCancelSleepMillis, TimeUnit.MILLISECONDS)) {
				queryTerminator.terminateQuery();
			}
		}
	}

	private class DecoratedResultCallback implements IResultCallback<Object> {

		@Override
		public final void finished(final Object result) {
			queryStartedLatch.countDown();
			queryFinishedLatch.countDown();
			afterInvocationTerminated();
			resultCallback.finished(result);
		}

		@Override
		public final void exception(final Throwable exception) {
			queryStartedLatch.countDown();
			queryFinishedLatch.countDown();
			afterInvocationTerminated();
			if (Thread.interrupted() && !(executionCallback != null && executionCallback.isCanceled())) {
				resultCallback.exception(new InterruptedException("Query invocation was interrupted."));
			}
			else if (executionCallback != null
				&& executionCallback.isCanceled()
				&& !(exception instanceof ServiceCanceledException)) {
				resultCallback.exception(new ServiceCanceledException());
			}
			else {
				resultCallback.exception(exception);
			}
		}
	}

	private class DecoratedUpdateCallback extends DecoratedResultCallback implements IUpdatableResultCallback<Object, Object> {

		@SuppressWarnings("unchecked")
		@Override
		public void update(final Object result) {
			queryStartedLatch.countDown();
			queryFinishedLatch.countDown();
			afterInvocationTerminated();
			((IUpdatableResultCallback<Object, Object>) resultCallback).update(result);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void updatesFinished() {
			queryStartedLatch.countDown();
			queryFinishedLatch.countDown();
			afterInvocationTerminated();
			((IUpdatableResultCallback<Object, Object>) resultCallback).updatesFinished();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void exceptionOnUpdate(final Throwable exception) {
			queryStartedLatch.countDown();
			queryFinishedLatch.countDown();
			afterInvocationTerminated();
			((IUpdatableResultCallback<Object, Object>) resultCallback).exceptionOnUpdate(exception);
		}

	}

}
