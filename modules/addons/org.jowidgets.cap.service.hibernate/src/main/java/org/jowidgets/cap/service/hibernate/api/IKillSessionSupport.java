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

package org.jowidgets.cap.service.hibernate.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Support to kill a database session in case that cancel request will not work
 */
public interface IKillSessionSupport {

	/**
	 * Sets a client identifier for the given connection.
	 * 
	 * The client identifier may be used in {@link #killSession(String, Statement)} later to identify the session to kill.
	 * 
	 * @param clientIdentifier The identifier to set
	 * @param connection The connection to set the identifier on
	 */
	void setClientIdentifier(String clientIdentifier, Connection connection);

	/**
	 * Kill the session with the given client identifier with help of the given statement.
	 * 
	 * @param clientIdentifier The identifier of the session to kill
	 * @param statement The statement that can be used to execute search and kill requests on
	 * 
	 * @return True if kill statement was executed, false otherwise (e.g. no session with identifier exists)
	 * 
	 * @throws SQLException if kill failed for some reason
	 */
	boolean killSession(String clientIdentifier, Statement statement) throws SQLException;

}
