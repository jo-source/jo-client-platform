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

import java.net.URL;

import org.jowidgets.api.convert.IConverterProvider;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.IToolkit;
import org.jowidgets.api.toolkit.IToolkitInterceptor;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.dto.IDocument;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanLinkPanelBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetailBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeDetailSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanRelationTreeSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionFormSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IExecutionTaskDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.image.IImageRegistry;
import org.jowidgets.common.widgets.factory.IGenericWidgetFactory;

final class CapToolkitInterceptor implements IToolkitInterceptor {

	@Override
	public void onToolkitCreate(final IToolkit toolkit) {
		registerWidgets(toolkit);
		registerIcons(toolkit);
		addDefaultsInitializer(toolkit);
		setBuilderConvenience(toolkit);
		registerConverter(toolkit);
	}

	@SuppressWarnings("unchecked")
	private void registerWidgets(final IToolkit toolkit) {
		final IGenericWidgetFactory factory = toolkit.getWidgetFactory();
		factory.register(IBeanTableBluePrint.class, new BeanTableFactory());
		factory.register(IBeanSelectionTableBluePrint.class, new BeanSelectionTableFactory());
		factory.register(IBeanSelectionDialogBluePrint.class, new BeanSelectionDialogFactory());
		factory.register(IBeanFormBluePrint.class, new BeanFormFactory());
		factory.register(IBeanDialogBluePrint.class, new BeanDialogFactory());
		factory.register(IBeanTableSettingsDialogBluePrint.class, new BeanTableSettingsDialogFactory());
		factory.register(ISingleBeanFormBluePrint.class, new SingleBeanFormFactory());
		factory.register(IAttributeFilterControlBluePrint.class, new AttributeFilterControlFactory());
		factory.register(ILookUpComboBoxSelectionBluePrint.class, new LookUpComboBoxSelectionFactory());
		factory.register(ILookUpCollectionInputFieldBluePrint.class, new LookUpCollectionInputFieldFactory());
		factory.register(IBeanTabFolderBluePrint.class, new BeanTabFolderFactory());
		factory.register(IBeanRelationTreeBluePrint.class, new BeanRelationTreeFactory());
		factory.register(IBeanRelationTreeDetailBluePrint.class, new BeanRelationTreeDetailFactory());
		factory.register(IBeanSelectionFormBluePrint.class, new BeanSelectionFormFactory());
		factory.register(IExecutionTaskDialogBluePrint.class, new ExecutionTaskDialogFactory());
		factory.register(IBeanLinkPanelBluePrint.class, new BeanLinkPanelFactory());
		factory.register(IBeanLinkDialogBluePrint.class, new BeanLinkDialogFactory());
	}

	private void registerIcons(final IToolkit toolkit) {
		final IImageRegistry registry = toolkit.getImageRegistry();
		registry.registerImageConstant(CapIcons.TABLE_HIDE_COLUMN, IconsSmall.SUB);
		registry.registerImageConstant(CapIcons.TABLE_UNHIDE_ALL_COLUMNS, IconsSmall.ADD_ALL);
		registry.registerImageConstant(CapIcons.TABLE_SETTINGS, IconsSmall.SETTINGS);
		registry.registerImageConstant(CapIcons.ADD_LINK, IconsSmall.ADD);
		registry.registerImageConstant(CapIcons.REMOVE_LINK, IconsSmall.SUB);
		registerImage(registry, CapIcons.GRAPH_ANIMATION, "graph_animation.png");
		registerImage(registry, CapIcons.GRAPH_LETTERING, "graph_lettering.png");
		registerImage(registry, CapIcons.GRAPH_SETTINGS, "graph_settings.gif");
		registerImage(registry, CapIcons.GRAPH_SNAPSHOT, "graph_snapshot.png");
		registerImage(registry, CapIcons.NODE_EXPANDED, "node_expanded.png");
		registerImage(registry, CapIcons.NODE_CONTRACTED, "node_contracted.png");
	}

	private void registerImage(final IImageRegistry registry, final IImageConstant imageConstant, final String relPath) {
		final URL url = getClass().getClassLoader().getResource("org/jowidgets/cap/ui/icons/" + relPath);
		registry.registerImageConstant(imageConstant, url);
	}

	private void registerConverter(final IToolkit toolkit) {
		final IConverterProvider converterProvider = toolkit.getConverterProvider();
		converterProvider.register(IDocument.class, new DocumentConverter());
	}

	private void addDefaultsInitializer(final IToolkit toolkit) {
		final IBluePrintFactory bpf = toolkit.getBluePrintFactory();
		bpf.addDefaultsInitializer(IBeanTableSetupBuilder.class, new BeanTableDefaults());
		bpf.addDefaultsInitializer(IBeanSelectionTableBluePrint.class, new BeanSelectionTableDefaults());
		bpf.addDefaultsInitializer(IBeanSelectionDialogBluePrint.class, new BeanSelectionDialogDefaults());
		bpf.addDefaultsInitializer(IBeanDialogBluePrint.class, new BeanDialogDefaults());
		bpf.addDefaultsInitializer(IBeanFormBluePrint.class, new BeanFormDefaults());
		bpf.addDefaultsInitializer(IBeanRelationTreeBluePrint.class, new BeanRelationTreeDefaults());
		bpf.addDefaultsInitializer(IBeanSelectionFormSetupBuilder.class, new BeanSelectionFormDefaults());
		bpf.addDefaultsInitializer(IExecutionTaskDialogBluePrint.class, new ExecutionTaskDialogDefaults());
	}

	private void setBuilderConvenience(final IToolkit toolkit) {
		final IBluePrintFactory bpf = toolkit.getBluePrintFactory();
		bpf.setSetupBuilderConvenience(IBeanTableSetupBuilder.class, new BeanTableSetupConvenience());
		bpf.setSetupBuilderConvenience(IBeanTabFolderBluePrint.class, new BeanTabFolderSetupConvenience());
		bpf.setSetupBuilderConvenience(IBeanFormBluePrint.class, new BeanFormSetupConvenience());
		bpf.setSetupBuilderConvenience(IBeanSelectionFormSetupBuilder.class, new BeanSelectionFormSetupConvenience());
		bpf.setSetupBuilderConvenience(IBeanRelationTreeSetupBuilder.class, new BeanRelationTreeSetupConvenience());
		bpf.setSetupBuilderConvenience(IBeanRelationTreeDetailSetupBuilder.class, new BeanRelationTreeDetailSetupConvenience());
	}

}
