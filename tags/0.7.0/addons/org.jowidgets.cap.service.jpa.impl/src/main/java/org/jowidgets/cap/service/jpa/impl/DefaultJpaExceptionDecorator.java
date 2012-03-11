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

package org.jowidgets.cap.service.jpa.impl;

import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IDecorator;

final class DefaultJpaExceptionDecorator implements IDecorator<Throwable> {

	@Override
	public Throwable decorate(final Throwable exception) {

		if (exception instanceof ServiceException) {
			return exception;
		}
		else if (exception instanceof OptimisticLockException) {
			final OptimisticLockException optimisticLockException = (OptimisticLockException) exception;
			return new StaleBeanException(getBeanId(optimisticLockException.getEntity()));
		}
		else if (exception instanceof EntityNotFoundException) {
			return new DeletedBeanException(null);
		}
		else if (exception instanceof ConstraintViolationException) {
			final ConstraintViolationException constraintViolationException = (ConstraintViolationException) exception;
			final Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
			if (EmptyCheck.isEmpty(constraintViolations)) {
				final ConstraintViolation<?> violation = constraintViolations.iterator().next();
				return new ExecutableCheckException(violation.getRootBean());
			}
			return new DeletedBeanException(null);
		}
		//TODO MG handle more jpa exceptions 

		return new ServiceException(exception);
	}

	private Object getBeanId(final Object bean) {
		if (bean instanceof IBean) {
			return ((IBean) bean).getId();
		}
		else {
			return null;
		}
	}
}
