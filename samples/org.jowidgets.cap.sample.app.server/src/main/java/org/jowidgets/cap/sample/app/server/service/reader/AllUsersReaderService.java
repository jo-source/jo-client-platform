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

package org.jowidgets.cap.sample.app.server.service.reader;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.sample.app.common.entity.IUser;
import org.jowidgets.cap.sample.app.server.datastore.DataStore;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;

public class AllUsersReaderService implements IReaderService<Integer> {

	private static final IBeanDtoFactory<IUser> BEAN_FACTORY = CapServiceToolkit.dtoFactory(
			IUser.class,
			IUser.ALL_PROPERTIES);

	@Override
	public List<IBeanDto> read(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final List<? extends ISort> sortedProperties,
		final int firstRow,
		final int maxRows,
		final Integer delay,
		final IExecutionCallback executionCallback) {

		//TODO MG this will currently not work
		//executionCallback = CapServiceToolkit.createDelayedExecutionCallback(executionCallback);

		final List<IBeanDto> result = BEAN_FACTORY.createDtos(DataStore.getPersons().getAllData(firstRow, maxRows));

		//TODO apply filter and sort

		if (delay != null) {
			try {
				if (delay.intValue() > 100) {
					final int sleepTime = delay.intValue() / 100;
					for (int i = 0; i < 100 && !executionCallback.isCanceled(); i++) {
						Thread.sleep(sleepTime);
					}
				}
				else {
					Thread.sleep(delay.intValue());
				}
			}

			catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	@Override
	public int count(
		final List<? extends IBeanKey> parentBeans,
		final IFilter filter,
		final Integer delay,
		final IExecutionCallback executionCallback) {

		//TODO apply filter

		return DataStore.getPersons().getAllData().size();
	}

}
