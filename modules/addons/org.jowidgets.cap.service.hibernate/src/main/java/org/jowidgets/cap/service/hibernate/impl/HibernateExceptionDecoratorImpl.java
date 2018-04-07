/*
 * Copyright (c) 2012, grossmann
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

import java.sql.SQLException;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.hibernate.JDBCException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IDecorator;

public class HibernateExceptionDecoratorImpl implements IDecorator<Throwable> {

	@Override
	public Throwable decorate(final Throwable original) {
		if (original instanceof PersistenceException) {
			final PersistenceException persistenceException = (PersistenceException) original;
			final Throwable cause = persistenceException.getCause();

			if (original instanceof OptimisticLockException) {
				if (cause instanceof StaleObjectStateException) {
					return getStaleBeanException((StaleObjectStateException) cause);
				}
			}
			else if (cause instanceof JDBCException) {
				return decorateJDBCException((JDBCException) cause);
			}
			else if (cause instanceof ConstraintViolationException) {
				final ConstraintViolationException constViolationException = (ConstraintViolationException) cause;
				final String message = constViolationException.getMessage();
				final String constraintName = constViolationException.getConstraintName();
				final String userBaseMessage = Messages.getString("HibernateExceptionDecoratorImpl.database_constraint_violated");
				final String userMessage;
				if (!EmptyCheck.isEmpty(constraintName)) {
					userMessage = userBaseMessage.replace("%1", "'" + constraintName + "'");
				}
				else {
					final SQLException sqlException = constViolationException.getSQLException();
					if (sqlException != null) {
						if (!EmptyCheck.isEmpty(sqlException.getLocalizedMessage())) {
							userMessage = sqlException.getLocalizedMessage();
						}
						else if (!EmptyCheck.isEmpty(sqlException.getMessage())) {
							userMessage = sqlException.getMessage();
						}
						else {
							userMessage = userBaseMessage.replace("%1", "");
						}
					}
					else {
						userMessage = userBaseMessage.replace("%1", "");
					}

				}
				return new ExecutableCheckException(null, message, userMessage);
			}
			//TODO MG handle more hibernate exceptions 
		}
		else if (original instanceof JDBCException) {
			return decorateJDBCException((JDBCException) original);
		}

		return original;
	}

	private Throwable decorateJDBCException(final JDBCException jdbcException) {
		final SQLException sqlException = jdbcException.getSQLException();
		return new ServiceException(
			sqlException.getMessage(),
			getUserMessageForJDBCException(sqlException.getSQLState()),
			jdbcException);
	}

	private String getUserMessageForJDBCException(final String sqlState) {
		if (sqlState == null) {
			return Messages.getString("HibernateExceptionDecoratorImpl.database_access_failed");
		}
		else if ("08003".equals(sqlState)) {
			return Messages.getString("HibernateExceptionDecoratorImpl.database_connection_closed");
		}
		else if (sqlState.startsWith("08")) {
			return Messages.getString("HibernateExceptionDecoratorImpl.database_connection_failed");
		}
		else {
			return Messages.getString("HibernateExceptionDecoratorImpl.database_access_failed");
		}
	}

	private StaleBeanException getStaleBeanException(final StaleObjectStateException exception) {
		return new StaleBeanException(exception.getIdentifier(), exception);
	}

}
