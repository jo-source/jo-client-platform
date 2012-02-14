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

package org.jowidgets.cap.ui.api;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.IToolkit;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.types.InputDialogDefaultButtonPolicy;
import org.jowidgets.api.widgets.ITabItem;
import org.jowidgets.api.widgets.blueprint.IInputComponentValidationLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.defaults.IDefaultInitializer;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.color.CapColors;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.control.IDisplayFormatFactory;
import org.jowidgets.cap.ui.api.control.IInputControlSupportRegistry;
import org.jowidgets.cap.ui.api.converter.ICapConverterFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.filter.IFilterToolkit;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.cap.ui.api.lookup.ILookUpCache;
import org.jowidgets.cap.ui.api.model.ISingleBeanModelBuilder;
import org.jowidgets.cap.ui.api.sort.ISortModelConfigBuilder;
import org.jowidgets.cap.ui.api.tabfolder.IBeanTabFolderModelBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuFactory;
import org.jowidgets.cap.ui.api.table.IBeanTableMenuInterceptor;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IAttributeFilterControlBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormSetupConvenience;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanSelectionTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTab;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFactory;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTabFolderSetupConvenience;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableFormSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanTablesFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.api.widgets.ILookUpCollectionInputFieldBluePrint;
import org.jowidgets.cap.ui.api.widgets.ILookUpComboBoxSelectionBluePrint;
import org.jowidgets.cap.ui.api.widgets.ISingleBeanFormBluePrint;
import org.jowidgets.cap.ui.api.workbench.ICapWorkbenchToolkit;
import org.jowidgets.cap.ui.impl.DefaultCapUiToolkit;
import org.jowidgets.cap.ui.impl.widgets.AttributeFilterControlFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanDialogFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanFormFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanSelectionDialogFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanSelectionTableFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTabFolderFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTableFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTableFormFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTableSettingsDialogFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTablesFormFactory;
import org.jowidgets.cap.ui.impl.widgets.LookUpCollectionInputFieldFactory;
import org.jowidgets.cap.ui.impl.widgets.LookUpComboBoxSelectionFactory;
import org.jowidgets.cap.ui.impl.widgets.SingleBeanFormFactory;
import org.jowidgets.common.types.TableSelectionPolicy;
import org.jowidgets.common.widgets.factory.IGenericWidgetFactory;
import org.jowidgets.tools.controller.TabItemAdapter;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.tools.validation.MandatoryValidator;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.blueprint.convenience.AbstractSetupBuilderConvenience;
import org.jowidgets.util.IDecorator;
import org.jowidgets.validation.IValidationResult;

public final class CapUiToolkit {

	private static Map<IToolkit, ICapUiToolkit> toolkits = new WeakHashMap<IToolkit, ICapUiToolkit>();

	private CapUiToolkit() {}

	public static void initialize() {
		getInstance();
	}

	public static ICapUiToolkit getInstance() {
		final IToolkit currentToolkit = Toolkit.getInstance();
		ICapUiToolkit result = toolkits.get(currentToolkit);
		if (result == null) {
			result = createDefaultInstance(currentToolkit);
			toolkits.put(currentToolkit, result);
		}
		return result;
	}

	public static ICapApiBluePrintFactory bluePrintFactory() {
		return getInstance().bluePrintFactory();
	}

	public static ICapActionFactory actionFactory() {
		return getInstance().actionFactory();
	}

	public static ICapConverterFactory converterFactory() {
		return getInstance().converterFactory();
	}

	public static <BEAN_TYPE> IBeanTableMenuFactory<BEAN_TYPE> beanTableMenuFactory(
		final Collection<IBeanTableMenuInterceptor<BEAN_TYPE>> interceptors) {
		return getInstance().beanTableMenuFactory(interceptors);
	}

	public static <BEAN_TYPE> IBeanTableMenuFactory<BEAN_TYPE> beanTableMenuFactory() {
		return getInstance().beanTableMenuFactory();
	}

