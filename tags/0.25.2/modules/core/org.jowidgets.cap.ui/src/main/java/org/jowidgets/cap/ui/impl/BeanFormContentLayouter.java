/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.cap.ui.api.form.BeanFormGroupRendering;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.common.types.AlignmentVertical;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.ILayoutDescriptor;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

final class BeanFormContentLayouter implements IBeanFormLayouter {

	private final IBeanFormLayout layout;

	private int validationLabelGap;

	BeanFormContentLayouter(final IBeanFormLayout layout) {
		Assert.paramNotNull(layout, "layout");
		this.layout = layout;
		this.validationLabelGap = 0;
	}

	@Override
	public void layout(final IContainer container, final IBeanFormControlFactory controlFactory) {
		final Collection<ICustomWidgetCreator<? extends IControl>> buttons = controlFactory.createButtons();
		if (!buttons.isEmpty()) {
			container.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[0::]15[]0"));

			//add the form
			final IContainer formContainer = container.add(BPF.composite(), "growx, w 0::, wrap");
			layoutForm(formContainer, controlFactory);

			//add the button bar
			final IComposite buttonBar = container.add(BPF.composite(), "alignx r");
			buttonBar.setLayout(getButtonBarLayout(buttons));
			for (final ICustomWidgetCreator<? extends IControl> button : buttons) {
				buttonBar.add(button, "sg bg");
			}
		}
		else {
			layoutForm(container, controlFactory);
		}

	}

	private ILayoutDescriptor getButtonBarLayout(final Collection<ICustomWidgetCreator<? extends IControl>> buttons) {
		final int gap;
		if (validationLabelGap > 0) {
			gap = validationLabelGap + 3;
		}
		else {
			gap = 0;
		}
		final StringBuilder cc = new StringBuilder("0");
		for (int i = 0; i < buttons.size(); i++) {
			cc.append("[]");
		}
		cc.append("" + gap);
		return new MigLayoutDescriptor(cc.toString(), "0[]0");
	}

