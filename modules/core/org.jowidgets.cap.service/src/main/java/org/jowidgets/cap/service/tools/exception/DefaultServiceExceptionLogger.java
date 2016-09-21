/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.service.tools.exception;

import org.jowidgets.cap.common.api.exception.AuthorizationFailedException;
import org.jowidgets.cap.common.api.exception.BeanException;
import org.jowidgets.cap.common.api.exception.BeanValidationException;
import org.jowidgets.cap.common.api.exception.BeansValidationException;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.ForeignKeyConstraintViolationException;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.exception.UniqueConstraintViolationException;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.service.api.exception.IServiceExceptionLogger;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;

public class DefaultServiceExceptionLogger implements IServiceExceptionLogger {

	private static final String THIS_WRAPPER_FQCN = DefaultServiceExceptionLogger.class.getName();

	private final ILogger logger;

	public DefaultServiceExceptionLogger(final String loggerName) {
		this(loggerName, null);
	}

	public DefaultServiceExceptionLogger(final String loggerName, final String wrapperFQCN) {
		this.logger = LoggerProvider.get(loggerName, wrapperFQCN != null ? wrapperFQCN : THIS_WRAPPER_FQCN);
	}

	@Override
	public void log(final Class<?> serviceType, final Throwable original, final Throwable decorated) {
		if (!logServiceException(serviceType, original, decorated)) {
			logUnknownException(serviceType, original, decorated);
		}
	}

	protected void logUnknownException(final Class<?> serviceType, final Throwable original, final Throwable decorated) {
		logger.error("Unkwown exception invoking service of type '" + serviceType + "'", decorated);
		if (original != decorated) {
			logger.error("Caused by: ", original);
		}
	}

	protected boolean logServiceException(final Class<?> serviceType, final Throwable original, final Throwable decorated) {
		if (decorated instanceof ServiceException) {
			final ServiceException serviceException = (ServiceException) decorated;
			if (logServiceCanceledException(serviceType, original, serviceException)
				|| logAuthorizationFailedException(serviceType, original, serviceException)
				|| logBeanException(serviceType, original, serviceException)
				|| logBeansValidationException(serviceType, original, serviceException)
				|| logForeignKeyConstraintViolationException(serviceType, original, serviceException)) {
				return true;
			}
			else {
				//log as unknown exception
				return false;
			}
		}
		else {
			return false;
		}
	}

	protected boolean logServiceCanceledException(
		final Class<?> serviceType,
		final Throwable original,
		final ServiceException serviceException) {
		if (serviceException instanceof ServiceCanceledException) {
			if (logger.isInfoEnabled()) {
				logger.info("Service '" + serviceType + "' canceled by user");
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logAuthorizationFailedException(
		final Class<?> serviceType,
		final Throwable original,
		final ServiceException serviceException) {
		if (serviceException instanceof AuthorizationFailedException) {
			if (logger.isInfoEnabled()) {
				final AuthorizationFailedException authorizationException = (AuthorizationFailedException) serviceException;
				logger.info("Authorization failed for '" + authorizationException.getAuthorisation() + "'");
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logBeanException(
		final Class<?> serviceType,
		final Throwable original,
		final ServiceException serviceException) {
		if (serviceException instanceof BeanException) {
			final BeanException beanException = (BeanException) serviceException;
			if (logBeanValidationException(serviceType, original, beanException)
				|| logDeletedBeanException(serviceType, original, beanException)
				|| logExecutableCheckException(serviceType, original, beanException)
				|| logStaleBeanException(serviceType, original, beanException)) {
				return true;
			}
			else {
				//log as unknown exception
				return false;
			}
		}
		else {
			return false;
		}
	}

	protected boolean logBeanValidationException(
		final Class<?> serviceType,
		final Throwable original,
		final BeanException beanException) {
		if (beanException instanceof BeanValidationException) {
			if (logger.isInfoEnabled()) {
				final BeanValidationException beanValidationException = (BeanValidationException) beanException;
				final IBeanValidationResult validationResult = beanValidationException.getValidationResult();
				if (validationResult != null && validationResult.getValidationResult() != null) {
					logger.info(
							"Bean validation failed: "
								+ validationResult.getValidationResult().getAll()
								+ " for bean: "
								+ beanValidationException.getBeanId());
				}
				else {
					logger.info("Bean validation failed for bean: " + beanValidationException.getBeanId());
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logDeletedBeanException(
		final Class<?> serviceType,
		final Throwable original,
		final BeanException beanException) {
		if (beanException instanceof DeletedBeanException) {
			if (logger.isInfoEnabled()) {
				logger.info("Bean already deleted: " + beanException.getBeanId());
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logExecutableCheckException(
		final Class<?> serviceType,
		final Throwable original,
		final BeanException beanException) {
		if (beanException instanceof ExecutableCheckException) {
			if (logger.isInfoEnabled()) {
				logger.info(
						"Executable check failed for bean: " + beanException.getBeanId() + " " + beanException.getUserMessage());
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logStaleBeanException(
		final Class<?> serviceType,
		final Throwable original,
		final BeanException beanException) {
		if (beanException instanceof StaleBeanException) {
			if (logger.isInfoEnabled()) {
				logger.info("Bean is stale: " + beanException.getBeanId());
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logBeansValidationException(
		final Class<?> serviceType,
		final Throwable original,
		final ServiceException beanException) {
		if (beanException instanceof BeansValidationException) {
			if (logger.isInfoEnabled()) {
				final BeansValidationException beanValidationException = (BeansValidationException) beanException;
				logger.info("Bean validation failed: " + beanValidationException.getValidationResults());
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logForeignKeyConstraintViolationException(
		final Class<?> serviceType,
		final Throwable original,
		final ServiceException decorated) {
		if (decorated instanceof ForeignKeyConstraintViolationException) {
			if (logger.isInfoEnabled()) {
				logger.info("Foreign key constraint violation: " + decorated.getUserMessage());
			}
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean logUniqueConstraintViolationException(
		final Class<?> serviceType,
		final Throwable original,
		final ServiceException decorated) {
		if (decorated instanceof UniqueConstraintViolationException) {
			if (logger.isInfoEnabled()) {
				logger.info("Unique key constraint violation: " + decorated.getUserMessage());
			}
			return true;
		}
		else {
			return false;
		}
	}

}
