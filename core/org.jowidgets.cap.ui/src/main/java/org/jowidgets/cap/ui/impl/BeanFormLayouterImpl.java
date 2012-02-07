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

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
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
import org.jowidgets.common.widgets.layout.ILayoutDescriptor;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

final class BeanFormLayouterImpl implements IBeanFormLayouter {

	private static final String DEFAULT_CONTROL_MIN_WIDTH = String.valueOf(60);

	private final IBeanFormLayout layout;

	BeanFormLayouterImpl(final IBeanFormLayout layout) {
		Assert.paramNotNull(layout, "layout");
		this.layout = layout;
	}

	@Override
	public void layout(final IContainer globalContainer, final IBeanFormControlFactory controlFactory) {
		final Integer minWidth = layout.getMinWidth();
		final Integer width = layout.getWidth();
		final Integer maxWidth = layout.getMaxWidth();
		final IAction saveAction = controlFactory.getSaveAction();
		final IAction undoAction = controlFactory.getUndoAction();
		final boolean hasButtons = saveAction != null || undoAction != null;
		if (minWidth != null || width != null || maxWidth != null || saveAction != null || undoAction != null) {
			final String widthCC = getWidthConstraints(minWidth, width, maxWidth);
			final String buttonBarCC = getButtonBarCC(hasButtons);
			globalContainer.setLayout(new MigLayoutDescriptor("0[grow, 0::]0", "0[" + widthCC + "]" + buttonBarCC + "0"));
			final String innerContainerCC = getInnerContainerCC(hasButtons, widthCC);
			final IComposite innerContainer = globalContainer.add(BPF.composite(), innerContainerCC);
			layoutInnerContainer(innerContainer, controlFactory);
			if (hasButtons) {
				//TODO MG make the buttons align right with the controls (but not with the validation labels)
				final IComposite buttonBar = globalContainer.add(BPF.composite(), "growx, w " + widthCC + ", alignx left");
				buttonBar.setLayout(getButtonBarLayout(saveAction, undoAction));
				if (undoAction != null) {
					buttonBar.add(BPF.button(), "sg bg").setAction(undoAction);
					buttonBar.add(BPF.button(), "sg bg").setAction(saveAction);
				}
			}
		}
		else {
			layoutInnerContainer(globalContainer, controlFactory);
		}
	}

	private ILayoutDescriptor getButtonBarLayout(final IAction saveAction, final IAction undoAction) {
		if (saveAction != null && undoAction != null) {
			return new MigLayoutDescriptor("0[][]0", "0[]0");
		}
		else {
			return new MigLayoutDescriptor("0[]0", "0[]0");
		}
	}

	private String getInnerContainerCC(final boolean hasButtons, final String widthCC) {
		final StringBuilder result = new StringBuilder("growx, w " + widthCC + ", h 0::");
		if (hasButtons) {
			result.append(",wrap");
		}
		return result.toString();
	}

	private String getButtonBarCC(final boolean hasButtons) {
		if (hasButtons) {
			return "10[]";
		}
		else {
			return "";
		}
	}

	private String getWidthConstraints(final Integer minWidth, final Integer width, final Integer maxWidth) {
		final StringBuilder result = new StringBuilder();
		result.append(minWidth != null ? minWidth.intValue() : "0");
		result.append(":");
		result.append(width != null ? width.intValue() : "");
		result.append(":");
		result.append(maxWidth != null ? maxWidth.intValue() : "");
		return result.toString();
	}

	private void layoutInnerContainer(final IContainer innerContainer, final IBeanFormControlFactory controlFactory) {
		innerContainer.setLayout(new MigLayoutDescriptor(getColumnsConstraints(layout), ""));

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
				container = innerContainer;
			}
			else if (BeanFormGroupRendering.SEPARATOR.equals(rendering)) {
				final String baseConstraints = "growx, cell 0 " + row + " " + (3 * layout.getColumnCount()) + " 1";
				setUsed(globalGrid, row, 0, 1, layout.getColumnCount());

				if (label != null && !"".equals(label)) {
					final String gapTop = (row > 0) ? "gaptop 27" : "";
					final String gapBottom = "gapbottom 7";
					final String cell = constraints(baseConstraints, gapTop, gapBottom);
					innerContainer.add(Toolkit.getBluePrintFactory().textSeparator(label), cell);
				}
				else if (row > 0 && showSeparators) {
					final String cell = constraints(baseConstraints, "gaptop 17, gapbottom 17");
					innerContainer.add(Toolkit.getBluePrintFactory().separator(), cell);
				}
				grid = globalGrid;
				container = innerContainer;
			}
			else if (BeanFormGroupRendering.BORDER.equals(rendering)) {
				final String cell = "growx, cell 0 " + row + " " + (3 * layout.getColumnCount()) + " 1";

				setUsed(globalGrid, row, 0, 1, layout.getColumnCount());
				if (label != null && !"".equals(label)) {
					container = innerContainer.add(Toolkit.getBluePrintFactory().composite(label), cell);
				}
				else {
					container = innerContainer.add(Toolkit.getBluePrintFactory().compositeWithBorder(), cell);
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

				final String sizeGroupLabel = "sg lbl";
				final String sizeGroupControl = "sgy r" + row + "ctrlspn" + propertyRowSpan;
				final ICustomWidgetCreator<? extends IControl> validationLabelCreator = controlFactory.createValidationLabel(
						propertyName,
						property.getValidationLabel());
				final String controlConstraints = getControlWidthConstraints(layout, logicalColumn);

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

				container.add(
						controlCreator,
						constraints(
								cell,
								sizeGroupControl,
								controlConstraints,
								getHeightConstraints(property.getHeight()),
								"growx",
								"growy"));

				//add validation label
				if (validationLabelCreator != null) {
					container.add(validationLabelCreator, "w 25::");
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
			if (result.length() > 0 && !"".equals(constraint)) {
				result.append(", ");
			}
			result.append(constraint);
		}
		return result.toString();
	}

	private static String getColumnWidthConstraints(final IBeanFormLayout layout, final int column) {
		final String controlMinWidth = getControlMinWidth(layout, column);
		final String controlMaxWidth = getControlMaxWidth(layout, column);
		return controlMinWidth + ":" + controlMinWidth + ":" + controlMaxWidth;
	}

	private static String getControlWidthConstraints(final IBeanFormLayout layout, final int column) {
		final String controlMinWidth = getControlMinWidth(layout, column);
		final String controlMaxWidth = getControlMaxWidth(layout, column);
		return "width " + controlMinWidth + ":" + controlMinWidth + ":" + controlMaxWidth;
	}

	private static String getControlMinWidth(final IBeanFormLayout layout, final int column) {
		final Integer width = layout.getControlMinWidth(column);
		if (width != null) {
			return String.valueOf(width.intValue());
		}
		else {
			return DEFAULT_CONTROL_MIN_WIDTH;
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