	private void layoutForm(final IContainer formContainer, final IBeanFormControlFactory controlFactory) {
		formContainer.setLayout(new MigLayoutDescriptor(getColumnsConstraints(layout), ""));

		final List<boolean[]> globalGrid = new ArrayList<boolean[]>();

		int row = 0;

		final boolean showSeparators = layout.getGroups().size() > 1;
		for (final IBeanFormGroup group : layout.getGroups()) {

			final String label = group.getLabel();
			final BeanFormGroupRendering rendering = group.getRendering();

			row = getNextFreeRow(globalGrid);
			final List<boolean[]> grid;
			final IContainer container;
			if (BeanFormGroupRendering.NONE.equals(rendering)) {
				grid = globalGrid;
				container = formContainer;
			}
			else if (BeanFormGroupRendering.SEPARATOR.equals(rendering)) {
				final String baseConstraints = "growx, cell 0 " + row + " " + (3 * layout.getColumnCount()) + " 1";
				setUsed(globalGrid, row, 0, 1, layout.getColumnCount());

				if (label != null && !"".equals(label)) {
					final String gapTop = (row > 0) ? "gaptop 27" : "";
					final String gapBottom = "gapbottom 7";
					final String cell = constraints(baseConstraints, gapTop, gapBottom);
					formContainer.add(Toolkit.getBluePrintFactory().textSeparator(label), cell);
				}
				else if (row > 0 && showSeparators) {
					final String cell = constraints(baseConstraints, "gaptop 17, gapbottom 17");
					formContainer.add(Toolkit.getBluePrintFactory().separator(), cell);
				}
				grid = globalGrid;
				container = formContainer;
			}
			else if (BeanFormGroupRendering.BORDER.equals(rendering)) {
				final String cell = "growx, cell 0 " + row + " " + (3 * layout.getColumnCount()) + " 1";

				setUsed(globalGrid, row, 0, 1, layout.getColumnCount());
				if (label != null && !"".equals(label)) {
					container = formContainer.add(Toolkit.getBluePrintFactory().composite(label), cell);
				}
				else {
					container = formContainer.add(Toolkit.getBluePrintFactory().compositeWithBorder(), cell);
				}
				container.setLayout(new MigLayoutDescriptor(getColumnsConstraints(layout), ""));
				grid = new ArrayList<boolean[]>();
			}
			else {
				throw new IllegalStateException("Unkown BeanFormGroupRendering '" + rendering + "'.");
			}

			// reset column index
			int logicalColumn = 0;
			row = getNextFreeRow(grid);
			int currentRowHeight = 1;
			for (final IBeanFormProperty property : group.getProperties()) {
				final String propertyName = property.getPropertyName();

				final ICustomWidgetCreator<? extends IControl> controlCreator = controlFactory.createControl(propertyName);

				//only add to the layout if there is a control for this property
				if (controlCreator == null) {
					continue;
				}

				final int propertyColumnCount = property.getColumnCount();
				final int propertyColumnSpan = property.getColumnSpan();
				final int propertyRowCount = property.getRowCount();
				final int propertyRowSpan = property.getRowSpan();

				final Position nextGridPosition = getNextGridPosition(grid, row, logicalColumn, propertyColumnCount);
				logicalColumn = nextGridPosition.getX();
				row = nextGridPosition.getY();
				setUsed(grid, row, logicalColumn, propertyRowCount, propertyColumnCount);

				currentRowHeight = Math.max(currentRowHeight, propertyRowCount);

				final String sizeGroupLabel = "sg lbl" + logicalColumn;
				final String sizeGroupControl = "sgy r" + row + "ctrlspn" + propertyRowSpan;
				final ICustomWidgetCreator<? extends IControl> validationLabelCreator = controlFactory.createPropertyValidationLabel(propertyName);

				final int firstPropertyColumn = (3 * logicalColumn);

				//add label
				String cell;
				if (property.showLabel()) {
					final AlignmentVertical labelAlignmentVertical = property.getLabelAlignmentVertical();
					if (AlignmentVertical.TOP.equals(labelAlignmentVertical)) {
						cell = "aligny top, cell " + firstPropertyColumn + " " + row;
					}
					else if (AlignmentVertical.CENTER.equals(labelAlignmentVertical)) {
						cell = "aligny center, cell " + firstPropertyColumn + " " + row + " 1 " + propertyRowSpan;
					}
					else if (AlignmentVertical.BOTTOM.equals(labelAlignmentVertical)) {
						cell = "aligny bottom, cell " + firstPropertyColumn + " " + (row + propertyRowSpan - 1);
					}
					else {
						throw new IllegalStateException("Unknown vertical alignment '" + labelAlignmentVertical + "'.");
					}

					final ICustomWidgetCreator<? extends IControl> labelCreator;
					labelCreator = controlFactory.createLabel(propertyName, property.getLabelAlignmentHorizontal());
					container.add(labelCreator, constraints(cell, sizeGroupLabel));

					cell = "cell "
						+ (firstPropertyColumn + 1)
						+ " "
						+ row
						+ " "
						+ (3 * propertyColumnSpan - 2)
						+ " "
						+ propertyRowSpan;
				}
				else {
					cell = "cell " + firstPropertyColumn + " " + row + " " + (3 * propertyColumnSpan - 1) + " " + propertyRowSpan;
				}

				container.add(
						controlCreator,
						constraints(
								cell,
								sizeGroupControl,
								getControlWidthConstraints(layout, logicalColumn),
								getHeightConstraints(property.getHeight()),
								"growx",
								"growy"));

				//add validation label
				if (validationLabelCreator != null) {
					final int validationLabelMinWidth = property.getValidationLabelMinWidth();
					container.add(validationLabelCreator, "w " + validationLabelMinWidth);
					validationLabelGap = Math.max(validationLabelGap, validationLabelMinWidth);
				}

				logicalColumn = logicalColumn + propertyColumnCount;
			}
		}
	}

