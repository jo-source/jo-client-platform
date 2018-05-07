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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jowidgets.cap.service.hibernate.api.IKillSessionSupport;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.DefaultSystemTimeProvider;
import org.jowidgets.util.ISystemTimeProvider;
import org.jowidgets.util.maybe.IMaybe;
import org.jowidgets.util.maybe.Some;

/**
 * This class was designed to cancel a query of a hibernate session.
 * 
 * Unfortunately the {@link Session#cancelQuery()} doe's not always work reliable so in some cases
 * the session must be killed. For this, a {@link IKillSessionSupport} can be given to this class.
 * 
 * If the killAfterMillis duration has been reached since the first termination attempt, the underlying session
 * will be killed instead of canceling the query.
 * 
 * Each termination attempt must be done explicitly by invoking the method {@link #terminateQuery()}.
 * 
 * After the thread that executes the query has been finished the query execution, the method
 * {@link #rollbackConnectionIfNoTransactionIsActive()}
 * must be invoked in the query invoking thread to do necessary work on the session.
 */
final class QueryTerminator {

	private static final ILogger LOGGER = LoggerProvider.get(QueryTerminator.class);

	private final ISystemTimeProvider systemTimeProvider;
	private final EntityManagerFactory entityManagerFactory;
	private final Session session;
	private final CountDownLatch queryFinishedLatch;
	private final CountDownLatch queryStartedLatch;
	private final AtomicLong queryStartedTimestamp;
	private final String clientIdentifier;
	private final Long killAfterMillis;
	private final Long minQueryRuntimeMillis;

	private final AtomicReference<Long> firstTerminationAttempt;
	private final AtomicInteger terminationAttempts;
	private final AtomicInteger cancelInvocationsWithoutError;
	private final AtomicInteger killInvocationsWithoutError;

	private final IKillSessionSupport killSessionSupport;

	private IMaybe<Connection> connection;

	QueryTerminator(
		final EntityManagerFactory entityManagerFactory,
		final EntityManager entityManager,
		final CountDownLatch queryStartedLatch,
		final AtomicLong queryStartedTimestamp,
		final CountDownLatch queryFinishedLatch,
		final String clientIdentifier,
		final Long minQueryRuntimeMillis,
		final IKillSessionSupport killSessionSupport,
		final Long killAfterMillis) {
		this(
			DefaultSystemTimeProvider.getInstance(),
			entityManagerFactory,
			Assert.getParamNotNull(entityManager, "entityManager").unwrap(Session.class),
			queryStartedLatch,
			queryStartedTimestamp,
			queryFinishedLatch,
			clientIdentifier,
			minQueryRuntimeMillis,
			killSessionSupport,
			killAfterMillis);
	}

	QueryTerminator(
		final ISystemTimeProvider systemTimeProvider,
		final EntityManagerFactory entityManagerFactory,
		final Session session,
		final CountDownLatch queryStartedLatch,
		final AtomicLong queryStartedTimestamp,
		final CountDownLatch queryFinishedLatch,
		final String clientIdentifier,
		final Long minQueryRuntimeMillis,
		final IKillSessionSupport killSessionSupport,
		final Long killAfterMillis) {

		Assert.paramNotNull(systemTimeProvider, "systemTimeProvider");
		Assert.paramNotNull(entityManagerFactory, "entityManagerFactory");
		Assert.paramNotNull(session, "session");
		Assert.paramNotNull(queryStartedLatch, "queryStartedLatch");
		Assert.paramNotNull(queryStartedTimestamp, "queryStartedTimestamp");
		Assert.paramNotNull(queryFinishedLatch, "queryFinishedLatch");
		Assert.paramNotNull(clientIdentifier, "clientIdentifier");

		this.systemTimeProvider = systemTimeProvider;
		this.entityManagerFactory = entityManagerFactory;
		this.session = session;
		this.queryStartedLatch = queryStartedLatch;
		this.queryStartedTimestamp = queryStartedTimestamp;
		this.queryFinishedLatch = queryFinishedLatch;
		this.clientIdentifier = clientIdentifier;
		this.minQueryRuntimeMillis = minQueryRuntimeMillis;
		this.killAfterMillis = killAfterMillis;

		this.firstTerminationAttempt = new AtomicReference<Long>(null);
		this.terminationAttempts = new AtomicInteger(0);
		this.cancelInvocationsWithoutError = new AtomicInteger(0);
		this.killInvocationsWithoutError = new AtomicInteger(0);
		this.killSessionSupport = killSessionSupport;
	}

	/**
	 * Get's the underlying {@link Connection} of the session that runs the query to terminate.
	 * 
	 * @return The connection or null if no connection is available for the session
	 */
	synchronized Connection getConnection() {
		if (connection == null) {
			connection = new Some<Connection>(getConnectionOfSession(session));
		}
		//This may be null, e.g. if the thread was interrupted on first attempt.
		//In this case it's not reasonable to try to get a connection again, e.g. to rollback, because
		//there is nothing to rollback
		return connection.getValue();
	}

	/**
	 * Terminates the query.
	 * 
	 * This method can be invoked from any thread while a query is running on the given session in the query thread.
	 * By invoking this method, the query execution will be terminated. Because this doe's not always work reliable,
	 * this method can be invoked more than once, in case the query is still running.
	 * 
	 * If a {@link IKillSessionSupport} was given to this class and the kill after millis duration has been expired after
	 * the first termination attempt, the session will tried to kill with a separate statement.
	 */
	synchronized void terminateQuery() {

		try {
			waitForQueryStarted();
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}

		firstTerminationAttempt.compareAndSet(null, Long.valueOf(systemTimeProvider.currentTimeMillis()));
		try {
			if (session.isOpen() && queryFinishedLatch.getCount() > 0) {
				logInfoIfMoreThanOneAttempt();
				terminateOpenSession();
			}
		}
		catch (final Exception e) {
			LOGGER.error("Execption on cancel: ", e);
		}
	}

