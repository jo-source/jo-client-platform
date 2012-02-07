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

package org.jowidgets.cap.sample1.ui;

import org.jowidgets.addons.icons.silkicons.SilkIconsInitializer;
import org.jowidgets.api.command.IAction;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.defaults.IDefaultInitializer;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.sample1.ui.workbench.command.WorkbenchActions;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.util.IProvider;

public final class SampleDefaultsInitializer {

	private SampleDefaultsInitializer() {}

	public static void initialize() {
		SilkIconsInitializer.initializeFull();

		final IBluePrintFactory bpf = Toolkit.getBluePrintFactory();

		bpf.addDefaultsInitializer(IBeanFormBluePrint.class, new IDefaultInitializer<IBeanFormBluePrint<?>>() {
			@Override
			public void initialize(final IBeanFormBluePrint<?> setupBuilder) {
				setupBuilder.setUndoAction(createUndoActionProvider());
				setupBuilder.setSaveAction(createSaveActionProvider());
			}
		});
	}

	private static IProvider<IAction> createUndoActionProvider() {
		return new IProvider<IAction>() {
			@Override
			public IAction get() {
				return new ActionWrapper(WorkbenchActions.undoAction()) {
					@Override
					public IImageConstant getIcon() {
						return null;
					}
				};
			}
		};
	}

	private static IProvider<IAction> createSaveActionProvider() {
		return new IProvider<IAction>() {
			@Override
			public IAction get() {
				return new ActionWrapper(WorkbenchActions.saveAction()) {
					@Override
					public IImageConstant getIcon() {
						return null;
					}
				};
			}
		};
	}
}
