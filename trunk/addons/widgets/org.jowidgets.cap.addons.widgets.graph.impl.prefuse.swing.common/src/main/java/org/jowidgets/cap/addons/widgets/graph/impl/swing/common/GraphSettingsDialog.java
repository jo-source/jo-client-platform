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
import java.util.Set;

import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IInputField;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.ISlider;
import org.jowidgets.api.widgets.blueprint.ISliderBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.cap.addons.widgets.graph.impl.swing.common.BeanRelationGraphImpl.GraphLayout;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.powo.JoFrame;
import org.jowidgets.tools.widgets.blueprint.BPF;

import prefuse.util.force.DragForce;
import prefuse.util.force.Force;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;

final class GraphSettingsDialog extends JoFrame {

	private static final int NODE_LINK_DISTANCE_MAX = 300;
	private static final int NODE_LINK_DISTANCE_MIN = 0;

	private static final int RADIAL_RADIUS_MAX = 500;
	private static final int RADIAL_RADIUS_MIN = 0;

	private static final String[] NODE_LINK_DISTANCES = {"Level distance", "Neighbor distance", "Subtree distance"};
	private static final String[] RADIAL_RADIUS_DISTANCES = {"Radius distance"};

	private static final String[] SPRING_FORCE = {
			Messages.getString("GraphSettingsDialog.spring_force"), Messages.getString("GraphSettingsDialog.spring_coefficient"),
			Messages.getString("GraphSettingsDialog.default_spring_length")};
	private static final String[] DRAG_FORCE = {
			Messages.getString("GraphSettingsDialog.drag_force"), Messages.getString("GraphSettingsDialog.drag_coefficient")};
	private static final String[] NBODY_FORCE = {
			Messages.getString("GraphSettingsDialog.nbody_force"),
			Messages.getString("GraphSettingsDialog.gravitational_constant"), Messages.getString("GraphSettingsDialog.distance"),
			Messages.getString("GraphSettingsDialog.barneshuttheta")};
	private static final String[] DISPLAY_PERCENT_PARAMETER = {"SpringCoefficient", "DragCoefficient", "BarnesHutTheta"};
	private static final Map<String, Float> MODIFIED_VALUES = new HashMap<String, Float>();
	private static final Map<ISlider, Integer> DEFAULT_SLIDER_SETTINGS = new HashMap<ISlider, Integer>();

	private final LayoutManager layoutManager;

	GraphSettingsDialog(final GraphLayout type, final LayoutManager layoutManager, final Position position) {
		super(Messages.getString("GraphSettingsDialog.settings"));

		if (position != null) {
			setPosition(position);
		}

		DEFAULT_SLIDER_SETTINGS.clear();

		this.layoutManager = layoutManager;
		setLayout(MigLayoutFactory.growingInnerCellLayout());
		setMinPackSize(new Dimension(400, 100));

		final IScrollComposite content = add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		content.setLayout(new MigLayoutDescriptor("[0::][grow, 150::][right, 50!]", ""));

		switch (type) {
			case FORCE_DIRECTED_LAYOUT:
				initializeOwnValues();

				final Force[] forces = layoutManager.getForceSimulator().getForces();
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
					initializeForceComponent(content, paramNames, forces[i], GraphLayout.FORCE_DIRECTED_LAYOUT);
				}
				break;
			case RADIAL_TREE_LAYOUT:
				initializeForceComponent(content, "Radius", layoutManager.getRadialRadius(), 0, GraphLayout.RADIAL_TREE_LAYOUT);
				break;
			case NODE_TREE_LINK_LAYOUT:
				for (int i = 0; i < layoutManager.getNodeLinkedForces().length; i++) {
					initializeForceComponent(
							content,
							layoutManager.getNodeLinkedForces()[i],
							layoutManager.getNodeLinkedDistances()[i],
							i,
							GraphLayout.NODE_TREE_LINK_LAYOUT);
				}
				break;
			default:
				break;
		}

