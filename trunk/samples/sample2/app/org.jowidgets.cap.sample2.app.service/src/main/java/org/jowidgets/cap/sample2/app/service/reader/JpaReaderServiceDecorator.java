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

package org.jowidgets.cap.sample2.app.service.reader;

import java.util.List;

import javax.persistence.EntityManager;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;
import org.jowidgets.cap.service.impl.jpa.JpaReaderService;

//This is just for test purpose, generic decoration will be done later
public final class JpaReaderServiceDecorator<PARAM_TYPE> implements IReaderService<PARAM_TYPE> {

	private final JpaReaderService<PARAM_TYPE> originalReader;
	private final JpaReaderService<PARAM_TYPE> originalCounter;

	public JpaReaderServiceDecorator(
		final JpaReaderService<PARAM_TYPE> originalReader,
		final JpaReaderService<PARAM_TYPE> originalCounter) {
		super();
		this.originalReader = originalReader;
		this.originalCounter = originalCounter;
	}

	@Override
	public void read(
		final IResultCallback<List<IBeanDto>> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		final EntityManager entityManager = EntityManagerProvider.entityManager();
		originalReader.setEntityManager(entityManager);

		try {
			originalReader.read(result, parentBeanKeys, filter, sorting, firstRow, maxRows, parameter, executionCallback);
		}
		finally {
			entityManager.close();
			originalReader.setEntityManager(null);
		}

	}

	@Override
	public void count(
		final IResultCallback<Integer> result,
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final PARAM_TYPE parameter,
		final IExecutionCallback executionCallback) {

		final EntityManager entityManager = EntityManagerProvider.entityManager();
		originalCounter.setEntityManager(entityManager);

		try {
			originalCounter.count(result, parentBeanKeys, filter, parameter, executionCallback);
		}
		finally {
			entityManager.close();
			originalCounter.setEntityManager(null);
		}

	}

}
