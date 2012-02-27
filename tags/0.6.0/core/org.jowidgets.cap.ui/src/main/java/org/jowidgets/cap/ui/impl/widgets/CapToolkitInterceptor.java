/*
 * Copyright (c) 2012, grossmann
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

import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.IToolkit;
import org.jowidgets.api.toolkit.IToolkitInterceptor;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableFormSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;
import org.jowidgets.common.image.IImageRegistry;
import org.jowidgets.common.widgets.factory.IGenericWidgetFactory;

final class CapToolkitInterceptor implements IToolkitInterceptor {

	@Override
	public void onToolkitCreate(final IToolkit toolkit) {
		registerWidgets(toolkit);
		registerIcons(toolkit);
		addDefaultsInitializer(toolkit);
		setBuilderConvenience(toolkit);
	}

	@SuppressWarnings("unchecked")
	private void registerWidgets(final IToolkit toolkit) {
		final IGenericWidgetFactory genericWidgetFactory = toolkit.getWidgetFactory();
		genericWidgetFactory.register(IBeanTableBluePrint.class, new BeanTableFactory());
		genericWidgetFactory.register(IBeanSelectionTableBluePrint.class, new BeanSelectionTableFactory());
		genericWidgetFactory.register(IBeanSelectionDialogBluePrint.class, new BeanSelectionDialogFactory());
		genericWidgetFactory.register(IBeanFormBluePrint.class, new BeanFormFactory());
		genericWidgetFactory.register(IBeanDialogBluePrint.class, new BeanDialogFactory());
		genericWidgetFactory.register(IBeanTablesFormBluePrint.class, new BeanTablesFormFactory());
		genericWidgetFactory.register(IBeanTableFormBluePrint.class, new BeanTableFormFactory());
		genericWidgetFactory.register(IBeanTableSettingsDialogBluePrint.class, new BeanTableSettingsDialogFactory());
		genericWidgetFactory.register(ISingleBeanFormBluePrint.class, new SingleBeanFormFactory());
		genericWidgetFactory.register(IAttributeFilterControlBluePrint.class, new AttributeFilterControlFactory());
		genericWidgetFactory.register(ILookUpComboBoxSelectionBluePrint.class, new LookUpComboBoxSelectionFactory());
		genericWidgetFactory.register(ILookUpCollectionInputFieldBluePrint.class, new LookUpCollectionInputFieldFactory());
		genericWidgetFactory.register(IBeanTabFolderBluePrint.class, new BeanTabFolderFactory());
	}

	private void registerIcons(final IToolkit toolkit) {
		final IImageRegistry imageRegistry = toolkit.getImageRegistry();
		imageRegistry.registerImageConstant(CapIcons.TABLE_HIDE_COLUMN, IconsSmall.SUB);
		imageRegistry.registerImageConstant(CapIcons.TABLE_UNHIDE_ALL_COLUMNS, IconsSmall.ADD_ALL);
		imageRegistry.registerImageConstant(CapIcons.TABLE_SETTINGS, IconsSmall.SETTINGS);
	}

	private void addDefaultsInitializer(final IToolkit toolkit) {
		final IBluePrintFactory bpf = toolkit.getBluePrintFactory();
		bpf.addDefaultsInitializer(IBeanTableSetupBuilder.class, new BeanTableDefaults());
		bpf.addDefaultsInitializer(IBeanSelectionTableBluePrint.class, new BeanSelectionTableDefaults());
		bpf.addDefaultsInitializer(IBeanSelectionDialogBluePrint.class, new BeanSelectionDialogDefaults());
		bpf.addDefaultsInitializer(IBeanTableFormSetupBuilder.class, new BeanTableFormDefaults());
		bpf.addDefaultsInitializer(IBeanDialogBluePrint.class, new BeanDialogDefaults());
		bpf.addDefaultsInitializer(IBeanFormBluePrint.class, new BeanFormDefaults());
	}

	private void setBuilderConvenience(final IToolkit toolkit) {
		final IBluePrintFactory bpf = toolkit.getBluePrintFactory();
		bpf.setSetupBuilderConvenience(IBeanTabFolderBluePrint.class, new BeanTabFolderSetupConvenience());
		bpf.setSetupBuilderConvenience(IBeanFormBluePrint.class, new BeanFormSetupConvenience());
	}

}
