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

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;

//TODO MG implement better converter
//TODO i18n
final class DefaultBeanExceptionConverter implements IBeanExceptionConverter {

	@Override
	public IBeanMessage convert(
		final List<? extends IBeanProxy<?>> processedBeans,
		final IBeanProxy<?> destinationBean,
		final Throwable throwable) {
		//CHECKSTYLE:OFF
		throwable.printStackTrace();
		//CHECKSTYLE:ON
		if (throwable instanceof ServiceException) {
			final ServiceException serviceException = ((ServiceException) throwable);
			final IBeanKey exceptionBean = serviceException.getBeanKey();
			String message = serviceException.getUserMessage();
			if (serviceException instanceof ExecutableCheckException) {
				if (message == null) {
					if (destinationBean.getId().equals(exceptionBean.getId())) {
						message = "Executable check failed!";
					}
					else {
						message = "Executable check of the bean '" + serviceException.getBeanKey().getId() + "' failed!";
					}
				}
				return new BeanMessageImpl(BeanMessageType.WARNING, message, throwable);
			}
			else if (serviceException instanceof StaleBeanException) {
				if (message == null) {
					if (destinationBean.getId().equals(exceptionBean.getId())) {
						message = "Stale data!";
					}
					else {
						message = "Stale data (id= '" + serviceException.getBeanKey().getId() + "')!";
					}
				}
				return new BeanMessageImpl(BeanMessageType.WARNING, message, throwable);
			}
			else if (serviceException instanceof DeletedBeanException) {
				if (message == null) {
					if (destinationBean.getId().equals(exceptionBean.getId())) {
						message = "Deleted data!";
					}
					else {
						message = "Deleted data (id= '" + serviceException.getBeanKey().getId() + "')!";
					}
				}
				return new BeanMessageImpl(BeanMessageType.ERROR, message, throwable);
			}
			else {
				return new BeanMessageImpl(BeanMessageType.ERROR, "Undefined runtime exception!", throwable);
			}
		}
		else {
			return new BeanMessageImpl(BeanMessageType.ERROR, "Undefined runtime exception!", throwable);
		}
	}

}
