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

package org.jowidgets.cap.ui.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.exception.AuthorizationFailedException;
import org.jowidgets.cap.common.api.exception.BeanException;
import org.jowidgets.cap.common.api.exception.BeanValidationException;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.ForeignKeyConstraintViolationException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.exception.UniqueConstraintViolationException;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.StringUtils;

final class BeanExceptionConverterImpl implements IBeanExceptionConverter {

	private static final ILogger LOGGER = LoggerProvider.get(BeanExceptionConverterImpl.class);

	private static final IMessage EXECUTABLE_CHECK_BEAN = Messages.getMessage("BeanExceptionConverterImpl.executableCheckBean");
	private static final IMessage EXECUTABLE_CHECK = Messages.getMessage("BeanExceptionConverterImpl.executableCheck");
	private static final IMessage STALE_DATA_BEAN = Messages.getMessage("BeanExceptionConverterImpl.staleDataBean");
	private static final IMessage STALE_DATA = Messages.getMessage("BeanExceptionConverterImpl.staleData");
	private static final IMessage DELETED_DATA_BEAN = Messages.getMessage("BeanExceptionConverterImpl.deletedDataBean");
	private static final IMessage DELETED_DATA = Messages.getMessage("BeanExceptionConverterImpl.deletedData");
	private static final IMessage FK_CONSTRAINT = Messages.getMessage("BeanExceptionConverterImpl.fkConstraint");
	private static final IMessage UNIQUE_CONSTRAINT_NO_PROP = Messages.getMessage("BeanExceptionConverterImpl.uniqueConstraintNoProp");
	private static final IMessage UNIQUE_CONSTRAINT_SINGLE_PROP = Messages.getMessage("BeanExceptionConverterImpl.uniqueConstraintSingleProp");
	private static final IMessage UNIQUE_CONSTRAINT_PLURAL_PROP = Messages.getMessage("BeanExceptionConverterImpl.uniqueConstraintPluralProp");
	private static final IMessage AUTHORIZATION_FAILED = Messages.getMessage("BeanExceptionConverterImpl.authorizationFailed");
	private static final IMessage UNDEFINED_RUNTIME_EXCEPTION = Messages.getMessage("BeanExceptionConverterImpl.undefinedRuntimeException");

	@Override
	public IBeanMessage convert(
		final String shortMessage,
		final List<? extends IBeanProxy<?>> processedBeans,
		final IBeanProxy<?> destinationBean,
		final Throwable throwable) {
		if (throwable instanceof ServiceException) {
			return convertServiceException(shortMessage, destinationBean, (ServiceException) throwable);
		}
		else {
			return convertUndefinedException(shortMessage, throwable);
		}
	}

