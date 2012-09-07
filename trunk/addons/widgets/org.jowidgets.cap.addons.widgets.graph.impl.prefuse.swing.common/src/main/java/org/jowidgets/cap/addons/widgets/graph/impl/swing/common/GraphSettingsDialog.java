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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

import prefuse.util.force.DragForce;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;

public class GraphSettingsDialog extends JoFrame {

	private static final String[] SPRING_FORCE = {
			Messages.getString("GraphSettingsDialog.spring_force"), Messages.getString("GraphSettingsDialog.spring_coefficient"), Messages.getString("GraphSettingsDialog.default_spring_length")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] DRAG_FORCE = {
			Messages.getString("GraphSettingsDialog.drag_force"), Messages.getString("GraphSettingsDialog.drag_coefficient")}; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] NBODY_FORCE = {
			Messages.getString("GraphSettingsDialog.nbody_force"), Messages.getString("GraphSettingsDialog.gravitational_constant"), Messages.getString("GraphSettingsDialog.distance"), Messages.getString("GraphSettingsDialog.barneshuttheta")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] DISPLAY_PERCENT_PARAMETER = {"SpringCoefficient"};
	private static final Map<String, Float> MODIFIED_VALUES = new HashMap<String, Float>();

	private final ForceSimulator forceSimulator;

	private final IBluePrintFactory bpF;
	private final ITextLabelBluePrint labelBp;
	private final ITextFieldBluePrint textBp;

	public GraphSettingsDialog(final ForceSimulator forceSimulator) {
		super(Messages.getString("GraphSettingsDialog.settings")); //$NON-NLS-1$
		this.setMinPackSize(new Dimension(300, 325));
		setLayout(new MigLayoutDescriptor("[0::][150::][right, 50!]", "[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$
		initializeOwnValues();

		bpF = Toolkit.getBluePrintFactory();
		labelBp = bpF.textLabel().alignLeft();
		textBp = bpF.textField();

		this.forceSimulator = forceSimulator;
		final Force[] forces = this.forceSimulator.getForces();
		for (int i = 0; i < forces.length; i++) {
			String[] paramNames = null;
			final String forceName = forces[i].getClass().getSimpleName();
			if (forceName.equals(SpringForce.class.getSimpleName())) {
				paramNames = SPRING_FORCE;
			}
			else if (forceName.equals(DragForce.class.getSimpleName())) {
				paramNames = DRAG_FORCE;
			}
			else if (forceName.equals(NBodyForce.class.getSimpleName())) {
				paramNames = NBODY_FORCE;
			}
			else {
				final String[] parameterNames = new String[forces[i].getParameterCount() + 1];
				parameterNames[0] = forces[i].getClass().getSimpleName();
				for (int j = 0; j < forces[i].getParameterCount(); j++) {
					parameterNames[j + 1] = forces[i].getParameterName(j);
				}
				paramNames = parameterNames;
			}
			initializeForceComponent(paramNames, forces[i]);
		}
	}

	private void initializeForceComponent(final String[] parameterNames, final Force force) {
		this.add(bpF.textSeparator(parameterNames[0]).alignCenter().setStrong(), "grow, span, wrap"); //$NON-NLS-1$
		for (int j = 0; j < force.getParameterCount(); j++) {
			createSliderComponent(force, j, parameterNames[1 + j]);
		}
	}

	private void createSliderComponent(final Force force, final int param, final String description) {

		final String forceParameter = force.getParameterName(param);

		final double value = MODIFIED_VALUES.containsKey(forceParameter + "_DEFAULT") ? MODIFIED_VALUES.get(forceParameter
			+ "_DEFAULT") : force.getParameter(param);
		final double min = MODIFIED_VALUES.containsKey(forceParameter + "_MIN")
				? MODIFIED_VALUES.get(forceParameter + "_MIN") : force.getMinValue(param);
		final double max = MODIFIED_VALUES.containsKey(forceParameter + "_MAX")
				? MODIFIED_VALUES.get(forceParameter + "_MAX") : force.getMaxValue(param);

		final double step = (max - min) / 100;
		final boolean displayPercent;

		this.add(labelBp.setText(description), "sg lg"); //$NON-NLS-1$
		final ISlider slider = this.add(bpF.slider(), "growx"); //$NON-NLS-1$
		slider.setValue((int) ((value - min) / step));

		final ITextControl textControl = this.add(textBp.setText("0"), "growx, wrap"); //$NON-NLS-1$ //$NON-NLS-2$
		if (Arrays.asList(DISPLAY_PERCENT_PARAMETER).contains(forceParameter)) {
			textControl.setText(slider.getValue() + " %"); //$NON-NLS-1$
			displayPercent = true;
		}
		else {
			textControl.setText(String.format("%.3f", (float) (min + slider.getValue() * step))); //$NON-NLS-1$
			displayPercent = false;
		}

		slider.addInputListener(new SliderListener(slider, textControl, force, param, step, min, displayPercent));
	}

	//Define own Values with Syntax : ForceParameterName_(MIN | MAX | DEFAULT)
	private void initializeOwnValues() {
		MODIFIED_VALUES.put("DefaultSpringLength_MAX", 500.0f);
	}

	private class SliderListener implements IInputListener {
		private final ISlider slider;
		private final ITextControl textControl;
		private final double step;
		private final double min;
		private final Force force;
		private final int param;
		private final boolean percent;

		public SliderListener(
			final ISlider slider,
			final ITextControl textControl,
			final Force force,
			final int param,
			final double step,
			final double min,
			final boolean percent) {
			this.slider = slider;
			this.textControl = textControl;
			this.step = step;
			this.min = min;
			this.force = force;
			this.param = param;
			this.percent = percent;
		}

		@Override
		public void inputChanged() {
			if (percent) {
				textControl.setText(slider.getValue() + " %"); //$NON-NLS-1$
			}
			else {
				textControl.setText(String.format("%.3f", (float) (min + slider.getValue() * step))); //$NON-NLS-1$
			}
			force.setParameter(param, (float) (min + slider.getValue() * step));
		}
	}
}
