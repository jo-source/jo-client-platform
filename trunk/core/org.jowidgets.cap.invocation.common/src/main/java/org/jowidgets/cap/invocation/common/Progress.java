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

package org.jowidgets.cap.invocation.common;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.util.Assert;

public final class Progress implements Serializable {

	private static final long serialVersionUID = 8822619729853039106L;

	private final Object taskId;

	private final Integer stepProportion;
	private final Integer totalStepCount;
	private final Integer totalWorked;
	private final String description;
	private final boolean finished;
	private final List<Progress> subProgress;

	public Progress() {
		this(null, null, null, null, null, false, new LinkedList<Progress>());
	}

	public Progress(
		final Object taskId,
		final Integer stepProportion,
		final Integer totalStepCount,
		final Integer totalWorked,
		final String description,
		final boolean finished,
		final List<Progress> subProgress) {
		Assert.paramNotNull(subProgress, "subProgress");
		this.taskId = taskId;
		this.stepProportion = stepProportion;
		this.totalStepCount = totalStepCount;
		this.totalWorked = totalWorked;
		this.description = description;
		this.finished = finished;
		this.subProgress = subProgress;
	}

	public Object getTaskId() {
		return taskId;
	}

	public Integer getStepProportion() {
		return stepProportion;
	}

	public Integer getTotalStepCount() {
		return totalStepCount;
	}

	public Integer getTotalWorked() {
		return totalWorked;
	}

	public String getDescription() {
		return description;
	}

	public boolean isFinished() {
		return finished;
	}

	public List<Progress> getSubProgressList() {
		return subProgress;
	}

	@Override
	public String toString() {
		return "Progress [taskId="
			+ taskId
			+ ", stepProportion="
			+ stepProportion
			+ ", totalStepCount="
			+ totalStepCount
			+ ", totalWorked="
			+ totalWorked
			+ ", description="
			+ description
			+ ", finished="
			+ finished
			+ ", subProgress="
			+ subProgress
			+ "]";
	}

}
