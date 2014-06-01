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

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.ICheckedItemModelBuilder;
import org.jowidgets.api.model.item.IItemModelFactory;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.ILabel;
import org.jowidgets.api.widgets.IValidationResultLabel;
import org.jowidgets.api.widgets.blueprint.ILabelBluePrint;
import org.jowidgets.api.widgets.blueprint.IValidationResultLabelBluePrint;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.IItemStateListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;

final class BeanTableValidationLabel<BEAN_TYPE> {

	private final IComposite composite;
	private final IBeanTableModel<BEAN_TYPE> model;
	private final IComposite content;

	private final IValidationResultLabel mainValidationLabel;
	private final ILabel processStateLabel;
	private final ICheckedItemModel validationLabelItemModel;
	private final IItemStateListener validationLabelItemListener;

	BeanTableValidationLabel(
		final IComposite composite,
		final BeanTableImpl<BEAN_TYPE> table,
		final IValidationLabelSetup validationlabelSetup) {

		this.composite = composite;
		this.model = table.getModel();

		this.validationLabelItemModel = createValidationLabelItemModel();
		this.validationLabelItemListener = new IItemStateListener() {
			@Override
			public void itemStateChanged() {
				table.setValidationLabelVisible(validationLabelItemModel.isSelected());
			}
		};
		validationLabelItemModel.addItemListener(validationLabelItemListener);

		this.content = composite.add(1, BPF.composite(), "growx, w 0::");
		content.setLayout(new MigLayoutDescriptor("wrap", "0[grow, 0::]0", "0[22!]0[]0"));

		final IComposite validationLabelContainer = content.add(BPF.composite(), "growx, w 30::");
		validationLabelContainer.setLayout(new MigLayoutDescriptor("wrap, hidemode 3", "3[grow, 0::]1", "0[22!]0"));

		final IValidationResultLabelBluePrint validationLabelBp = BPF.validationResultLabel();
		validationLabelBp.setSetup(validationlabelSetup);
		mainValidationLabel = validationLabelContainer.add(validationLabelBp, "growx, w 30::");

		//TODO Show process state and messages like in BeanFormControl
		final ILabelBluePrint processStateLabelBp = BPF.label();
		processStateLabelBp.setMarkup(Markup.EMPHASIZED);
		processStateLabelBp.setColor(Colors.STRONG);
		processStateLabelBp.setIcon(IconsSmall.WAIT_1);
		processStateLabel = validationLabelContainer.add(processStateLabelBp, "growx, w 30::");
		processStateLabel.setVisible(false);

		content.add(BPF.separator(), "growx, w 0::");

		content.setVisible(false);

		final IValidationConditionListener validationListener = new IValidationConditionListener() {
			@Override
			public void validationConditionsChanged() {
				onValidationConditionChanged();
			}
		};
		model.addValidationConditionListener(validationListener);

		final IModificationStateListener modificationListener = new IModificationStateListener() {
			@Override
			public void modificationStateChanged() {
				onValidationConditionChanged();
			}
		};
		model.addModificationStateListener(modificationListener);

		table.addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				model.removeValidationConditionListener(validationListener);
				model.removeModificationStateListener(modificationListener);
			}
		});
	}

	private void onValidationConditionChanged() {
		final IValidationResult validationResult = model.validate();
		if (!validationResult.isOk() || model.hasModifications()) {
			mainValidationLabel.setResult(validationResult);
		}
		else {
			mainValidationLabel.setEmpty();
		}
	}

	private ICheckedItemModel createValidationLabelItemModel() {
		final IItemModelFactory modelFactory = Toolkit.getModelFactoryProvider().getItemModelFactory();
		final ICheckedItemModelBuilder builder = modelFactory.checkedItemBuilder();
		final String text = Messages.getString("BeanTableValidationLabel.show_validation_label_text");
		final String tooltip = Messages.getString("BeanTableValidationLabel.show_validation_label_tooltip");
		builder.setText(text).setToolTipText(tooltip);
		return builder.build();
	}

	void setVisible(final boolean visible) {
		if (content.isVisible() != visible) {
			composite.layoutBegin();
			content.setVisible(visible);
			composite.layoutEnd();
			validationLabelItemModel.removeItemListener(validationLabelItemListener);
			validationLabelItemModel.setSelected(visible);
			validationLabelItemModel.addItemListener(validationLabelItemListener);
		}
	}

	ICheckedItemModel getItemModel() {
		return validationLabelItemModel;
	}

}
