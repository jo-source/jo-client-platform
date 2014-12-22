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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.Collection;
import java.util.LinkedList;

import org.jowidgets.api.command.IAction;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormSetupConvenience;
import org.jowidgets.tools.widgets.blueprint.convenience.AbstractSetupBuilderConvenience;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IProvider;

final class BeanFormSetupConvenience extends AbstractSetupBuilderConvenience<IBeanFormBluePrint<Object>> implements
		IBeanFormSetupConvenience<Object, IBeanFormBluePrint<Object>> {

	@Override
	public IBeanFormBluePrint<Object> setLayouter(final IBeanFormLayouter layouter) {
		final IBeanFormBluePrint<Object> builder = getBuilder();
		builder.setEditModeLayouter(layouter);
		builder.setCreateModeLayouter(layouter);
		return builder;
	}

	@Override
	public IBeanFormBluePrint<Object> setAttributes(final Collection<? extends IAttribute<?>> attributes) {
		final IBeanFormBluePrint<Object> builder = getBuilder();
		builder.setEditModeAttributes(attributes);
		builder.setCreateModeAttributes(attributes);
		return builder;
	}

	@Override
	public IBeanFormBluePrint<Object> addCustomAction(final IProvider<IAction> action) {
		Assert.paramNotNull(action, "action");
		final IBeanFormBluePrint<Object> builder = getBuilder();
		Collection<IProvider<IAction>> actions = builder.getCustomActions();
		if (actions == null) {
			actions = new LinkedList<IProvider<IAction>>();
		}
		actions.add(action);
		builder.setCustomActions(actions);
		return builder;
	}

	@Override
	public IBeanFormBluePrint<Object> addCustomAction(final IAction action) {
		Assert.paramNotNull(action, "action");
		return addCustomAction(new IProvider<IAction>() {
			@Override
			public IAction get() {
				return action;
			}
		});
	}

}