	private String getHeightConstraints(final Integer height) {
		if (height != null) {
			return "height " + height + "!";
		}
		else {
			return "";
		}
	}

	private int getNextFreeRow(final List<boolean[]> grid) {
		return grid.size();
	}

	private void setUsed(final List<boolean[]> grid, final int row, final int column, final int rowCount, final int columnCount) {
		for (int usedRow = row; usedRow < row + rowCount; usedRow++) {
			if (usedRow >= grid.size()) {
				grid.add(usedRow, new boolean[layout.getColumnCount()]);
			}

			final boolean[] columns = grid.get(usedRow);
			for (int usedColumn = column; usedColumn < column + columnCount; usedColumn++) {
				columns[usedColumn] = true;
			}
			grid.set(usedRow, columns);
		}
	}

	private int getAvailableColumns(final List<boolean[]> grid, final int row, final int column) {
		if (row >= grid.size()) {
			return layout.getColumnCount() - column;
		}
		else {
			final boolean[] columns = grid.get(row);
			int result = 0;
			while (column + result < layout.getColumnCount() && !columns[column + result]) {
				result++;
			}

			return result;
		}
	}

	private Position getNextGridPosition(final List<boolean[]> grid, int row, int column, int columnCount) {
		if (column >= layout.getColumnCount()) {
			column = 0;
			row++;
		}
		if (columnCount > layout.getColumnCount()) {
			// throw exception?
			columnCount = layout.getColumnCount();
		}

		while (getAvailableColumns(grid, row, column) < columnCount) {
			column++;
			if (column >= layout.getColumnCount()) {
				column = 0;
				row++;
			}
		}

		return new Position(column, row);
	}

	private static String constraints(final String... constraints) {
		final StringBuilder result = new StringBuilder();
		for (final String constraint : constraints) {
			if (result.length() > 0 && !"".equals(constraint)) {
				result.append(", ");
			}
			result.append(constraint);
		}
		return result.toString();
	}

	private static String getColumnWidthConstraints(final IBeanFormLayout layout, final int column) {
		final String controlMinWidth = getControlMinWidth(layout, column);
		final String controlPrefWidth = getControlPrefWidth(layout, column);
		final String controlMaxWidth = getControlMaxWidth(layout, column);
		return controlMinWidth + ":" + controlPrefWidth + ":" + controlMaxWidth;
	}

	private static String getControlWidthConstraints(final IBeanFormLayout layout, final int column) {
		final String controlMinWidth = getControlMinWidth(layout, column);
		final String controlPrefWidth = getControlPrefWidth(layout, column);
		return "w " + controlMinWidth + ":" + controlPrefWidth + ":";
	}

	private static String getControlMinWidth(final IBeanFormLayout layout, final int column) {
		final Integer width = layout.getControlMinWidth(column);
		if (width != null) {
			return String.valueOf(width.intValue());
		}
		else {
			return String.valueOf("");
		}
	}

	private static String getControlPrefWidth(final IBeanFormLayout layout, final int column) {
		final Integer width = layout.getControlPrefWidth(column);
		if (width != null) {
			return String.valueOf(width.intValue());
		}
		else {
			return getControlMinWidth(layout, column);
		}
	}

	private static String getControlMaxWidth(final IBeanFormLayout layout, final int column) {
		final Integer width = layout.getControlMaxWidth(column);
		if (width != null) {
			return String.valueOf(width.intValue());
		}
		else {
			return String.valueOf("");
		}
	}

	private static String getColumnsConstraints(final IBeanFormLayout layout) {
		final StringBuilder result = new StringBuilder();
		result.append("0");
		for (int column = 0; column < layout.getColumnCount(); column++) {
			if (column > 0) {
				result.append("0");
			}

			result.append("[]"); // label column
			result.append('[');
			result.append("grow");
			final String widths = getColumnWidthConstraints(layout, column);
			if (!"".equals(widths)) {
				result.append(", ");
				result.append(widths);
			}
			result.append(']');

			result.append("[0::]"); // validation label column
		}
		result.append("0");
		return result.toString();
	}
}
