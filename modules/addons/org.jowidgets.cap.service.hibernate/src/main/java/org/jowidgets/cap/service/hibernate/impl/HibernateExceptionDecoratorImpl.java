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

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.persistence.OptimisticLockException;

import org.hibernate.JDBCException;
import org.hibernate.PessimisticLockException;
import org.hibernate.QueryTimeoutException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.exception.ServiceUnavailableException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IDecorator;

public class HibernateExceptionDecoratorImpl implements IDecorator<Throwable> {

	@Override
	public Throwable decorate(final Throwable original) {
		return decorate(original, original);
	}

	private Throwable decorate(final Throwable exception, final Throwable rootException) {
		if (exception instanceof ConstraintViolationException) {
			final ConstraintViolationException constViolationException = (ConstraintViolationException) exception;
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
		else if (exception instanceof OptimisticLockException && exception.getCause() instanceof StaleObjectStateException) {
			return getStaleBeanException((StaleObjectStateException) exception);
		}
		else if (exception instanceof UnresolvableObjectException) {
			return getDeletedBeanException((UnresolvableObjectException) exception);
		}
		else if (exception instanceof JDBCException && !excludeJDBCExceptionDecoration((JDBCException) exception)) {
			return decorateJDBCException((JDBCException) exception);
		}
		else if (exception instanceof InvocationTargetException
			&& ((InvocationTargetException) exception).getTargetException() != null) {
			return decorate(((InvocationTargetException) exception).getTargetException(), rootException);
		}
		else if (exception.getCause() != null) {
			return decorate(exception.getCause(), rootException);
		}

		return rootException;
	}

	private boolean excludeJDBCExceptionDecoration(final JDBCException exception) {
		return exception instanceof ConstraintViolationException
			|| exception instanceof DataException
			|| exception instanceof LockAcquisitionException
			|| exception instanceof PessimisticLockException
			|| exception instanceof QueryTimeoutException
			|| exception instanceof SQLGrammarException;
	}

	private Throwable decorateJDBCException(final JDBCException jdbcException) {
		if (hasResourcePoolTimeoutExceptionCause(jdbcException)) {
			return new ServiceUnavailableException(
				"All available database connections are in use",
				Messages.getString("HibernateExceptionDecoratorImpl.database_not_available"),
				jdbcException);
		}
		else {
			final SQLException sqlException = jdbcException.getSQLException();
			return new ServiceException(
				sqlException.getMessage(),
				getUserMessageForJDBCException(jdbcException, sqlException.getSQLState()),
				jdbcException);
		}
	}

	private String getUserMessageForJDBCException(final JDBCException jdbcException, final String sqlState) {
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

	private boolean hasResourcePoolTimeoutExceptionCause(final Throwable exception) {
		if (exception instanceof com.mchange.v2.resourcepool.TimeoutException) {
			return true;
		}
		else if (exception.getCause() != null) {
			return hasResourcePoolTimeoutExceptionCause(exception.getCause());
		}
		else {
			return false;
		}
	}

	private StaleBeanException getStaleBeanException(final StaleObjectStateException exception) {
		return new StaleBeanException(exception.getIdentifier(), exception);
	}

	private DeletedBeanException getDeletedBeanException(final UnresolvableObjectException exception) {
		return new DeletedBeanException(exception.getIdentifier());
	}

}
