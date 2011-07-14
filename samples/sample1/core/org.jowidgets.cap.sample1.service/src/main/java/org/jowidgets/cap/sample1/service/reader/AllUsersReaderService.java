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

package org.jowidgets.cap.sample1.service.reader;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.service.datastore.DataStore;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.security.api.SecurityContextHolder;

public class AllUsersReaderService implements ISyncReaderService<Integer> {

	private final ISyncReaderService<Void> readerService;

	public AllUsersReaderService() {
		this.readerService = new SyncReaderService<IUser>(DataStore.getPersons(), IUser.ALL_PROPERTIES);
	}

	@Override
	public List<IBeanDto> read(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final List<? extends ISort> sorting,
		final int firstRow,
		final int maxRows,
		final Integer delay,
		final IExecutionCallback executionCallback) {

		final List<IBeanDto> result = readerService.read(
				parentBeanKeys,
				filter,
				sorting,
				firstRow,
				maxRows,
				null,
				executionCallback);

		if (delay != null) {
			try {
				if (delay.intValue() > 100) {
					final int sleepTime = delay.intValue() / 100;
					for (int i = 0; i < 100 && !executionCallback.isCanceled(); i++) {
						Thread.sleep(sleepTime);
					}
				}
				else {
					Thread.sleep(Math.max(delay.intValue(), 0));
				}
			}

			catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		//CHECKSTYLE:OFF
		System.out.println(SecurityContextHolder.getSecurityContext());
		//CHECKSTYLE:ON

		return result;
	}

	@Override
	public Integer count(
		final List<? extends IBeanKey> parentBeanKeys,
		final IFilter filter,
		final Integer delay,
		final IExecutionCallback executionCallback) {

		return Integer.valueOf(readerService.count(parentBeanKeys, filter, null, executionCallback));
	}

}
