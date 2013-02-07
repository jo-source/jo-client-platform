/*
 * Copyright (c) 2013, grossmann
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

import org.jowidgets.api.controller.IExpandListener;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IExpandComposite;
import org.jowidgets.api.widgets.IWidget;
import org.jowidgets.api.widgets.blueprint.IExpandCompositeBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormInfo;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.Insets;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.Assert;

final class BeanFormLayouterImpl implements IBeanFormLayouter {

	private final IBeanFormLayout layout;
	private final boolean border;
	private final boolean headerBorder;
	private final boolean contentBorder;
	private final Insets insets;
	private final Insets contentInsets;
	private final Markup headerTextMarkup;
	private final IColorConstant headerTextColor;
	private final IColorConstant headerBackgroundColor;
	private final Markup contentTextMarkup;
	private final IColorConstant contentTextColor;
	private final IColorConstant contentBackgroundColor;
	private final BeanFormContentLayouter contentLayouter;

	BeanFormLayouterImpl(
		final IBeanFormLayout layout,
		final boolean border,
		final boolean headerBorder,
		final boolean contentBorder,
		final Insets insets,
		final Insets contentInsets,
		final Markup headerTextMarkup,
		final IColorConstant headerTextColor,
		final IColorConstant headerBackgroundColor,
		final Markup contentTextMarkup,
		final IColorConstant contentTextColor,
		final IColorConstant contentBackgroundColor) {

		Assert.paramNotNull(layout, "layout");
		Assert.paramNotNull(insets, "insets");
		Assert.paramNotNull(contentInsets, "contentInsets");
		Assert.paramNotNull(headerTextMarkup, "headerTextMarkup");
		Assert.paramNotNull(contentTextMarkup, "contentTextMarkup");

		this.layout = layout;
		this.border = border;
		this.headerBorder = headerBorder;
		this.contentBorder = contentBorder;
		this.insets = insets;
		this.contentInsets = contentInsets;
		this.headerTextMarkup = headerTextMarkup;
		this.headerTextColor = headerTextColor;
		this.headerBackgroundColor = headerBackgroundColor;
		this.contentTextMarkup = contentTextMarkup;
		this.contentTextColor = contentTextColor;
		this.contentBackgroundColor = contentBackgroundColor;

		this.contentLayouter = new BeanFormContentLayouter(layout);
	}

	@Override
	public void layout(final IContainer parent, final IBeanFormControlFactory controlFactory) {
		final IBeanFormInfo beanFormInfo = controlFactory.getBeanFormInfo();

		final IContainer container;
		if (controlFactory.getScrollbarsAllowed() && beanFormInfo != null) {
			parent.setLayout(MigLayoutFactory.growingInnerCellLayout());
			container = parent.add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		}
		else {
			container = parent;
		}

		final String colConstraints = insets.getLeft() + "[grow, 0::]" + insets.getRight();
		final String rowConstraints;
		if (beanFormInfo != null) {
			rowConstraints = insets.getTop() + "[][grow, 0::]" + insets.getBottom();
		}
		else {
			rowConstraints = insets.getTop() + "[grow, 0::]" + insets.getBottom();
		}
		container.setLayout(new MigLayoutDescriptor(colConstraints, rowConstraints));

		if (beanFormInfo != null) {
			final IExpandCompositeBluePrint expandCompositeBp = expandCompositeBp();
			expandCompositeBp.setExpanded(beanFormInfo.isExpanded());
			expandCompositeBp.setText(beanFormInfo.getHeader().get());
			expandCompositeBp.setIcon(beanFormInfo.getHeaderIcon());

			final IExpandComposite expandComposite = container.add(expandCompositeBp, "growx, w 0::, wrap");
			final String cc = contentInsets.getLeft() + "[grow, 0::]" + contentInsets.getRight();
			final String rc = contentInsets.getTop() + "[]" + contentInsets.getBottom();
			expandComposite.setLayout(new MigLayoutDescriptor(cc, rc));
			final ITextLabelBluePrint labelBp = BPF.textLabel(beanFormInfo.getText().get());
			labelBp.setForegroundColor(contentTextColor).setMarkup(contentTextMarkup);
			expandComposite.add(labelBp, "growx, w 0::");
			expandComposite.addExpandListener(new IExpandListener() {
				@Override
				public void expandedChanged(final boolean expanded) {
					getRootContainer(parent).layout();
				}
			});
		}
		final boolean contentScrollbar = beanFormInfo == null && controlFactory.getScrollbarsAllowed();
		final IContainer contentContainer = createAndAddContentContainer(container, controlFactory, contentScrollbar);
		contentLayouter.layout(contentContainer, controlFactory);
	}

	private IContainer getRootContainer(final IContainer container) {
		final IWidget parent = container.getParent();
		if (parent instanceof IContainer) {
			return getRootContainer((IContainer) parent);
		}
		else {
			return container;
		}
	}

	private IExpandCompositeBluePrint expandCompositeBp() {
		final IExpandCompositeBluePrint result = BPF.expandComposite();
		result.setTextColor(headerTextColor);
		result.setTextMarkup(headerTextMarkup);
		result.setHeaderBackgroundColor(headerBackgroundColor);
		result.setBackgroundColor(contentBackgroundColor);
		result.setBorder(border);
		result.setHeaderBorder(headerBorder);
		result.setContentBorder(contentBorder);
		return result;
	}

	private IContainer createAndAddContentContainer(
		final IContainer parent,
		final IBeanFormControlFactory controlFactory,
		final boolean scrollComposite) {

		final ICustomWidgetCreator<? extends IControl> mainValidationLabel = controlFactory.createMainValidationLabel();

		final IContainer outerContainer;
		if (mainValidationLabel != null) {
			final IExpandCompositeBluePrint expandCompositeBp = expandCompositeBp();
			expandCompositeBp.setExpanded(true);
			expandCompositeBp.setCustomHeader(mainValidationLabel);
			final IExpandComposite expandComposite = parent.add(expandCompositeBp, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
			expandComposite.setEnabled(false);
			outerContainer = expandComposite;
		}
		else {
			outerContainer = parent;
		}

		final IContainer innerContainer;
		if (scrollComposite) {
			outerContainer.setLayout(MigLayoutFactory.growingInnerCellLayout());
			innerContainer = outerContainer.add(BPF.scrollComposite(), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
		}
		else {
			innerContainer = outerContainer;
		}
		final String cc = contentInsets.getLeft() + "[grow, 0::]" + contentInsets.getRight();
		final String rc = contentInsets.getTop() + "[]" + contentInsets.getBottom();
		innerContainer.setLayout(new MigLayoutDescriptor(cc, rc));

		final String widthCC = getMainWidthConstraints(controlFactory);
		return innerContainer.add(BPF.composite(), "growx, " + widthCC + ", h 0::, aligny top");
	}

	private String getMainWidthConstraints(final IBeanFormControlFactory controlFactory) {
		final Integer minWidth = layout.getMinWidth();
		final Integer width = layout.getPrefWidth();
		Integer maxWidth = layout.getMaxWidth();
		if (maxWidth == null) {
			maxWidth = controlFactory.getMaxWidthDefault();
		}
		final StringBuilder result = new StringBuilder("w ");
		result.append(minWidth != null ? minWidth.intValue() : "0");
		result.append(":");
		result.append(width != null ? width.intValue() : "");
		result.append(":");
		result.append(maxWidth != null ? maxWidth.intValue() : "");
		return result.toString();
	}

}
