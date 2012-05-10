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

package org.jowidgets.cap.sample1.ui.workbench.component.config;

import org.jowidgets.cap.sample1.common.entity.ISampleConfig;
import org.jowidgets.cap.sample1.ui.workbench.command.WorkbenchActions;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.model.ISingleBeanModel;
import org.jowidgets.cap.ui.api.model.ISingleBeanModelBuilder;
import org.jowidgets.common.types.IVetoable;
import org.jowidgets.workbench.api.IComponent;
import org.jowidgets.workbench.api.IComponentContext;
import org.jowidgets.workbench.api.IView;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.toolkit.api.IComponentNodeModel;
import org.jowidgets.workbench.tools.AbstractComponent;

public class SampleConfigComponent extends AbstractComponent implements IComponent {

	private final ISingleBeanModel<ISampleConfig> model;

	public SampleConfigComponent(final IComponentNodeModel componentNodeModel, final IComponentContext componentContext) {
		componentContext.setLayout(new SampleConfigComponentDefaultLayout().getLayout());
		this.model = createModel();
	}

	@Override
	public IView createView(final String viewId, final IViewContext context) {
		if (SampleConfigView.ID.equals(viewId)) {
			return new SampleConfigView(context, model);
		}
		else {
			throw new IllegalArgumentException("View id '" + viewId + "' is not known.");
		}
	}

	@Override
	public void onDispose() {}

	@Override
	public void onActivation() {
		WorkbenchActions.loadAction().addDataModel(model);
		WorkbenchActions.saveAction().addDataModel(model);
		WorkbenchActions.undoAction().addDataModel(model);
		WorkbenchActions.cancelAction().addDataModel(model);
	}

	@Override
	public void onDeactivation(final IVetoable vetoable) {
		WorkbenchActions.loadAction().removeDataModel(model);
		WorkbenchActions.saveAction().removeDataModel(model);
		WorkbenchActions.undoAction().removeDataModel(model);
		WorkbenchActions.cancelAction().removeDataModel(model);
	}

	private ISingleBeanModel<ISampleConfig> createModel() {
		final ISingleBeanModelBuilder<ISampleConfig> builder = CapUiToolkit.singleBeanModelBuilder(ISampleConfig.class);
		return builder.build();
	}

}
