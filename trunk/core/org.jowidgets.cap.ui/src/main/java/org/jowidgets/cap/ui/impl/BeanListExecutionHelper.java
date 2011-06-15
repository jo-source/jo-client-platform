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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.QuestionResult;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.execution.UserQuestionResult;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanState;
import org.jowidgets.cap.ui.api.execution.BeanExecutionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.api.execution.IUserAnswerCallback;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.util.ValueHolder;

@SuppressWarnings({"rawtypes", "unchecked"})
final class BeanListExecutionHelper {

	private final IBeanListModel<Object> listModel;
	private final Collection<? extends IBeanProxy> beans;
	private final IBeanExceptionConverter exceptionConverter;
	private final BeanExecutionPolicy beanExecutionPolicy;
	private final IUiThreadAccess uiThreadAccess;

	BeanListExecutionHelper(
		final IBeanListModel listModel,
		final Collection<? extends IBeanProxy> beans,
		final BeanExecutionPolicy beanExecutionPolicy,
		final IBeanExceptionConverter exceptionConverter) {
		super();
		this.listModel = listModel;
		this.beans = beans;
		this.beanExecutionPolicy = beanExecutionPolicy;
		this.exceptionConverter = exceptionConverter;
		this.uiThreadAccess = Toolkit.getUiThreadAccess();
	}

	List<List<IBeanProxy>> prepareExecutions() {
		final List<List<IBeanProxy>> result = new LinkedList<List<IBeanProxy>>();

		if (BeanExecutionPolicy.SERIAL == beanExecutionPolicy) {
			final IExecutionTask executionTask = createExecutionTask();
			final List<IBeanProxy> subList = new LinkedList<IBeanProxy>();
			result.add(subList);
			for (final IBeanProxy bean : beans) {
				if (bean.getExecutionTask() == null) {
					bean.setExecutionTask(executionTask);
					subList.add(bean);
				}
			}
		}
		else {
			for (final IBeanProxy bean : beans) {
				final List<IBeanProxy> subList = new LinkedList<IBeanProxy>();
				result.add(subList);
				if (bean.getExecutionTask() == null) {
					final IExecutionTask executionTask = createExecutionTask();
					bean.setExecutionTask(executionTask);
					subList.add(bean);
				}
			}
		}
		listModel.fireBeansChanged();
		return result;
	}

	IResultCallback<List<IBeanDto>> createResultCallback(final List<IBeanProxy> beansToExecute) {
		return new IResultCallback<List<IBeanDto>>() {
			@Override
			public void finished(final List<IBeanDto> result) {
				invokeAfterExecutionLater(beansToExecute, result);
			}

			@Override
			public void exception(final Throwable exception) {
				invokeOnExceptionLater(beansToExecute, exception);
			}

			@Override
			public void timeout() {
				invokeOnExceptionLater(beansToExecute, new TimeoutException());
			}
		};
	}

	void invokeAfterExecutionLater(final List<IBeanProxy> executedBeans, final List<IBeanDto> result) {
		uiThreadAccess.invokeLater(new Runnable() {
			@Override
			public void run() {
				afterExecution(executedBeans, result);
			}
		});
	}

	void afterExecution(final List<IBeanProxy> executedBeans, final List<IBeanDto> result) {
		if (result != null) {
			final Map<Object, IBeanDto> resultMap = new HashMap<Object, IBeanDto>();
			for (final IBeanDto beanDto : result) {
				resultMap.put(beanDto.getId(), beanDto);
			}

			for (final IBeanProxy bean : executedBeans) {
				final IBeanDto updatedBean = resultMap.get(bean.getId());
				if (updatedBean != null) {
					bean.update(updatedBean);
				}
				//TODO MG //else {}
			}
		}

		for (final IBeanProxy bean : executedBeans) {
			bean.setExecutionTask(null);
		}
		listModel.fireBeansChanged();
	}

	void invokeOnExceptionLater(final List<IBeanProxy> executedBeans, final Throwable exception) {
		uiThreadAccess.invokeLater(new Runnable() {
			@Override
			public void run() {
				onExecption(executedBeans, exception);
			}
		});
	}

	void onExecption(final List<IBeanProxy> executedBeans, final Throwable exception) {
		final IBeanState beanState = exceptionConverter.convert(exception);
		for (final IBeanProxy bean : executedBeans) {
			bean.setState(beanState);
			bean.setExecutionTask(null);
		}
		listModel.fireBeansChanged();
	}

	IExecutionTask createExecutionTask() {
		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();

		//TODO MG remove this later
		executionTask.addExecutionTaskListener(new IExecutionTaskListener() {

			@Override
			public void worked(final int totalWorked) {
				//CHECKSTYLE:OFF
				System.out.println("WORKED " + totalWorked);
				//CHECKSTYLE:ON
			}

			@Override
			public void userQuestionAsked(final String question, final IUserAnswerCallback callback) {
				final ValueHolder<QuestionResult> resultHolder = new ValueHolder<QuestionResult>();
				try {
					uiThreadAccess.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							final QuestionResult result = Toolkit.getQuestionPane().askYesNoQuestion(question);
							resultHolder.set(result);
						}
					});
				}
				catch (final InterruptedException e) {
					callback.setQuestionResult(UserQuestionResult.NO);
				}

				if (QuestionResult.YES == resultHolder.get()) {
					callback.setQuestionResult(UserQuestionResult.YES);
				}
				else {
					callback.setQuestionResult(UserQuestionResult.NO);
				}

			}

			@Override
			public void totalStepCountChanged(final int totalStepCount) {
				//CHECKSTYLE:OFF
				System.out.println("TOTAL STEP COUNT " + totalStepCount);
				//CHECKSTYLE:ON
			}

			@Override
			public void subExecutionAdded(final IExecutionTask executionTask) {}

			@Override
			public void finished() {
				//CHECKSTYLE:OFF
				System.out.println("FINISHED " + executionTask.isFinshed());
				//CHECKSTYLE:ON
			}

			@Override
			public void descriptionChanged(final String description) {
				//CHECKSTYLE:OFF
				System.out.println("DESCRIPTION CHANGED " + description);
				//CHECKSTYLE:ON
			}

		});

		return executionTask;
	}

}
