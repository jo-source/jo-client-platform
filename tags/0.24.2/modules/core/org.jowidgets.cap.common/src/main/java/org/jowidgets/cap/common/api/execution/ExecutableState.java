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

package org.jowidgets.cap.common.api.execution;

public final class ExecutableState implements IExecutableState {

	public static final ExecutableState EXECUTABLE = new ExecutableState();
	public static final ExecutableState NOT_EXECUTABLE = new ExecutableState(false, null);

	private final boolean executable;
	private final String reason;

	private ExecutableState() {
		this(true, null);
	}

	private ExecutableState(final boolean executable, final String reason) {
		this.executable = executable;
		this.reason = reason;
	}

	@Override
	public boolean isExecutable() {
		return executable;
	}

	@Override
	public String getReason() {
		return reason;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (executable ? 1231 : 1237);
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ExecutableState other = (ExecutableState) obj;
		if (executable != other.executable) {
			return false;
		}
		if (reason == null) {
			if (other.reason != null) {
				return false;
			}
		}
		else if (!reason.equals(other.reason)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExecutableState [executable=" + executable + ", reason=" + reason + "]";
	}

	public static ExecutableState notExecutable(final String reason) {
		return new ExecutableState(false, reason);
	}

}