	private IBeanMessage convertServiceException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final ServiceException exception) {
		if (exception instanceof BeanException) {
			return convertBeanException(shortMessage, bean, (BeanException) exception);
		}
		else if (exception instanceof AuthorizationFailedException) {
			return convertAuthorizationFailedException(shortMessage, (AuthorizationFailedException) exception);
		}
		else if (exception instanceof ForeignKeyConstraintViolationException) {
			return convertForeignKeyConstraintViolationException(shortMessage, exception);
		}
		else if (exception instanceof UniqueConstraintViolationException) {
			return convertUniqueConstraintViolationException(shortMessage, bean, (UniqueConstraintViolationException) exception);
		}
		else {
			return convertUndefinedServiceException(shortMessage, exception);
		}
	}

	private IBeanMessage convertBeanException(final String shortMessage, final IBeanProxy<?> bean, final BeanException exception) {
		if (exception instanceof BeanValidationException) {
			return convertBeanValidationException(shortMessage, bean, (BeanValidationException) exception);
		}
		else if (exception instanceof ExecutableCheckException) {
			return convertExecutableCheckException(shortMessage, bean, (ExecutableCheckException) exception);
		}
		else if (exception instanceof StaleBeanException) {
			return convertStaleBeanException(shortMessage, bean, (StaleBeanException) exception);
		}
		else if (exception instanceof DeletedBeanException) {
			return convertDeletedBeanException(shortMessage, bean, (DeletedBeanException) exception);
		}
		else {
			return convertUndefinedServiceException(shortMessage, exception);
		}
	}

	private IBeanMessage convertBeanValidationException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final BeanValidationException exception) {
		final IBeanValidationResult firstWorstBeanResult = exception.getValidationResult();
		final String propertyName = firstWorstBeanResult.getPropertyName();
		final Collection<String> propertyLabel = getPropertyLabels(bean, Collections.singleton(propertyName));
		final String labels = StringUtils.concatElementsSeparatedBy(propertyLabel, ',');
		final String message = labels + " - " + firstWorstBeanResult.getValidationResult().getWorstFirst().getText();
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertExecutableCheckException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final ExecutableCheckException exception) {
		String message = exception.getUserMessage();
		if (EmptyCheck.isEmpty(message)) {
			if (bean.getId().equals(exception.getBeanId())) {
				message = EXECUTABLE_CHECK.get();
			}
			else {
				message = MessageReplacer.replace(EXECUTABLE_CHECK_BEAN.get(), exception.getBeanId().toString());
			}
		}
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertStaleBeanException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final StaleBeanException exception) {
		String message = exception.getUserMessage();
		if (EmptyCheck.isEmpty(message)) {
			if (bean.getId().equals(exception.getBeanId())) {
				message = STALE_DATA.get();
			}
			else {
				message = MessageReplacer.replace(STALE_DATA_BEAN.get(), exception.getBeanId().toString());
			}
		}
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertDeletedBeanException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final DeletedBeanException exception) {
		String message = exception.getUserMessage();
		if (message == null) {
			if (exception.getBeanId() == null || bean.getId().equals(exception.getBeanId())) {
				message = DELETED_DATA.get();
			}
			else {
				message = MessageReplacer.replace(DELETED_DATA_BEAN.get(), exception.getBeanId().toString());
			}
		}
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertAuthorizationFailedException(
		final String shortMessage,
		final AuthorizationFailedException exception) {
		return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, AUTHORIZATION_FAILED.get(), exception);
	}

	private IBeanMessage convertForeignKeyConstraintViolationException(final String shortMessage, final Throwable rootThrowable) {
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, FK_CONSTRAINT.get(), rootThrowable);
	}

	private IBeanMessage convertUniqueConstraintViolationException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final UniqueConstraintViolationException exception) {
		final Collection<String> propertyNames = exception.getPropertyNames();
		final Collection<String> propertyLabels = getPropertyLabels(bean, propertyNames);
		final String labels = StringUtils.concatElementsSeparatedBy(propertyLabels, ',');
		final String message;
		if (propertyLabels.size() == 1) {
			MessageReplacer.replace(UNIQUE_CONSTRAINT_SINGLE_PROP.get(), labels);
			message = MessageReplacer.replace(UNIQUE_CONSTRAINT_SINGLE_PROP.get(), labels);
		}
		else if (propertyLabels.size() > 1) {
			message = MessageReplacer.replace(UNIQUE_CONSTRAINT_PLURAL_PROP.get(), labels);
		}
		else {
			message = UNIQUE_CONSTRAINT_NO_PROP.get();
		}
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertUndefinedServiceException(final String shortMessage, final ServiceException exception) {
		LOGGER.error("Undefined service exception", exception);
		final String userMessage = exception.getUserMessage();
		if (!EmptyCheck.isEmpty(userMessage)) {
			return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, userMessage, exception);
		}
		else {
			return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, UNDEFINED_RUNTIME_EXCEPTION.get(), exception);
		}
	}

	private IBeanMessage convertUndefinedException(final String shortMessage, final Throwable throwable) {
		LOGGER.error("Undefined service exception", throwable);
		return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, UNDEFINED_RUNTIME_EXCEPTION.get(), throwable);
	}

	private Collection<String> getPropertyLabels(final IBeanProxy<?> destinationBean, final Collection<String> propertyNames) {
		final List<String> result = new LinkedList<String>();
		if (destinationBean != null) {
			for (final String propertyName : propertyNames) {
				final IAttribute<Object> attribute = destinationBean.getAttribute(propertyName);
				if (attribute != null) {
					result.add(attribute.getCurrentLabel());
				}
			}
		}
		return result;
	}
}