	/**
	 * Must be invoked after the query was canceled and termination has successfully finished to rollback the
	 * connection of the session.
	 */
	synchronized void rollbackConnectionIfNoTransactionIsActive() {
		try {
			if (session.isOpen() && !session.getTransaction().isActive()) {
				//If the session was canceled without any hibernate exception,
				//the connection is in a dirty state what may lead
				//to a timeout exception in future when connection will be recycled from
				//another session.
				//To avoid this, the connection will be rolled back. This only
				//works if the release mode (hibernate.connection.release_mode) is set to 'on_close'
				final Connection connectionOfSession = getConnection();
				if (connectionOfSession != null && !connectionOfSession.isClosed()) {
					LOGGER.debug("Try to rollback connection");
					connectionOfSession.rollback();
				}
			}
		}
		catch (final Throwable throwable) {
			LOGGER.debug("Execption on connection rollback after session cancel", throwable);
		}
	}

	private void waitForQueryStarted() throws InterruptedException {
		//avoid cancel query will be invoked before a query has been started
		queryStartedLatch.await();

		if (minQueryRuntimeMillis == null) {
			return;
		}

		final long currentTimeMillis = systemTimeProvider.currentTimeMillis();

		final long queryMethodStarted = queryStartedTimestamp.get();
		//In case 'queryMethodStarted == 0' and exception has been occurred before 
		//query started and no more sleep is necessary
		if (queryMethodStarted == 0) {
			return;
		}

		final long queryRuntime = currentTimeMillis - queryMethodStarted;
		if (queryRuntime < minQueryRuntimeMillis.longValue()) {
			//Avoid that cancel query will be invoked before a query has been started.
			//This is only a heuristic method but if it fails, termination should work with
			//next #terminateQuery() attempt.
			Thread.sleep(minQueryRuntimeMillis.longValue() - queryRuntime);
		}
	}

	private void terminateOpenSession() {
		if (mustSessionKilled()) {
			if (killSession()) {
				killInvocationsWithoutError.incrementAndGet();
			}
		}
		else {
			if (cancelQueryOfSession()) {
				cancelInvocationsWithoutError.incrementAndGet();
			}
		}
	}

	private boolean mustSessionKilled() {
		if (killSessionSupport == null || killAfterMillis == null || firstTerminationAttempt.get() == null) {
			return false;
		}
		return (getDurationSinceFirstAttempt()) > killAfterMillis.longValue();
	}

	private void logInfoIfMoreThanOneAttempt() {
		if (terminationAttempts.getAndIncrement() >= 1) {
			LOGGER.info(
					"Query is still running for session with client identifier '"
						+ clientIdentifier
						+ "' after '"
						+ getDurationSinceFirstAttempt()
						+ "' millis and "
						+ (terminationAttempts.get() - 1)
						+ " termination attempts. Cancel invocations without exception: "
						+ cancelInvocationsWithoutError
						+ ", kill invocation without exception: "
						+ killInvocationsWithoutError);
		}
	}

	private long getDurationSinceFirstAttempt() {
		return systemTimeProvider.currentTimeMillis() - firstTerminationAttempt.get().longValue();
	}

	private boolean cancelQueryOfSession() {
		try {
			session.cancelQuery();
			return true;
		}
		catch (final Exception e) {
			LOGGER.warn("Exception on cancel query for session with client identifier: '" + clientIdentifier + "'.", e);
			return false;
		}
	}

	private boolean killSession() {
		LOGGER.warn("Try to kill session with client identifier: '" + clientIdentifier + "'.");

		EntityManager entityManagerOfThread = null;
		Connection connectionOfThread = null;
		Statement statement = null;
		try {
			entityManagerOfThread = entityManagerFactory.createEntityManager();
			connectionOfThread = getConnectionOfSession(entityManagerOfThread.unwrap(Session.class));
			statement = connectionOfThread.createStatement();
			return killSessionSupport.killSession(clientIdentifier, statement);
		}
		catch (final Exception e) {
			LOGGER.error("Exception on kill session with client identifier: '" + clientIdentifier + "'.", e);
			return false;
		}
		finally {
			tryCloseStatement(statement);
			tryCloseConnection(connectionOfThread);
			tryCloseEntityManager(entityManagerOfThread);
		}
	}

	private void tryCloseStatement(final Statement statement) {
		if (statement == null) {
			return;
		}
		try {
			statement.close();
		}
		catch (final Throwable e) {
			LOGGER.error(
					"Error on statement close after kill session attempt for client identifier: '" + clientIdentifier + "'.",
					e);
		}
	}

	private void tryCloseConnection(final Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		}
		catch (final Throwable e) {
			LOGGER.error(
					"Error on connection close after kill session attempt for client identifier: '" + clientIdentifier + "'.",
					e);
		}
	}

	private void tryCloseEntityManager(final EntityManager entityManager) {
		if (entityManager == null) {
			return;
		}
		try {
			entityManager.close();
		}
		catch (final Throwable e) {
			LOGGER.error(
					"Error on entity manager close after kill session attempt for client identifier: '" + clientIdentifier + "'.",
					e);
		}
	}

	private static Connection getConnectionOfSession(final Session session) {
		final AtomicReference<Connection> result = new AtomicReference<Connection>();
		session.doWork(new Work() {
			@Override
			public void execute(final Connection connection) throws SQLException {
				result.set(connection);
			}
		});
		return result.get();
	}

}
