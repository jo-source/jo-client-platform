/*
 * Copyright (c) 2011, nimoll
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

import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.IInputField;
import org.jowidgets.api.widgets.ITextLabel;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.table.BeanTableModelConfig;
import org.jowidgets.cap.ui.api.table.BeanTableSettings;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableSettings;
import org.jowidgets.cap.ui.api.table.IBeanTableSettingsBuilder;
import org.jowidgets.cap.ui.api.types.AutoScrollPolicy;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.WindowWrapper;
import org.jowidgets.util.Assert;

final class BeanTableSettingsDialogImpl extends WindowWrapper implements IBeanTableSettingsDialog {

	private final IBluePrintFactory bpF;
	private final IBeanTable<?> table;
	private final IBeanTableModel<?> model;
	private final IFrame frame;

	private final BeanTableAttributeListImpl beanTableAttributeListImpl;
	private IBeanTableSettings currentSettings;

	private boolean okPressed;

	private final ICheckBox autoSelection;
	private final ICheckBox autoUpdate;
	private final IInputField<Integer> autoUpdateInterval;
	private final IComboBox<AutoScrollPolicy> autoScrollPolicy;

	BeanTableSettingsDialogImpl(final IFrame frame, final IBeanTableSettingsDialogBluePrint setup) {
		super(frame);
		Assert.paramNotNull(frame, "frame"); //$NON-NLS-1$
		Assert.paramNotNull(setup, "setup"); //$NON-NLS-1$
		Assert.paramNotNull(setup.getTable(), "setup.getTable()"); //$NON-NLS-1$

		this.bpF = Toolkit.getBluePrintFactory();
		this.frame = frame;
		this.table = setup.getTable();
		this.model = table.getModel();
		this.currentSettings = setup.getTable().getSettings();

		frame.setLayout(new MigLayoutDescriptor("hidemode 2", "[][grow]", "[][]10[][]10[][]10[grow][pref!]")); //$NON-NLS-1$ //$NON-NLS-2$

		final String textCommonSettings = Messages.getString("BeanTableSettingsDialogImpl.common_settings"); //$NON-NLS-1$
		final String textAutoSelection = Messages.getString("BeanTableSettingsDialogImpl.auto_selection"); //$NON-NLS-1$
		final String textAutoUpdateSettings = Messages.getString("BeanTableSettingsDialogImpl.auto_update_settings");//"Auto update settings"; //$NON-NLS-1$
		final String textAutoUpdate = Messages.getString("BeanTableSettingsDialogImpl.auto_update");//"Auto update"; //$NON-NLS-1$
		final String textAutoUpdateInterval = Messages.getString("BeanTableSettingsDialogImpl.interval_sec");//"Interval (sec)"; //$NON-NLS-1$
		final String textAutoScrollPolicy = Messages.getString("BeanTableSettingsDialogImpl.auto_scroll");//"Auto scroll";
		final String textColumns = Messages.getString("BeanTableSettingsDialogImpl.columns"); //$NON-NLS-1$
		final String textSearch = Messages.getString("BeanTableSettingsDialogImpl.search_"); //$NON-NLS-1$

		// common settings
		frame.add(bpF.textSeparator(textCommonSettings), "grow, span"); //$NON-NLS-1$
		autoSelection = frame.add(bpF.checkBox().setText(textAutoSelection), "grow, span, wrap"); //$NON-NLS-1$ 

		final ITextLabel autoUpdateSeparator = frame.add(bpF.textSeparator(textAutoUpdateSettings), "grow, span"); //$NON-NLS-1$
		final IComposite autoUpdateBar = frame.add(bpF.composite(), "grow, span, wrap"); //$NON-NLS-1$ 
		autoUpdateBar.setLayout(new MigLayoutDescriptor("0[]20[][]20[][]0", "0[]0"));

		autoUpdate = autoUpdateBar.add(bpF.checkBox().setText(textAutoUpdate), ""); //$NON-NLS-1$ 
		autoUpdate.addInputListener(new IInputListener() {
			@Override
			public void inputChanged() {
				final boolean enabled = autoUpdate.getValue().booleanValue();
				autoScrollPolicy.setEnabled(enabled);
				autoUpdateInterval.setEnabled(enabled);
			}
		});

		autoUpdateBar.add(BPF.textLabel(textAutoUpdateInterval));
		autoUpdateInterval = autoUpdateBar.add(BPF.inputFieldIntegerNumber(), "w 60::");
		autoUpdateInterval.setEnabled(false);

		autoUpdateBar.add(BPF.textLabel(textAutoScrollPolicy));
		final IComboBoxSelectionBluePrint<AutoScrollPolicy> autoScrollBp = BPF.comboBoxSelection(new AutoScrollPolicyConverter());
		autoScrollBp.setElements(AutoScrollPolicy.OFF, AutoScrollPolicy.TO_SELECTION, AutoScrollPolicy.TO_END);
		autoScrollBp.setAutoCompletion(false);
		autoScrollPolicy = autoUpdateBar.add(autoScrollBp, "w 60::");
		autoScrollPolicy.setEnabled(false);

		if (!table.isAutoUpdateConfigurable()) {
			autoUpdateSeparator.setVisible(false);
			autoUpdateBar.setVisible(false);
		}

		frame.add(bpF.textSeparator(textColumns), "grow, span, wrap"); //$NON-NLS-1$ 
		frame.add(bpF.textLabel(textSearch), ""); //$NON-NLS-1$
		final IInputField<String> filter = frame.add(bpF.inputFieldString(), "wrap, grow"); //$NON-NLS-1$
		filter.addInputListener(new IInputListener() {

			@Override
			public void inputChanged() {
				beanTableAttributeListImpl.setFilter(filter.getText());
			}
		});

		beanTableAttributeListImpl = new BeanTableAttributeListImpl(frame.add(
				bpF.compositeWithBorder(),
				"grow, wrap, span, w 0::, h 0::"), model); //$NON-NLS-1$

		createButtonBar(frame.add(bpF.composite(), "alignx right, span, wrap")); //$NON-NLS-1$
	}

	@Override
	public IBeanTableSettings show() {
		okPressed = false;
		currentSettings = table.getSettings();
		final IBeanTableModelConfig modelConfig = currentSettings.getModelConfig();
		if (modelConfig.isAutoSelection() != null) {
			autoSelection.setValue(modelConfig.isAutoSelection());
		}
		autoUpdate.setValue(currentSettings.isAutoUpdate());
		autoUpdateInterval.setValue(currentSettings.getAutoUpdateInterval());
		autoScrollPolicy.setValue(currentSettings.getAutoScrollPolicy());
		beanTableAttributeListImpl.updateValues(modelConfig);
		frame.setVisible(true);

		if (okPressed) {
			return getUserSettings();
		}
		else {
			return currentSettings;
		}
	}

	private IBeanTableSettings getUserSettings() {
		final IBeanTableSettingsBuilder builder = BeanTableSettings.builder();
		builder.setModelConfig(getUserModelConfig());
		builder.setAutoUpdate(autoUpdate.getValue().booleanValue());
		if (autoUpdateInterval.getValue() != null) {
			builder.setAutoUpdateInterval(autoUpdateInterval.getValue());
		}
		else {
			builder.setAutoUpdateInterval(currentSettings.getAutoUpdateInterval());
		}
		builder.setAutoScrollPolicy(autoScrollPolicy.getValue());
		return builder.build();
	}

	private IBeanTableModelConfig getUserModelConfig() {
		final IBeanTableModelConfigBuilder builder = BeanTableModelConfig.builder();
		beanTableAttributeListImpl.buildConfig(builder);
		builder.setAutoSelection(autoSelection.getValue());
		return builder.build();
	}

	@Override
	public boolean isOkPressed() {
		return okPressed;
	}

	private void createButtonBar(final IComposite buttonBar) {
		buttonBar.setLayout(new MigLayoutDescriptor("0[][]0", "0[]0")); //$NON-NLS-1$ //$NON-NLS-2$
		final IButton ok = buttonBar.add(
				bpF.button(Messages.getString("BeanTableSettingsDialogImpl.ok")), "w 80::, aligny b, sg bg"); //$NON-NLS-1$ //$NON-NLS-2$
		ok.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				okPressed = true;
				setVisible(false);
			}
		});
		frame.setDefaultButton(ok);

		final IButton cancel = buttonBar.add(
				bpF.button(Messages.getString("BeanTableSettingsDialogImpl.cancel")), "w 80::, aligny b, sg bg"); //$NON-NLS-1$ //$NON-NLS-2$
		cancel.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				setVisible(false);
			}
		});
	}

	private static class AutoScrollPolicyConverter implements IObjectStringConverter<AutoScrollPolicy> {

		private final String off = Messages.getString("BeanTableSettingsDialogImpl.auto_scroll_off");//"of"; //$NON-NLS-1$
		private final String selection = Messages.getString("BeanTableSettingsDialogImpl.auto_scroll_selection");//"Selected"; //$NON-NLS-1$
		private final String end = Messages.getString("BeanTableSettingsDialogImpl.auto_scroll_end");//"end"; //$NON-NLS-1$

		@Override
		public String convertToString(final AutoScrollPolicy value) {
			if (value == AutoScrollPolicy.OFF) {
				return off;
			}
			else if (value == AutoScrollPolicy.TO_SELECTION) {
				return selection;
			}
			else if (value == AutoScrollPolicy.TO_END) {
				return end;
			}
			return null;
		}

		@Override
		public String getDescription(final AutoScrollPolicy value) {
			return null;
		}

	}
}
