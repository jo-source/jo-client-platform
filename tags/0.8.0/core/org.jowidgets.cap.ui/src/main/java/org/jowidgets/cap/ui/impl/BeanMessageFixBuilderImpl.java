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

import org.jowidgets.cap.ui.api.bean.IBeanMessageFix;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.util.Assert;

final class BeanMessageFixBuilderImpl implements IBeanMessageFixBuilder {

	private static final Runnable DEFAULT_RUNNABLE = createDefaultRunnable();

	private Object type;
	private String label;
	private String description;
	private IImageConstant icon;
	private Runnable execution;

	BeanMessageFixBuilderImpl() {
		this.execution = DEFAULT_RUNNABLE;
	}

	@Override
	public IBeanMessageFixBuilder setType(final Object type) {
		this.type = type;
		return this;
	}

	@Override
	public IBeanMessageFixBuilder setLabel(final String label) {
		Assert.paramNotNull(label, "label");
		this.label = label;
		return this;
	}

	@Override
	public IBeanMessageFixBuilder setDescription(final String decription) {
		this.description = decription;
		return this;
	}

	@Override
	public IBeanMessageFixBuilder setIcon(final IImageConstant icon) {
		this.icon = icon;
		return this;
	}

	@Override
	public IBeanMessageFixBuilder setExecution(final Runnable execution) {
		Assert.paramNotNull(execution, "execution");
		this.execution = execution;
		return this;
	}

	@Override
	public IBeanMessageFix build() {
		return new BeanMessageFixImpl(type, label, description, icon, execution);
	}

	private static Runnable createDefaultRunnable() {
		return new Runnable() {
			@Override
			public void run() {}
		};
	}

}
