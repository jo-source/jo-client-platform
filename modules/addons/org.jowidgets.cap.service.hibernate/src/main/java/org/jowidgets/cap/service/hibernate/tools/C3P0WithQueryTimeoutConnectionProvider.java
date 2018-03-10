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

package org.jowidgets.cap.service.hibernate.tools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.connection.C3P0ConnectionProvider;
import org.hibernate.connection.ConnectionProvider;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

import com.mchange.v2.c3p0.C3P0ProxyConnection;

public final class C3P0WithQueryTimeoutConnectionProvider implements ConnectionProvider {

	public static final String QUERY_TIMEOUT_PARAMETER_NAME = "org.jowidgets.cap.service.hibernate.tools.C3P0WithQueryTimeoutConnectionProvider.query_timeout";

	private static final ILogger LOGGER = LoggerProvider.get(C3P0WithQueryTimeoutConnectionProvider.class);

	private final ConnectionProvider original;

	private Integer queryTimeout;

	public C3P0WithQueryTimeoutConnectionProvider() {
		this.original = new C3P0ConnectionProvider();
	}

	@Override
	public void configure(final Properties props) throws HibernateException {
		original.configure(props);
		queryTimeout = getQueryTimeout(props);
	}

	private static Integer getQueryTimeout(final Properties props) {
		final String timeoutProperty = props.getProperty(QUERY_TIMEOUT_PARAMETER_NAME);
		if (!EmptyCheck.isEmpty(timeoutProperty)) {
			try {
				return Integer.parseInt(timeoutProperty);
			}
			catch (final Exception e) {
				LOGGER.warn("Can not parse query_timeout property to long: " + timeoutProperty);
			}
		}
		return null;
	}

	@Override
	public Connection getConnection() throws SQLException {
		final Connection originalConnection = original.getConnection();
		if (queryTimeout == null || queryTimeout.longValue() <= 0) {
			return originalConnection;
		}
		return (Connection) Proxy.newProxyInstance(
				C3P0WithQueryTimeoutConnectionProvider.class.getClassLoader(),
				new Class[] {C3P0ProxyConnection.class},
				new ConnectionProxyInvocationHandler(originalConnection, queryTimeout.intValue()));
	}

	@Override
	public void closeConnection(final Connection conn) throws SQLException {
		original.closeConnection(conn);
	}

	@Override
	public void close() throws HibernateException {
		original.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return original.supportsAggressiveRelease();
	}

	private final class ConnectionProxyInvocationHandler implements InvocationHandler {

		private final Connection original;
		private final int queryTimeout;

		ConnectionProxyInvocationHandler(final Connection original, final int queryTimeout) {
			Assert.paramNotNull(original, "original");
			this.original = original;
			this.queryTimeout = queryTimeout;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			final Object result = method.invoke(original, args);
			if (result instanceof Statement) {
				LOGGER.debug("Set query timeout to: " + queryTimeout);
				((Statement) result).setQueryTimeout(queryTimeout);
			}
			return result;
		}

	}

}
