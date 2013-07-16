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

package org.jowidgets.cap.common.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.cap.common.api.execution.ExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;

public class ExecutableCheckerCompositeImpl<BEAN_TYPE> implements IExecutableChecker<BEAN_TYPE> {

	private final List<IExecutableChecker<? extends BEAN_TYPE>> executableCheckers;
	private final Set<String> propertyDependencies;

	public ExecutableCheckerCompositeImpl(final List<IExecutableChecker<? extends BEAN_TYPE>> checkers) {
		super();
		this.executableCheckers = new LinkedList<IExecutableChecker<? extends BEAN_TYPE>>(checkers);
		this.propertyDependencies = createPropertyDependencies(checkers);
	}

	private static Set<String> createPropertyDependencies(final List<? extends IExecutableChecker<?>> checkers) {
		final Set<String> result = new HashSet<String>();
		for (final IExecutableChecker<?> checker : checkers) {
			result.addAll(checker.getPropertyDependencies());
		}
		return Collections.unmodifiableSet(result);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public IExecutableState getExecutableState(final BEAN_TYPE bean) {
		for (final IExecutableChecker checker : executableCheckers) {
			final IExecutableState result = checker.getExecutableState(bean);
			if (!result.isExecutable()) {
				return result;
			}
		}
		return ExecutableState.EXECUTABLE;
	}

	@Override
	public Set<String> getPropertyDependencies() {
		return propertyDependencies;
	}

}
