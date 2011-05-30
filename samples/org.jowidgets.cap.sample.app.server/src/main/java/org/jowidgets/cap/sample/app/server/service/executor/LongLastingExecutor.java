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

package org.jowidgets.cap.sample.app.server.service.executor;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.sample.app.server.entity.User;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.util.types.Null;

public class LongLastingExecutor implements IBeanExecutor<User, Null> {

	private static final int OUTER_LOOP_COUNT = 10;
	private static final int INNER_LOOP_COUNT = 20;
	private static final long DELAY = 20;

	@Override
	public User execute(final User user, final Null parameter, final IExecutionCallback executionCallback) {
		executionCallback.setTotalStepCount(OUTER_LOOP_COUNT * 3);
		executionCallback.setDescription("Does some execution with '" + user.getName() + " " + user.getLastName() + "' ");
		for (int i = 0; i < OUTER_LOOP_COUNT && !executionCallback.isCanceled(); i++) {
			final IExecutionCallback subExecution1 = executionCallback.createSubExecution(1, false);
			subExecution1.setTotalStepCount(INNER_LOOP_COUNT);
			subExecution1.setDescription("Some parallel sub execution");

			final IExecutionCallback subExecution2 = executionCallback.createSubExecution(2, false);
			subExecution2.setDescription("Some other parallel sub execution");
			subExecution2.setTotalStepCount(INNER_LOOP_COUNT * 2);

			for (int j = 0; j < INNER_LOOP_COUNT * 2 && !executionCallback.isCanceled(); j++) {
				try {
					Thread.sleep(DELAY);
				}
				catch (final InterruptedException e) {
					throw new RuntimeException(e);
				}
				if (j < INNER_LOOP_COUNT) {
					subExecution1.workedOne();
				}
				subExecution2.workedOne();
			}
			executionCallback.worked(3);
		}

		user.setName(user.getName());
		return user;
	}
}