	public static IDisplayFormatFactory displayFormatFactory() {
		return getInstance().displayFormatFactory();
	}

	public static IInputControlSupportRegistry inputControlRegistry() {
		return getInstance().inputControlRegistry();
	}

	public static IAttributeToolkit attributeToolkit() {
		return getInstance().attributeToolkit();
	}

	public static IFilterToolkit filterToolkit() {
		return getInstance().filterToolkit();
	}

	public static ICapWorkbenchToolkit workbenchToolkit() {
		return getInstance().workbenchToolkit();
	}

	public static IExecutionTaskFactory executionTaskFactory() {
		return getInstance().executionTaskFactory();
	}

	public static <BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> beansStateTracker() {
		return getInstance().beansStateTracker();
	}

	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> beanProxyFactory(final Class<? extends BEAN_TYPE> proxyType) {
		return getInstance().beanProxyFactory(proxyType);
	}

	public static IBeanKeyFactory beanKeyFactory() {
		return getInstance().beanKeyFactory();
	}

	public static <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTableModelBuilder(beanType);
	}

	public static <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTableModelBuilder(entityId, beanType);
	}

	public static IBeanTableModelBuilder<IBeanDto> beanTableModelBuilder(final Object entityId) {
		return getInstance().beanTableModelBuilder(entityId);
	}

	public static <BEAN_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> beanTabFolderModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTabFolderModelBuilder(beanType);
	}

	public static <BEAN_TYPE> IBeanTabFolderModelBuilder<BEAN_TYPE> beanTabFolderModelBuilder(
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTabFolderModelBuilder(entityId, beanType);
	}

	public static IBeanTabFolderModelBuilder<IBeanDto> beanTabFolderModelBuilder(final Object entityId) {
		return getInstance().beanTabFolderBuilder(entityId);
	}

	public static <BEAN_TYPE> ISingleBeanModelBuilder<BEAN_TYPE> singleBeanModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().singleBeanModelBuilder(beanType);
	}

	public static <BEAN_TYPE> ISingleBeanModelBuilder<BEAN_TYPE> singleBeanModelBuilder(
		final Object entityId,
		final Class<BEAN_TYPE> beanType) {
		return getInstance().singleBeanModelBuilder(entityId, beanType);
	}

	public static ISingleBeanModelBuilder<IBeanDto> singleBeanModelBuilder(final Object entityId) {
		return getInstance().singleBeanModelBuilder(entityId);
	}

	public static IBeanTableConfigBuilder beanTableConfigBuilder() {
		return getInstance().beanTableConfigBuilder();
	}

	public static IBeanMessageBuilder beanMessageBuilder(final BeanMessageType type) {
		return getInstance().beanMessageBuilder(type);
	}

	public static IBeanMessageFixBuilder beanMessageFixBuilder() {
		return getInstance().beanMessageFixBuilder();
	}

	public static ISortModelConfigBuilder sortModelConfigBuilder() {
		return getInstance().sortModelConfigBuilder();
	}

	public static IBeanFormToolkit beanFormToolkit() {
		return getInstance().beanFormToolkit();
	}

	public static ILookUpCache lookUpCache() {
		return getInstance().lookUpCache();
	}

	private static ICapUiToolkit createDefaultInstance(final IToolkit toolkit) {
		registerWidgets(toolkit);
		return new DefaultCapUiToolkit();
	}

	@SuppressWarnings("unchecked")
	private static void registerWidgets(final IToolkit toolkit) {
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

		toolkit.getImageRegistry().registerImageConstant(CapIcons.TABLE_HIDE_COLUMN, IconsSmall.SUB);
		toolkit.getImageRegistry().registerImageConstant(CapIcons.TABLE_UNHIDE_ALL_COLUMNS, IconsSmall.ADD_ALL);
		toolkit.getImageRegistry().registerImageConstant(CapIcons.TABLE_SETTINGS, IconsSmall.SETTINGS);

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanTableSetupBuilder.class,
				new IDefaultInitializer<IBeanTableSetupBuilder<?>>() {
					@Override
					public void initialize(final IBeanTableSetupBuilder<?> bluePrint) {
						bluePrint.setSelectionPolicy(TableSelectionPolicy.MULTI_ROW_SELECTION);
						bluePrint.setColumnsMoveable(true);
						bluePrint.setColumnsResizeable(true);
						bluePrint.setDefaultMenus(true);
						bluePrint.setDefaultCreatorAction(true);
						bluePrint.setDefaultDeleterAction(true);
						bluePrint.setSearchFilterToolbarVisible(false);
					}
				});

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanSelectionTableBluePrint.class,
				new IDefaultInitializer<IBeanSelectionTableBluePrint<?>>() {
					@Override
					public void initialize(final IBeanSelectionTableBluePrint<?> bluePrint) {
						bluePrint.setSearchFilterToolbarVisible(true);
						bluePrint.setMandatorySelectionValidator(true);
					}
				});

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanSelectionDialogBluePrint.class,
				new IDefaultInitializer<IBeanSelectionDialogBluePrint<?>>() {
					@Override
					public void initialize(final IBeanSelectionDialogBluePrint<?> bluePrint) {
						bluePrint.setContentScrolled(false);
						bluePrint.setContentBorder();
						bluePrint.setMissingInputHint("Bitte wählen Sie einen Datensatz aus");
						bluePrint.setDefaultButtonPolicy(InputDialogDefaultButtonPolicy.DISABLED);
						final IInputComponentValidationLabelBluePrint validationLabelBp = BPF.inputComponentValidationLabel();
						validationLabelBp.setInitialValidationDecorator(new IDecorator<IValidationResult>() {
							@Override
							public IValidationResult decorate(final IValidationResult original) {
								if (!original.isValid()) {
									return original;
								}
								return null;
							}
						});
					}
				});

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanTableFormSetupBuilder.class,
				new IDefaultInitializer<IBeanTableFormSetupBuilder<?>>() {
					@Override
					public void initialize(final IBeanTableFormSetupBuilder<?> bluePrint) {
						bluePrint.setHideReadonlyAttributes(true);
						bluePrint.setHideMetaAttributes(true);
					}
				});

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanDialogBluePrint.class,
				new IDefaultInitializer<IBeanDialogBluePrint<?>>() {
					@Override
					public void initialize(final IBeanDialogBluePrint<?> bluePrint) {
						bluePrint.setOkButton(BPF.buttonOk());
						bluePrint.setCancelButton(BPF.buttonCancel());
					}
				});

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanFormBluePrint.class,
				new IDefaultInitializer<IBeanFormBluePrint<?>>() {

					@Override
					public void initialize(final IBeanFormBluePrint<?> bluePrint) {
						bluePrint.setAutoResetValidation(true);
						bluePrint.setContentScrolled(true);
						bluePrint.setCreateModeForegroundColor(Colors.GREEN);
						final IInputComponentValidationLabelBluePrint editModeValidationLabel = Toolkit.getBluePrintFactory().inputComponentValidationLabel();
						final IDecorator<IValidationResult> editModeValidationDecorator = new IDecorator<IValidationResult>() {
							@Override
							public IValidationResult decorate(final IValidationResult original) {
								return null;
							}
						};
						editModeValidationLabel.setInitialValidationDecorator(editModeValidationDecorator);
						editModeValidationLabel.setUnmodifiedValidationDecorator(editModeValidationDecorator);
						bluePrint.setEditModeValidationLabel(editModeValidationLabel);

						final IInputComponentValidationLabelBluePrint createModeValidationLabel = Toolkit.getBluePrintFactory().inputComponentValidationLabel();
						final IDecorator<IValidationResult> createModeValidationDecorator = new IDecorator<IValidationResult>() {
							@Override
							public IValidationResult decorate(final IValidationResult original) {
								if (!original.isValid()) {
									return original;
								}
								else {
									return null;
								}
							}
						};
						createModeValidationLabel.setInitialValidationDecorator(createModeValidationDecorator);
						createModeValidationLabel.setUnmodifiedValidationDecorator(createModeValidationDecorator);
						bluePrint.setCreateModeValidationLabel(createModeValidationLabel);
						bluePrint.setCreateModeInputHint(Messages.getString("CapUiToolkit.fill_out_mandatory_fields"));

						bluePrint.setMandatoryBackgroundColor(CapColors.MANDATORY_BACKGROUND);
						bluePrint.setMandatoryLabelDecorator(new IDecorator<String>() {
							@Override
							public String decorate(final String original) {
								if (original != null) {
									return original + "*";
								}
								return null;
							}
						});
						bluePrint.setMandatoryValidator(new MandatoryValidator<Object>());
					}
				});

		toolkit.getBluePrintFactory().setSetupBuilderConvenience(
				IBeanTabFolderBluePrint.class,
				new BeanTabFolderSetupConvenience());

		toolkit.getBluePrintFactory().setSetupBuilderConvenience(IBeanFormBluePrint.class, new BeanFormSetupConvenienve());

	}

	private static class BeanTabFolderSetupConvenience extends AbstractSetupBuilderConvenience<IBeanTabFolderBluePrint<Object>> implements
			IBeanTabFolderSetupConvenience<Object, IBeanTabFolderBluePrint<Object>> {

		@Override
		public IBeanTabFolderBluePrint<Object> setTabFactory(final IBeanFormBluePrint<Object> beanFormBp) {
			getBuilder().setTabFactory(new IBeanTabFactory<Object>() {
				@Override
				public IBeanTab<Object> createTab(final ITabItem tab) {

					return new IBeanTab<Object>() {

						private IBeanForm<Object> beanForm;

						private IBeanProxy<Object> bean;

						{
							tab.addTabItemListener(new TabItemAdapter() {

								@Override
								public void selectionChanged(final boolean selected) {
									if (selected && beanForm == null) {
										initialize();
									}
								}

							});

							if (tab.getParent().getItems().size() == 1 || tab.getParent().getSelectedItem() == tab) {
								initialize();
							}
						}

						@Override
						public void setBean(final IBeanProxy<Object> newBean) {
							bean = newBean;
							if (beanForm != null) {
								beanForm.setValue(bean);
							}
						}

						private void initialize() {
							tab.layoutBegin();
							final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
							final IBeanFormBluePrint<Object> bluePrintCopy = cbpf.beanForm(beanFormBp.getEntityId());
							bluePrintCopy.setSetup(beanFormBp);
							tab.setLayout(MigLayoutFactory.growingCellLayout());
							beanForm = tab.add(bluePrintCopy, MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
							beanForm.setValue(bean);
							tab.layoutEnd();
						}
					};
				}
			});
			return getBuilder();
		}
	}

	private static class BeanFormSetupConvenienve extends AbstractSetupBuilderConvenience<IBeanFormBluePrint<Object>> implements
			IBeanFormSetupConvenience<Object, IBeanFormBluePrint<Object>> {

		@Override
		public IBeanFormBluePrint<Object> setLayouter(final IBeanFormLayouter layouter) {
			final IBeanFormBluePrint<Object> builder = getBuilder();
			builder.setEditModeLayouter(layouter);
			builder.setCreateModeLayouter(layouter);
			return builder;
		}

		@Override
		public IBeanFormBluePrint<Object> setAttributes(final Collection<? extends IAttribute<?>> attributes) {
			final IBeanFormBluePrint<Object> builder = getBuilder();
			builder.setEditModeAttributes(attributes);
			builder.setCreateModeAttributes(attributes);
			return builder;
		}

	}
}
