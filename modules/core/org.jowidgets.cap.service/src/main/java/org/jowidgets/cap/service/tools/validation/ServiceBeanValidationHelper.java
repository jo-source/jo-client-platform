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

package org.jowidgets.cap.service.tools.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.exception.BeansValidationException;
import org.jowidgets.cap.common.api.exception.BeansValidationException.KeyType;
import org.jowidgets.cap.common.api.exception.ServiceCanceledException;
import org.jowidgets.cap.common.api.exception.ServiceInterruptedException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.validation.BeanValidationResultUtil;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;
import org.jowidgets.validation.IValidationMessage;
import org.jowidgets.validation.MessageType;

public final class ServiceBeanValidationHelper {

	private static final IMessage THERE_ARE_VALIDATION_WARNINGS = Messages.getMessage(
			"ServiceBeanValidationHelper.thereAreValidationWarnings");

	private static final ILogger LOGGER = LoggerProvider.get(ServiceBeanValidationHelper.class);

	private static final IMessage PROCEED_ANYWAY = Messages.getMessage("ServiceBeanValidationHelper.proceedAnyway");

	private ServiceBeanValidationHelper() {}

	public static <BEAN_TYPE> void validate(
		final IBeanValidator<BEAN_TYPE> beanValidator,
		final boolean confirmValidationWarnings,
		final Collection<BEAN_TYPE> beans,
		final IBeanIdentityResolver<BEAN_TYPE> beanIdentityResolver,
		final IExecutionCallback executionCallback) {

		final Map<Object, IBeanValidationResult> validationResultMap = new HashMap<Object, IBeanValidationResult>();
		boolean hasError = false;
		final List<IBeanValidationResult> validationWarnings = new LinkedList<IBeanValidationResult>();
		int beanIndex = 0;
		BeansValidationException.KeyType keyType = KeyType.ID;
		for (final BEAN_TYPE bean : beans) {
			CapServiceToolkit.checkCanceled(executionCallback);
			final Object id = beanIdentityResolver.getId(bean);
			final Collection<IBeanValidationResult> validationResults = beanValidator.validate(bean);
			final IBeanValidationResult worstFirst = BeanValidationResultUtil.getWorstFirst(validationResults);
			if (worstFirst != null) {
				if (!worstFirst.getValidationResult().isValid()) {
					hasError = true;
					keyType = addResultsToMap(validationResultMap, id, beanIndex, keyType, worstFirst);
				}
				else {
					final IValidationMessage validationResult = worstFirst.getValidationResult().getWorstFirst();
					if (MessageType.WARNING.equals(validationResult.getType())) {
						validationWarnings.add(worstFirst);
						keyType = addResultsToMap(validationResultMap, id, beanIndex, keyType, worstFirst);
					}
				}
			}
			beanIndex++;
		}
		if (hasError) {
			throw new BeansValidationException(validationResultMap, keyType);
		}
		else if (confirmValidationWarnings && validationWarnings.size() > 0) {
			final String userQuestion = createUserQuestionString(validationWarnings);
			final UserQuestionResult userQuestionResult;

			try {
				userQuestionResult = executionCallback.userQuestion(userQuestion);
			}
			catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ServiceInterruptedException(e);
			}
			if (UserQuestionResult.NO.equals(userQuestionResult)) {
				throw new ServiceCanceledException();
			}
		}
	}

	private static KeyType addResultsToMap(
		final Map<Object, IBeanValidationResult> validationResultMap,
		final Object id,
		final int beanIndex,
		final KeyType lastKeyType,
		final IBeanValidationResult validationResult) {
		if (id != null) {
			validationResultMap.put(id, validationResult);
			checkKeyType(KeyType.ID, lastKeyType);
			return KeyType.ID;
		}
		else {
			validationResultMap.put(beanIndex, validationResult);
			checkKeyType(KeyType.INDEX, lastKeyType);
			return KeyType.INDEX;
		}
	}

	private static void checkKeyType(final KeyType expected, final KeyType current) {
		if (current != null && !current.equals(expected)) {
			LOGGER.warn("There are different key types for bean validation. Validation results may not be assigned correctly.");
		}
	}

	private static String createUserQuestionString(final List<IBeanValidationResult> validationWarnings) {
		final Set<String> messages = new HashSet<String>();
		final StringBuilder builder = new StringBuilder();
		builder.append(THERE_ARE_VALIDATION_WARNINGS.get() + "\n\n");
		for (final IBeanValidationResult validationWarning : validationWarnings) {
			final String messageText = validationWarning.getValidationResult().getWorstFirst().getText();
			if (!messages.contains(messageText)) {
				builder.append(messageText + "\n");
				messages.add(messageText);
			}
		}
		builder.append("\n" + PROCEED_ANYWAY.get());
		return builder.toString();
	}

}
