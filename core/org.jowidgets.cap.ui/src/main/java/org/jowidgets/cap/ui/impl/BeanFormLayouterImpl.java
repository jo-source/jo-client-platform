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
import java.util.List;

import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.cap.ui.api.form.BeanFormGroupRendering;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormGroup;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormProperty;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.AlignmentVertical;
import org.jowidgets.common.types.Position;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.util.Assert;

final class BeanFormLayouterImpl implements IBeanFormLayouter {

	private final IBeanFormLayout layout;

	BeanFormLayouterImpl(final IBeanFormLayout layout) {
		Assert.paramNotNull(layout, "layout");
		this.layout = layout;
	}

	@Override
	public void layout(final IContainer globalContainer, final IBeanFormControlFactory controlFactory) {
		globalContainer.setLayout(new MigLayoutDescriptor(getColumnConstraints(layout), ""));

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
				container = globalContainer;
			}
			else if (BeanFormGroupRendering.SEPARATOR.equals(rendering)) {
				final String baseConstraints = "growx, cell 0 " + row + " " + (3 * layout.getColumnCount()) + " 1";
				setUsed(globalGrid, row, 0, 1, layout.getColumnCount());

				if (label != null && !"".equals(label)) {
					final String gapTop = (row > 0) ? "gaptop 30" : "";
					final String gapBottom = "gapbottom 10";
					final String cell = constraints(baseConstraints, gapTop, gapBottom);
					globalContainer.add(Toolkit.getBluePrintFactory().textSeparator(label), cell);
				}
				else if (row > 0 && showSeparators) {
					final String cell = constraints(baseConstraints, "gaptop 20, gapbottom 20");
					globalContainer.add(Toolkit.getBluePrintFactory().separator(), cell);
				}
				grid = globalGrid;
				container = globalContainer;
			}
			else if (BeanFormGroupRendering.BORDER.equals(rendering)) {
				final String cell = "growx, cell 0 " + row + " " + (3 * layout.getColumnCount()) + " 1";

				setUsed(globalGrid, row, 0, 1, layout.getColumnCount());
				if (label != null && !"".equals(label)) {
					container = globalContainer.add(Toolkit.getBluePrintFactory().composite(label), cell);
				}
				else {
					container = globalContainer.add(Toolkit.getBluePrintFactory().compositeWithBorder(), cell);
				}
				container.setLayout(new MigLayoutDescriptor(getColumnConstraints(layout), ""));
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

				final String sizeGroupLabel = "sg lbl";
				final String sizeGroupControl = "sgy r" + row + "ctrl";
				final ICustomWidgetCreator<? extends IControl> validationLabelCreator = controlFactory.createValidationLabel(
						propertyName,
						property.getValidationLabel());
				final String controlConstraints = "";

				final int firstPropertyColumn = (3 * logicalColumn);

				//add label
				String cell;
				if (property.showLabel()) {
					final AlignmentVertical labelAlignmentVertical = property.getLabelAlignmentVertical();
					if (AlignmentVertical.TOP.equals(labelAlignmentVertical)) {
						cell = "cell " + firstPropertyColumn + " " + row;
					}
					else if (AlignmentVertical.CENTER.equals(labelAlignmentVertical)) {
						cell = "cell " + firstPropertyColumn + " " + row + " 1 " + propertyRowSpan;
					}
					else if (AlignmentVertical.BOTTOM.equals(labelAlignmentVertical)) {
						cell = "cell " + firstPropertyColumn + " " + (row + propertyRowSpan - 1);
					}
					else {
						throw new IllegalStateException("Unknown vertical alignment '" + labelAlignmentVertical + "'.");
					}

					final ITextLabelBluePrint textLabelBp = Toolkit.getBluePrintFactory().textLabel(
							controlFactory.getLabel(propertyName));
					setAlignmentHorizontal(property.getLabelAlignmentHorizontal(), textLabelBp);
					container.add(textLabelBp, constraints(cell, sizeGroupLabel));
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

				//add control
				container.add(controlCreator, constraints(cell, sizeGroupControl, controlConstraints, "growx", "growy"));

				//add validation label
				if (validationLabelCreator != null) {
					container.add(validationLabelCreator, "w 25::");
				}

				logicalColumn = logicalColumn + propertyColumnCount;
			}
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

	private static void setAlignmentHorizontal(final AlignmentHorizontal alignment, final ITextLabelBluePrint textLabelBp) {
		if (AlignmentHorizontal.LEFT.equals(alignment)) {
			textLabelBp.alignLeft();
		}
		else if (AlignmentHorizontal.CENTER.equals(alignment)) {
			textLabelBp.alignCenter();
		}
		else if (AlignmentHorizontal.RIGHT.equals(alignment)) {
			textLabelBp.alignRight();
		}
		else {
			throw new IllegalStateException("Unknown horizontal alignment '" + alignment + "'.");
		}
	}

	private static String constraints(final String... constraints) {
		final StringBuilder result = new StringBuilder();
		for (final String constraint : constraints) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(constraint);
		}
		return result.toString();
	}

	private static String getColumnConstraints(final IBeanFormLayout layout) {
		final StringBuilder result = new StringBuilder();
		result.append("0");
		for (int column = 0; column < layout.getColumnCount(); column++) {
			if (column > 0) {
				// TODO NM add gap ?
				result.append("0");
			}

			result.append("[]"); // label column

			final Integer controlMinWidth = layout.getControlMinWidth(column);
			final Integer controlMaxWidth = layout.getControlMaxWidth(column);
			result.append('[');
			result.append("grow");
			if (controlMinWidth != null || controlMaxWidth != null) {
				result.append(", ");
				if (controlMinWidth != null) {
					result.append(controlMinWidth.toString());
				}
				result.append("::");
				if (controlMaxWidth != null) {
					result.append(controlMaxWidth.toString());
				}
			}
			result.append(']');

			result.append("[0::]"); // validation label column
		}
		result.append("0");

		return result.toString();
	}
}
