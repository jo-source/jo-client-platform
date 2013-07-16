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

import org.jowidgets.cap.common.api.bean.IBeanFormInfoDescriptor;
import org.jowidgets.cap.ui.api.form.IBeanFormGroupBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormInfo;
import org.jowidgets.cap.ui.api.form.IBeanFormInfoBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayoutBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouterBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormPropertyBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;

final class BeanFormToolkitImpl implements IBeanFormToolkit {

	@Override
	public IBeanFormLayoutBuilder layoutBuilder() {
		return new BeanFormLayoutBuilderImpl();
	}

	@Override
	public IBeanFormGroupBuilder groupBuilder() {
		return new BeanFormGroupBuilderImpl();
	}

	@Override
	public IBeanFormPropertyBuilder propertyBuilder() {
		return new BeanFormPropertyBuilderImpl();
	}

	@Override
	public IBeanFormLayouter layouter(final IBeanFormLayout layout) {
		return layouterBuilder(layout).build();
	}

	@Override
	public IBeanFormLayouterBuilder layouterBuilder(final IBeanFormLayout layout) {
		return new BeanFormLayouterBuilderImpl(layout);
	}

	@Override
	public IBeanFormLayouter contentLayouter(final IBeanFormLayout layout) {
		return new BeanFormContentLayouter(layout);
	}

	@Override
	public IBeanFormInfoBuilder infoBuilder() {
		return new BeanFormInfoBuilderImpl();
	}

	@Override
	public IBeanFormInfo info(final IBeanFormInfoDescriptor descriptor) {
		if (descriptor != null) {
			return infoBuilder().setDescriptor(descriptor).build();
		}
		else {
			return null;
		}
	}

}