		content.add(BPF.separator(), "growx, span3, gapy20,wrap");
		createDefaultSetterButton(content);
	}

	private void createDefaultSetterButton(final IScrollComposite content) {
		final IButton btn = content.add(BPF.button("Reset", "Set settings to default"), "growx, w 0::75, span3, align right");
		btn.addActionListener(new IActionListener() {

			@Override
			public void actionPerformed() {
				final Set<ISlider> sliders = DEFAULT_SLIDER_SETTINGS.keySet();
				for (final ISlider slider : sliders) {
					slider.setValue(DEFAULT_SLIDER_SETTINGS.get(slider));
				}
			}
		});

	}

	private void initializeForceComponent(
		final IScrollComposite content,
		final String parameterName,
		final double defaultValue,
		final int forceNumber,
		final GraphLayout layout) {
		content.add(BPF.textSeparator(parameterName).alignLeft().setStrong(), "growx, w 0::, span, gapy 20, wrap");
		switch (layout) {
			case RADIAL_TREE_LAYOUT:
				createSliderComponent(content, defaultValue, forceNumber, GraphLayout.RADIAL_TREE_LAYOUT);
				break;
			case NODE_TREE_LINK_LAYOUT:
				createSliderComponent(content, defaultValue, forceNumber, GraphLayout.NODE_TREE_LINK_LAYOUT);
				break;
			default:
				break;
		}
	}

	private void initializeForceComponent(
		final IScrollComposite content,
		final String[] parameterNames,
		final Force force,
		final GraphLayout type) {
		content.add(BPF.textSeparator(parameterNames[0]).alignLeft().setStrong(), "growx, w 0::, span, gapy 20, wrap");
		if (force != null) {
			for (int j = 0; j < force.getParameterCount(); j++) {
				createSliderComponent(content, force, j, parameterNames[1 + j]);
			}
		}
	}

	private void createSliderComponent(
		final IScrollComposite content,
		final double defaultValue,
		final int forceNumber,
		final GraphLayout layout) {

		int max = 0;
		int min = 0;
		String description = null;

		switch (layout) {
			case RADIAL_TREE_LAYOUT:
				max = RADIAL_RADIUS_MAX;
				min = RADIAL_RADIUS_MIN;
				description = RADIAL_RADIUS_DISTANCES[forceNumber];
				break;
			case NODE_TREE_LINK_LAYOUT:
				max = NODE_LINK_DISTANCE_MAX;
				min = NODE_LINK_DISTANCE_MIN;
				description = NODE_LINK_DISTANCES[forceNumber];
				break;
			default:
				break;
		}

		final ITextLabelBluePrint labelBp = BPF.textLabel().alignRight();

		content.add(labelBp.setText(description), "sg lg");
		final ISliderBluePrint sliderBp = BPF.slider();
		final ISlider slider;
		final int defaultVal = (int) defaultValue;
		final int step = (max - min) / 100;

		sliderBp.setMaximum(max).setMinimum(min).setTickSpacing((max - min) / 20);
		slider = content.add(sliderBp, "growx, w 0::");
		slider.setValue(defaultVal);

		IInputField<Integer> inputField;
		inputField = content.add(BPF.inputFieldIntegerNumber(), "growx, w 0::, wrap");
		inputField.setValue((int) (slider.getValue()));

		DEFAULT_SLIDER_SETTINGS.put(slider, defaultVal);

		inputField.addInputListener(new ParameterListener(
			slider,
			inputField,
			null,
			forceNumber,
			step,
			min,
			false,
			Listener.INPUTFIELD,
			layout));
		slider.addInputListener(new ParameterListener(
			slider,
			inputField,
			null,
			forceNumber,
			step,
			min,
			false,
			Listener.SLIDER,
			layout));
	}

	private void createSliderComponent(
		final IScrollComposite content,
		final Force force,
		final int param,
		final String description) {

		final String forceParameter = force.getParameterName(param);

		final double value = MODIFIED_VALUES.containsKey(forceParameter + "_DEFAULT") ? MODIFIED_VALUES.get(forceParameter
			+ "_DEFAULT") : force.getParameter(param);
		final double min = MODIFIED_VALUES.containsKey(forceParameter + "_MIN")
				? MODIFIED_VALUES.get(forceParameter + "_MIN") : force.getMinValue(param);
		final double max = MODIFIED_VALUES.containsKey(forceParameter + "_MAX")
				? MODIFIED_VALUES.get(forceParameter + "_MAX") : force.getMaxValue(param);

		final boolean displayPercent;
		final double step = (max - min) / 100;

		final ITextLabelBluePrint labelBp = BPF.textLabel().alignRight();

		content.add(labelBp.setText(description), "sg lg");
		final ISliderBluePrint sliderBp = BPF.slider();
		final ISlider slider;
		final int defaultValue;

		IInputField<Integer> inputField;
		if (Arrays.asList(DISPLAY_PERCENT_PARAMETER).contains(forceParameter)) {
			sliderBp.setMaximum(100).setMinimum(0).setTickSpacing(5);
			slider = content.add(sliderBp, "growx, w 0::");
			defaultValue = (int) (100 / max * value);
			slider.setValue(defaultValue);

			inputField = content.add(BPF.inputFieldIntegerNumber(), "growx, w 0::, split2");
			content.add(BPF.label().setText("%"), "wrap");
			inputField.setValue((int) (slider.getValue()));
			displayPercent = true;
			useForce(displayPercent, param, force, min, slider, step);
		}
		else {
			sliderBp.setMaximum((int) max).setMinimum((int) min).setTickSpacing((int) (max - min) / 20);
			slider = content.add(sliderBp, "growx, w 0::");
			defaultValue = (int) value;
			slider.setValue(defaultValue);

			inputField = content.add(BPF.inputFieldIntegerNumber(), "growx, w 0::, wrap");
			inputField.setValue((int) value);
			displayPercent = false;
			useForce(displayPercent, param, force, min, slider, step);
		}
		DEFAULT_SLIDER_SETTINGS.put(slider, defaultValue);

		inputField.addInputListener(new ParameterListener(
			slider,
			inputField,
			force,
			param,
			step,
			min,
			displayPercent,
			Listener.INPUTFIELD,
			GraphLayout.FORCE_DIRECTED_LAYOUT));
		slider.addInputListener(new ParameterListener(
			slider,
			inputField,
			force,
			param,
			step,
			min,
			displayPercent,
			Listener.SLIDER,
			GraphLayout.FORCE_DIRECTED_LAYOUT));
	}

	//Define own values with syntax : ForceParameterName_(MIN | MAX | DEFAULT)
	private void initializeOwnValues() {
		MODIFIED_VALUES.put("DefaultSpringLength_MAX", 500.0f);
		MODIFIED_VALUES.put("DragCoefficient_DEFAULT", 0.03f);
	}

	private class ParameterListener implements IInputListener {
		private final ISlider slider;
		private final IInputField<Integer> inputField;
		private final double step;
		private final double min;
		private final Force force;
		private final int param;
		private final boolean percent;
		private final Listener type;
		private final GraphLayout layout;

		public ParameterListener(
			final ISlider slider,
			final IInputField<Integer> inputField,
			final Force force,
			final int param,
			final double step,
			final double min,
			final boolean percent,
			final Listener type,
			final GraphLayout layout) {
			this.slider = slider;
			this.inputField = inputField;
			this.step = step;
			this.min = min;
			this.force = force;
			this.param = param;
			this.percent = percent;
			this.type = type;
			this.layout = layout;
		}

		@Override
		public void inputChanged() {
			if (type == Listener.SLIDER) {

				inputField.setValue((int) slider.getValue());
				if (force != null) {
					useForce(percent, param, force, min, slider, step);
				}
				switch (layout) {
					case RADIAL_TREE_LAYOUT:
						layoutManager.setRadialRadius(slider.getValue());
						layoutManager.runActiveLayout();

						break;
					case NODE_TREE_LINK_LAYOUT:
						if (param == 0) {
							layoutManager.setNodeLinkedDistance(slider.getValue());
						}
						else if (param == 1) {
							layoutManager.setNodeLinkedNeighborDistance(slider.getValue());
						}
						else if (param == 2) {
							layoutManager.setNodeLinkedSubtreeDistance(slider.getValue());
						}
						layoutManager.runActiveLayout();
						break;
					default:
						break;
				}

			}

			if (type == Listener.INPUTFIELD) {
				if (inputField.getValue() != null) {
					if (percent) {
						slider.setValue((int) (inputField.getValue()));
					}
					else {
						slider.setValue(inputField.getValue());
					}
				}
			}
		}
	}

	private void useForce(
		final boolean percent,
		final int param,
		final Force force,
		final double min,
		final ISlider slider,
		final double step) {
		if (percent) {
			force.setParameter(param, (float) (min + slider.getValue() * step));
		}
		else {
			force.setParameter(param, slider.getValue());
		}
	}

	private enum Listener {
		INPUTFIELD,
		SLIDER;
	}
}
