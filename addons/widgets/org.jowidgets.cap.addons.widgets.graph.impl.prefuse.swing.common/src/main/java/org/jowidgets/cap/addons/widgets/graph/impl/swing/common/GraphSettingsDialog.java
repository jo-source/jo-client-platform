/*
 * Copyright (c) 2012, sapalm
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

package org.jowidgets.cap.addons.widgets.graph.impl.swing.common;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.ISlider;
import org.jowidgets.api.widgets.ITextControl;
import org.jowidgets.api.widgets.blueprint.ITextFieldBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.powo.JoFrame;

import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;

public class GraphSettingsDialog extends JoFrame {

	@SuppressWarnings("unused")
	private final ForceSimulator forceSimulator;

	private final IBluePrintFactory bpF;
	private final ITextLabelBluePrint labelBp;
	private final ITextFieldBluePrint textBp;

	public GraphSettingsDialog(final ForceSimulator forceSimulator) {
		super("Settings");
		this.setMinPackSize(new Dimension(300, 325));
		setLayout(new MigLayoutDescriptor("[left, 0::][150::][right, 50!]", "[]5[]"));

		bpF = Toolkit.getBluePrintFactory();
		labelBp = bpF.textLabel().alignLeft();
		textBp = bpF.textField();

		this.forceSimulator = forceSimulator;
		final Force[] forces = forceSimulator.getForces();
		for (int i = 0; i < forces.length; i++) {
			this.add(bpF.textSeparator(forces[i].getClass().getName(), "tooltip").alignCenter().setStrong(), "grow, span, wrap");
			for (int j = 0; j < forces[i].getParameterCount(); j++) {
				createSliderComponent(forces[i], j, "tooltip");
			}
		}
	}

	private void createSliderComponent(final Force force, final int param, final String sliderTooltip) {
		final double value = force.getParameter(param);
		final double min = force.getMinValue(param);
		final double max = force.getMaxValue(param);
		final String name = force.getParameterName(param);
		final double step = (max - min) / 100;

		this.add(labelBp.setText(name).setToolTipText(sliderTooltip), "sg lg");
		final ISlider slider = this.add(bpF.slider(), "growx");
		final ITextControl textControl = this.add(textBp.setText("0"), "growx, wrap");
		textControl.setText(value + "");
		slider.setValue((int) (value / step));
		slider.addInputListener(new SliderListener(slider, textControl, force, param, step, min));
	}

	private class SliderListener implements IInputListener {
		private final ISlider slider;
		private final ITextControl textControl;
		private final double step;
		private final double min;
		private final Force force;
		private final int param;

		public SliderListener(
			final ISlider slider,
			final ITextControl textControl,
			final Force force,
			final int param,
			final double step,
			final double min) {
			this.slider = slider;
			this.textControl = textControl;
			this.step = step;
			this.min = min;
			this.force = force;
			this.param = param;
		}

		@Override
		public void inputChanged() {
			textControl.setText(min + slider.getValue() * step + "");
			force.setParameter(param, (float) (min + slider.getValue() * step));

		}
	}
}
