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
import java.util.LinkedList;
import java.util.List;

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
import org.jowidgets.tools.message.MessageReplacer;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.StringUtils;

final class BeanExceptionConverterImpl implements IBeanExceptionConverter {

	private final String executableCheckBean = Messages.getString("BeanExceptionConverterImpl.executableCheckBean");
	private final String executableCheck = Messages.getString("BeanExceptionConverterImpl.executableCheck");
	private final String staleDataBean = Messages.getString("BeanExceptionConverterImpl.staleDataBean");
	private final String staleData = Messages.getString("BeanExceptionConverterImpl.staleData");
	private final String deletedDataBean = Messages.getString("BeanExceptionConverterImpl.deletedDataBean");
	private final String deletedData = Messages.getString("BeanExceptionConverterImpl.deletedData");
	private final String fkConstraint = Messages.getString("BeanExceptionConverterImpl.fkConstraint");
	private final String uniqueConstraintNoProp = Messages.getString("BeanExceptionConverterImpl.uniqueConstraintNoProp");
	private final String uniqueConstraintSingleProp = Messages.getString("BeanExceptionConverterImpl.uniqueConstraintSingleProp");
	private final String uniqueConstraintPluralProp = Messages.getString("BeanExceptionConverterImpl.uniqueConstraintPluralProp");
	private final String undefinedRuntimeException = Messages.getString("BeanExceptionConverterImpl.undefinedRuntimeException");

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
			return convertBeanValidationException(shortMessage, (BeanValidationException) exception);
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

	private IBeanMessage convertBeanValidationException(final String shortMessage, final BeanValidationException exception) {
		final IBeanValidationResult firstWorstBeanResult = exception.getValidationResult();
		return new BeanMessageImpl(
			BeanMessageType.WARNING,
			shortMessage,
			firstWorstBeanResult.getValidationResult().getWorstFirst().getText(),
			exception);
	}

	private IBeanMessage convertExecutableCheckException(
		final String shortMessage,
		final IBeanProxy<?> bean,
		final ExecutableCheckException exception) {
		String message = exception.getUserMessage();
		if (EmptyCheck.isEmpty(message)) {
			if (bean.getId().equals(exception.getBeanId())) {
				message = executableCheck;
			}
			else {
				message = MessageReplacer.replace(executableCheckBean, exception.getBeanId().toString());
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
				message = staleData;
			}
			else {
				message = MessageReplacer.replace(staleDataBean, exception.getBeanId().toString());
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
			if (bean.getId().equals(exception.getBeanId())) {
				message = deletedData;
			}
			else {
				message = MessageReplacer.replace(deletedDataBean, exception.getBeanId().toString());
			}
		}
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertForeignKeyConstraintViolationException(final String shortMessage, final Throwable rootThrowable) {
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, fkConstraint, rootThrowable);
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
			MessageReplacer.replace(uniqueConstraintSingleProp, labels);
			message = MessageReplacer.replace(uniqueConstraintSingleProp, labels);
		}
		else if (propertyLabels.size() > 1) {
			message = MessageReplacer.replace(uniqueConstraintPluralProp, labels);
		}
		else {
			message = uniqueConstraintNoProp;
		}
		return new BeanMessageImpl(BeanMessageType.WARNING, shortMessage, message, exception);
	}

	private IBeanMessage convertUndefinedServiceException(final String shortMessage, final ServiceException exception) {
		//CHECKSTYLE:OFF
		exception.printStackTrace();
		//CHECKSTYLE:ON
		final String userMessage = exception.getUserMessage();
		if (!EmptyCheck.isEmpty(userMessage)) {
			return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, userMessage, exception);
		}
		else {
			return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, undefinedRuntimeException, exception);
		}
	}

	private IBeanMessage convertUndefinedException(final String shortMessage, final Throwable throwable) {
		//CHECKSTYLE:OFF
		throwable.printStackTrace();
		//CHECKSTYLE:ON
		return new BeanMessageImpl(BeanMessageType.ERROR, shortMessage, undefinedRuntimeException, throwable);
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
