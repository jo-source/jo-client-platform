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

package org.jowidgets.cap.ui.api.bean;

import java.util.Collection;
import java.util.List;

import org.jowidgets.api.validation.ValidationResult;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;

public interface IBeanProxy<BEAN_TYPE> extends
		IBeanDto,
		IPropertyChangeObservable,
		IBeanModificationStateObservable<BEAN_TYPE>,
		IBeanProcessStateObservable<BEAN_TYPE>,
		IBeanMessageStateObservable<BEAN_TYPE> {

	String META_PROPERTY_PROGRESS = IBeanProxy.class.getName() + "_META_PROPERTY_PROGRESS";
	String META_PROPERTY_MESSAGES = IBeanProxy.class.getName() + "_META_PROPERTY_MESSAGES";

	void setValue(String propertyName, Object value);

	ValidationResult validate();

	ValidationResult validate(String propertyName);

	ValidationResult validate(String propertyName, Object value);

	void update(IBeanDto beanDto);

	Collection<IBeanModification> getModifications();

	boolean hasModifications();

	boolean isModified(String propertyName);

	void undoModifications();

	void redoModifications();

	IExecutionTask getExecutionTask();

	boolean hasExecution();

	void setExecutionTask(IExecutionTask executionTask);

	void addMessage(IBeanMessage state);

	List<IBeanMessage> getMessages();

	IBeanMessage getFirstWorstMessage();

	IBeanMessage getFirstWorstMandatoryMessage();

	void clearMessages();

	void dispose();

	BEAN_TYPE getBean();
}
