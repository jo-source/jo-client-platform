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

package org.jowidgets.cap.service.hibernate.oracle.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jowidgets.cap.service.hibernate.api.IKillSessionSupport;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.Assert;

import com.mchange.v2.c3p0.C3P0ProxyConnection;

public final class KillOracleSessionSupport implements IKillSessionSupport {

	private static final ILogger LOGGER = LoggerProvider.get(KillOracleSessionSupport.class);

	private Method unwrapMethod;
	private Method setClientIdentifierMethod;

	@Override
	public void setClientIdentifier(final String clientIdentifier, final Connection connection) {
		Assert.paramNotNull(clientIdentifier, "clientIdentifier");
		Assert.paramNotNull(connection, "connection");

		try {
			final Connection unwrappedConnection = unwrapConnection(connection);
			if (unwrappedConnection != null) {
				getOrCreateSetClientIdentifierMethod(unwrappedConnection).invoke(unwrappedConnection, clientIdentifier);
			}
		}
		catch (final Exception e) {
			LOGGER.warn("Can not set client identifier", e);
		}
	}

	private Connection unwrapConnection(final Connection connection) throws IllegalArgumentException,
			SecurityException,
			IllegalAccessException,
			InvocationTargetException,
			SQLException,
			NoSuchMethodException {
		if (connection instanceof C3P0ProxyConnection) {
			return (Connection) ((C3P0ProxyConnection) connection).rawConnectionOperation(
					getOrCreateUnwrapMethod(),
					C3P0ProxyConnection.RAW_CONNECTION,
					new Class[] {Connection.class});
		}
		else {
			return connection.unwrap(Connection.class);
		}
	}

	private Method getOrCreateUnwrapMethod() throws SecurityException, NoSuchMethodException {
		if (unwrapMethod == null) {
			unwrapMethod = Connection.class.getMethod("unwrap", new Class[] {Class.class});
		}
		return unwrapMethod;
	}

	private Method getOrCreateSetClientIdentifierMethod(final Object connection) throws SecurityException, NoSuchMethodException {
		if (setClientIdentifierMethod == null) {
			setClientIdentifierMethod = connection.getClass().getMethod("setClientIdentifier", String.class);
			setClientIdentifierMethod.setAccessible(true);
		}
		return setClientIdentifierMethod;
	}

	@Override
	public boolean killSession(final String clientIdentifier, final Statement statement) throws SQLException {
		Assert.paramNotNull(clientIdentifier, "clientIdentifier");
		Assert.paramNotNull(statement, "statement");

		final ResultSet resultSet = statement.executeQuery(
				"SELECT SID, SERIAL# from v$session where CLIENT_IDENTIFIER='" + clientIdentifier + "'");

		if (resultSet.next()) {
			final String sid = resultSet.getString(1);
			final String serial = resultSet.getString(2);
			final String sql = "ALTER SYSTEM KILL SESSION '" + sid + "," + serial + "'";
			LOGGER.info("Try to kill session: '" + sid + "," + serial + "'");
			statement.execute(sql);
			return true;
		}
		else {
			LOGGER.warn("No session found for client identifier: " + clientIdentifier);
			return false;
		}

	}

}
