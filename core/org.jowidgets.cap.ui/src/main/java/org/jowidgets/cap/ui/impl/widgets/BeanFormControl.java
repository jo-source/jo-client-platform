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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.api.animation.IWaitAnimationProcessor;
import org.jowidgets.api.color.Colors;
import org.jowidgets.api.command.IAction;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IComposite;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IInputComponentValidationLabel;
import org.jowidgets.api.widgets.IInputControl;
import org.jowidgets.api.widgets.ILabel;
import org.jowidgets.api.widgets.ITextLabel;
import org.jowidgets.api.widgets.IValidationResultLabel;
import org.jowidgets.api.widgets.blueprint.IInputComponentValidationLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.ILabelBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.IValidationResultLabelBluePrint;
import org.jowidgets.api.widgets.descriptor.setup.IInputComponentValidationLabelSetup;
import org.jowidgets.api.widgets.descriptor.setup.IValidationLabelSetup;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanMessage;
import org.jowidgets.cap.ui.api.bean.IBeanMessageStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProcessStateListener;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyListener;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidator;
import org.jowidgets.cap.ui.api.bean.IExternalBeanValidatorListener;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskListener;
import org.jowidgets.cap.ui.api.form.IBeanFormControlFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormInfo;
import org.jowidgets.cap.ui.api.form.IBeanFormLayout;
import org.jowidgets.cap.ui.api.form.IBeanFormLayouter;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.plugin.IAttributePlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.cap.ui.tools.bean.BeanProxyListenerAdapter;
import org.jowidgets.cap.ui.tools.execution.ExecutionTaskAdapter;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.widgets.controller.IInputListener;
import org.jowidgets.common.widgets.factory.ICustomWidgetCreator;
import org.jowidgets.common.widgets.factory.ICustomWidgetFactory;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.plugin.api.PluginToolkit;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.tools.widgets.wrapper.AbstractInputControl;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.EmptyCompatibleEquivalence;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.NullCompatibleEquivalence;
import org.jowidgets.util.event.IChangeListener;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.MessageType;
import org.jowidgets.validation.ValidationResult;

final class BeanFormControl<BEAN_TYPE> extends AbstractInputControl<IBeanProxy<BEAN_TYPE>> implements
		IBeanForm<BEAN_TYPE>,
		IExternalBeanValidator {

	private static final IMessage PROCESSING_DATA = Messages.getMessage("BeanFormControl.processing_data");

	private final IDecorator<String> mandatoryLabelDecorator;
	private final IColorConstant mandatoryBackgroundColor;
	private final IColorConstant foregroundColor;
	private final IColorConstant modifiedForegroundColor;
	private final IValidator<Object> mandatoryValidator;
	private final String inputHint;
	private final Map<String, IAttribute<Object>> attributes;

	private final IValidationLabelSetup propertyValidationLabelSetup;
	private final IInputComponentValidationLabelSetup mainValidationLabelSetup;
	private final IBeanFormInfo beanFormInfo;
	private final boolean scrollbarsAllowed;
	private final Integer maxWidthDefault;

	private final Map<String, IInputControl<Object>> controls;
	private final Map<String, IInputListener> bindingListeners;
	private final Map<String, IValidationConditionListener> validationListeners;
	private final Map<String, IValidationResultLabel> validationLabels;
	private final Map<String, IValidationResult> validationResults;
	private final PropertyChangeListener propertyChangeListenerBinding;
	private final IBeanProcessStateListener<BEAN_TYPE> beanProcessStateListener;
	private final IBeanMessageStateListener<BEAN_TYPE> beanMessageStateListener;
	private final IBeanProxyListener<BEAN_TYPE> beanProxyListener;
	private final IExecutionTaskListener executionTaskListener;
	private final LabelWaitChangeListener labelWaitChangeListener;
	private final Set<IExternalBeanValidatorListener> externalValidatorListeners;

	private final IAction saveAction;
	private final IAction undoAction;
	private final Collection<IProvider<IAction>> customActions;

	private IComposite validationLabelContainer;
	private IInputComponentValidationLabel mainValidationLabel;
	private ILabel processStateLabel;

	private IBeanProxy<BEAN_TYPE> bean;
	private boolean editable;

	@SuppressWarnings("unchecked")
	BeanFormControl(
		final IComposite composite,
		final Object entityId,
		final Class<BEAN_TYPE> beanType,
		Collection<IAttribute<?>> attributes,
		IBeanFormLayouter layouter,
		final boolean scrollbarsAllowed,
		final Integer maxWidth,
		final IDecorator<String> manadtoryLabelDecorator,
		final IColorConstant mandatoryBackgroundColor,
		final IColorConstant foregroundColor,
		final IColorConstant modifiedForegroundColor,
		final IValidator<Object> manadtoryValidator,
		final String inputHint,
		final IInputComponentValidationLabelSetup mainValidationLabelSetup,
		final IValidationLabelSetup propertyValidationLabelSetup,
		final IBeanFormInfo beanFormInfo,
		final IProvider<IAction> undoAction,
		final IProvider<IAction> saveAction,
		final Collection<? extends IProvider<IAction>> customActions,
		final boolean createMode) {

		super(composite);

		attributes = createModifiedByPluginsAttributes(entityId, beanType, attributes);

		this.scrollbarsAllowed = scrollbarsAllowed;
		this.maxWidthDefault = maxWidth;
		this.mandatoryLabelDecorator = manadtoryLabelDecorator;
		this.mandatoryBackgroundColor = mandatoryBackgroundColor;
		this.foregroundColor = foregroundColor;
		this.modifiedForegroundColor = modifiedForegroundColor;
		this.mandatoryValidator = manadtoryValidator;
		this.inputHint = inputHint;
		this.propertyValidationLabelSetup = propertyValidationLabelSetup;
		this.mainValidationLabelSetup = mainValidationLabelSetup;
		this.beanFormInfo = beanFormInfo;

		this.saveAction = saveAction != null ? saveAction.get() : null;
		this.undoAction = undoAction != null ? undoAction.get() : null;
		this.customActions = new LinkedList<IProvider<IAction>>();
		if (customActions != null) {
			this.customActions.addAll(customActions);
		}

		this.editable = true;
		this.attributes = new HashMap<String, IAttribute<Object>>();
		this.controls = new LinkedHashMap<String, IInputControl<Object>>();
		this.bindingListeners = new HashMap<String, IInputListener>();
		this.validationListeners = new HashMap<String, IValidationConditionListener>();
		this.validationLabels = new HashMap<String, IValidationResultLabel>();
		this.validationResults = new LinkedHashMap<String, IValidationResult>();
		this.propertyChangeListenerBinding = new BeanPropertyChangeListenerBinding();
		this.beanProcessStateListener = new BeanProcessStateListener();
		this.beanMessageStateListener = new BeanMessageStateListener();
		this.beanProxyListener = new BeanProxyListener();
		this.executionTaskListener = new ExecutionTaskListener();
		this.labelWaitChangeListener = new LabelWaitChangeListener();
		this.externalValidatorListeners = new LinkedHashSet<IExternalBeanValidatorListener>();

		for (final IAttribute<?> attribute : attributes) {
			this.attributes.put(attribute.getPropertyName(), (IAttribute<Object>) attribute);
		}

		addDisposeListener(new IDisposeListener() {
			@Override
			public void onDispose() {
				Toolkit.getWaitAnimationProcessor().removeChangeListener(labelWaitChangeListener);
			}
		});

		if (layouter == null) {
			final Collection<IAttribute<?>> layoutAttributes = getDefaultLayoutAttributes(createMode, attributes);
			final IBeanFormToolkit beanFormToolkit = CapUiToolkit.beanFormToolkit();
			final IBeanFormLayout layout = beanFormToolkit.layoutBuilder().addGroups(layoutAttributes).build();
			layouter = beanFormToolkit.layouter(layout);
		}

		//this must be the last invocation in this constructor
		layouter.layout(composite, new BeanFormControlFactory());
	}

	private static Collection<IAttribute<?>> getDefaultLayoutAttributes(
		final boolean createMode,
		final Collection<IAttribute<?>> attributes) {

		final List<IAttribute<?>> result = new LinkedList<IAttribute<?>>();
		for (final IAttribute<?> attribute : attributes) {
			if ((attribute.isEditable() || !createMode) && attribute.isVisible()) {
				result.add(attribute);
			}
		}
		return result;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private List<IAttribute<?>> createModifiedByPluginsAttributes(
		final Object entityId,
		final Class<BEAN_TYPE> beanType,
		final Collection<IAttribute<?>> attributes) {

		List result = new LinkedList(attributes);

		final IPluginPropertiesBuilder propBuilder = PluginToolkit.pluginPropertiesBuilder();
		propBuilder.add(IAttributePlugin.ENTITIY_ID_PROPERTY_KEY, entityId);
		propBuilder.add(IAttributePlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		final IPluginProperties properties = propBuilder.build();
		for (final IAttributePlugin plugin : PluginProvider.getPlugins(IAttributePlugin.ID, properties)) {
			result = plugin.modifyAttributes(properties, result);
		}

		return result;
	}

	@Override
	protected IComposite getWidget() {
		return (IComposite) super.getWidget();
	}

	@Override
	public void dispose() {
		if (this.bean != null) {
			this.bean.removePropertyChangeListener(propertyChangeListenerBinding);
			this.bean.removeProcessStateListener(beanProcessStateListener);
			this.bean.removeMessageStateListener(beanMessageStateListener);
			this.bean.removeBeanProxyListener(beanProxyListener);
			this.bean.unregisterExternalValidator(this);
			unregisterExecutionTaskListener(this.bean);
			Toolkit.getWaitAnimationProcessor().removeChangeListener(labelWaitChangeListener);
		}
		super.dispose();
	}

	@Override
	public void setValue(final IBeanProxy<BEAN_TYPE> bean) {
		if (this.bean != null) {
			this.bean.removePropertyChangeListener(propertyChangeListenerBinding);
			this.bean.removeProcessStateListener(beanProcessStateListener);
			this.bean.removeMessageStateListener(beanMessageStateListener);
			this.bean.removeBeanProxyListener(beanProxyListener);
			this.bean.unregisterExternalValidator(this);
			unregisterExecutionTaskListener(this.bean);
		}

		this.bean = bean;
		updateExecutionTaskText(this.bean);
		registerExecutionTaskListener(this.bean);
		setValidationLabelsVisibility(this.bean);
		if (bean == null || bean.isDummy()) {
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				final IInputControl<Object> control = entry.getValue();
				final String propertyName = entry.getKey();
				control.setValue(null);
				control.setEnabled(false);
				setBackgroundColor(control, propertyName);
				setForeground(bean, propertyName, control);
			}
		}
		else {
			for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
				final String propertyName = entry.getKey();
				final IInputControl<Object> control = entry.getValue();
				final IAttribute<?> attribute = attributes.get(propertyName);
				control.removeInputListener(bindingListeners.get(propertyName));
				control.removeValidationConditionListener(validationListeners.get(propertyName));
				control.setValue(bean.getValue(entry.getKey()));
				control.setEnabled(!bean.hasExecution());
				control.setEditable(attribute.isEditable() && !bean.hasExecution());
				setBackgroundColor(control, propertyName);
				setForeground(bean, propertyName, control);
				control.addInputListener(bindingListeners.get(propertyName));
				control.addValidationConditionListener(validationListeners.get(propertyName));
			}
			bean.addPropertyChangeListener(propertyChangeListenerBinding);
			bean.addProcessStateListener(beanProcessStateListener);
			bean.addMessageStateListener(beanMessageStateListener);
			bean.addBeanProxyListener(beanProxyListener);
			bean.registerExternalValidator(this);
		}
		validateAllProperties();
	}

	private void setBackgroundColor(final IInputControl<Object> control, final String propertyName) {
		if (mandatoryBackgroundColor != null) {
			final IAttribute<?> attribute = attributes.get(propertyName);
			if (attribute.isMandatory()) {
				if (bean == null || bean.isDummy() || !control.isEditable()) {
					control.setBackgroundColor(null);
				}
				else {
					control.setBackgroundColor(mandatoryBackgroundColor);
				}
			}
		}
	}

	private void setForeground(final IBeanProxy<BEAN_TYPE> bean, final String propertyName, final IInputControl<Object> control) {
		if (control != null) {
			if (bean == null || bean.isDummy()) {
				control.setForegroundColor(null);
			}
			else if (bean.hasErrors()) {
				control.setForegroundColor(Colors.ERROR);
			}
			else if (bean.hasWarnings()) {
				control.setForegroundColor(Colors.WARNING);
			}
			else if (modifiedForegroundColor != null && !bean.isTransient() && bean.isModified(propertyName)) {
				control.setForegroundColor(modifiedForegroundColor);
			}
			else if (foregroundColor != null) {
				control.setForegroundColor(foregroundColor);
			}
			else {
				control.setForegroundColor(null);
			}
		}
	}

	private void setValidationLabelsVisibility(final IBeanProxy<BEAN_TYPE> bean) {
		if (bean != null && bean.hasExecution()) {
			setValidationLabelsVisible(true);
		}
		else {
			setValidationLabelsVisible(false);
		}
	}

	private void setValidationLabelsVisible(final boolean processMode) {
		if (validationLabelContainer != null) {
			validationLabelContainer.layoutBegin();
			mainValidationLabel.setVisible(!processMode);
			processStateLabel.setVisible(processMode);
			validationLabelContainer.layoutEnd();
		}
	}

	private void registerExecutionTaskListener(final IBeanProxy<BEAN_TYPE> bean) {
		if (bean != null) {
			final IExecutionTask executionTask = this.bean.getExecutionTask();
			if (executionTask != null) {
				executionTask.addExecutionTaskListener(executionTaskListener);
				if (processStateLabel != null) {
					Toolkit.getWaitAnimationProcessor().addChangeListener(labelWaitChangeListener);
				}
			}
		}
	}

	private void unregisterExecutionTaskListener(final IBeanProxy<BEAN_TYPE> bean) {
		if (bean != null) {
			final IExecutionTask executionTask = this.bean.getExecutionTask();
			if (executionTask != null) {
				executionTask.removeExecutionTaskListener(executionTaskListener);
			}
		}
		Toolkit.getWaitAnimationProcessor().removeChangeListener(labelWaitChangeListener);
	}

	private void updateExecutionTaskText(final IBeanProxy<BEAN_TYPE> bean) {
		if (processStateLabel != null) {
			if (bean != null) {
				final IExecutionTask executionTask = bean.getExecutionTask();
				if (executionTask != null) {
					processStateLabel.setText(computeLabelText(executionTask));
				}
				else {
					processStateLabel.setText("");
				}
			}
			else {
				processStateLabel.setText("");
			}
		}
	}

	private String computeLabelText(final IExecutionTask executionTask) {
		final StringBuilder result = new StringBuilder();
		final String description = executionTask.getDescription();
		if (!EmptyCheck.isEmpty(description)) {
			result.append(description);
		}
		else {
			result.append(PROCESSING_DATA.get());
		}
		result.append(" ");
		result.append(computeProgressText(executionTask));
		return result.toString();
	}

	private String computeProgressText(final IExecutionTask executionTask) {
		final Integer totalStepCount = executionTask.getTotalStepCount();
		if (totalStepCount != null && totalStepCount.intValue() > 0) {
			final double progr = ((double) executionTask.getWorked() / totalStepCount.intValue()) * 100;
			Math.floor(progr);
			return "" + Math.floor(progr) + " %";
		}
		else {
			return "...";
		}
	}

	private void validateAllProperties() {
		resetValidation();
		resetModificationState();
		if (bean != null && !bean.isDummy()) {
			final Set<String> properties = attributes.keySet();
			for (final IExternalBeanValidatorListener listener : externalValidatorListeners) {
				listener.validationConditionsChanged(this, properties);
			}
			setValidationCacheDirty();
		}
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getValue() {
		return bean;
	}

	@Override
	public void setEditable(final boolean editable) {
		this.editable = editable;
		for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
			final String propertyName = entry.getKey();
			final IInputControl<Object> control = entry.getValue();
			control.setEditable(editable);
			setBackgroundColor(control, propertyName);
		}
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public boolean hasModifications() {
		if (bean != null && bean.hasModifications() && !bean.isDummy()) {
			return true;
		}
		for (final IInputControl<Object> control : controls.values()) {
			if (control.hasModifications()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resetModificationState() {
		for (final IInputControl<Object> control : controls.values()) {
			control.resetModificationState();
		}
	}

	@Override
	public void resetValidation() {
		if (mainValidationLabel != null) {
			mainValidationLabel.resetValidation();
		}
		for (final IValidationResultLabel validationLabel : validationLabels.values()) {
			validationLabel.setEmpty();
		}
		validationResults.clear();
	}

	void invokeSetValidationCacheDirty() {
		setValidationCacheDirty();
	}

	@Override
	protected IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();
		if (bean != null) {
			final IBeanMessage worstMessage = bean.getFirstWorstMessage();
			if (worstMessage != null) {
				if (BeanMessageType.ERROR.equals(worstMessage.getType())) {
					builder.addError(worstMessage.getLabel());
				}
				else if (BeanMessageType.WARNING.equals(worstMessage.getType())) {
					builder.addWarning(worstMessage.getLabel());
				}
			}
		}
		for (final IValidationResult validationResult : validationResults.values()) {
			builder.addResult(validationResult);
		}
		return builder.build();
	}

	@Override
	public List<IBeanValidationResult> validate(final Collection<IBeanValidationResult> parentResults) {
		final List<IBeanValidationResult> result = new LinkedList<IBeanValidationResult>();
		boolean validationChanged = false;
		for (final IBeanValidationResult parentResult : parentResults) {
			final IBeanValidationResult subResult = validate(parentResult);
			if (subResult != null) {
				result.add(subResult);
				validationChanged = true;
			}
			else {
				result.add(parentResult);
			}
		}
		if (validationChanged) {
			setValidationCacheDirty();
		}
		return result;
	}

	private IBeanValidationResult validate(final IBeanValidationResult parentResult) {
		final String propertyName = parentResult.getPropertyName();
		final IValidationResult parentValidationResult = parentResult.getValidationResult();
		boolean validationChanged = false;
		IValidationResult validationResult = ValidationResult.ok();
		final IInputControl<?> control = controls.get(propertyName);
		final String propertyLabel = getLabel(propertyName);
		if (bean != null && control != null && EmptyCompatibleEquivalence.equals(control.getValue(), bean.getValue(propertyName))) {
			validationResult = control.validate();
			//use the parent result if the control result is valid and the parent result is not valid
			if (validationResult.isValid() && !parentValidationResult.isValid()) {
				validationResult = parentValidationResult;
			}

			//change the validation map, if the worst first changed
			final IValidationResult lastResult = validationResults.get(propertyName);
			boolean hasMessage = false;
			boolean hasHint = false;
			if (lastResult == null || !validationResult.getWorstFirst().equals(lastResult.getWorstFirst())) {
				if (!validationResult.isValid()
					&& !control.hasModifications()
					&& !bean.isModified(propertyName)
					&& inputHint != null) {
					validationResults.put(propertyName, ValidationResult.infoError(inputHint));
					hasHint = true;
				}
				else {
					validationResults.put(propertyName, validationResult.withContext(propertyLabel));
					hasMessage = validationResult.getWorstFirst().getType() != MessageType.OK;
				}
				validationChanged = true;
			}

			//update the validation label
			final IValidationResultLabel validationLabel = validationLabels.get(propertyName);
			if (validationLabel != null) {
				if (hasMessage || control.hasModifications() || bean.isModified(propertyName)) {
					if (!hasHint) {
						validationLabel.setResult(validationResult);
					}
				}
				else {
					validationLabel.setEmpty();
				}
			}
		}
		else if (bean != null && control == null) {
			//change the validation map, if the worst first changed
			validationResult = parentValidationResult;
			final IValidationResult lastResult = validationResults.get(propertyName);
			if (lastResult == null || !validationResult.getWorstFirst().equals(lastResult.getWorstFirst())) {
				if (propertyLabel != null) {
					validationResults.put(propertyName, validationResult.withContext(propertyLabel));
				}
				else {
					validationResults.put(propertyName, validationResult);
				}
				validationChanged = true;
			}
		}
		if (validationChanged) {
			final IValidationResult newValidationResult = validationResult;
			return new IBeanValidationResult() {

				@Override
				public IValidationResult getValidationResult() {
					return newValidationResult.withContext(propertyLabel);
				}

				@Override
				public String getPropertyName() {
					return propertyName;
				}
			};
		}
		else {
			return null;
		}
	}

	@Override
	public Collection<String> getObservedProperties() {
		return new LinkedList<String>(controls.keySet());
	}

	@Override
	public void addExternalValidatorListener(final IExternalBeanValidatorListener listener) {
		Assert.paramNotNull(listener, "listener");
		externalValidatorListeners.add(listener);
	}

	@Override
	public void removeExternalValidatorListener(final IExternalBeanValidatorListener listener) {
		Assert.paramNotNull(listener, "listener");
		externalValidatorListeners.remove(listener);
	}

	private String getLabel(final String propertyName) {
		Assert.paramNotNull(propertyName, "propertyName");
		final IAttribute<Object> attribute = attributes.get(propertyName);
		if (attribute != null) {
			return attribute.getCurrentLabel();
		}
		else {
			return null;
		}
	}

	private final class BeanFormControlFactory implements IBeanFormControlFactory {

		@Override
		public ICustomWidgetCreator<? extends IControl> createMainValidationLabel() {
			if (mainValidationLabelSetup != null) {
				return new ICustomWidgetCreator<IControl>() {
					@Override
					public IControl create(final ICustomWidgetFactory widgetFactory) {
						validationLabelContainer = widgetFactory.create(BPF.composite());
						validationLabelContainer.setLayout(new MigLayoutDescriptor("hidemode 3", "0[grow, 0::]0", "0[0::]0"));
						mainValidationLabel = validationLabelContainer.add(getMainValidationLabelBp(), "growx, w 30::");
						//mainValidationLabel.setMinSize(new Dimension(20, 20));

						final ILabelBluePrint processStateLabelBp = BPF.label();
						processStateLabelBp.setMarkup(Markup.EMPHASIZED);
						processStateLabelBp.setColor(Colors.STRONG);
						processStateLabelBp.setIcon(IconsSmall.WAIT_1);
						processStateLabel = validationLabelContainer.add(processStateLabelBp, "growx, w 30::");
						//processStateLabel.setMinSize(new Dimension(20, 20));
						processStateLabel.setVisible(false);

						return validationLabelContainer;
					}
				};
			}
			else {
				return null;
			}
		}

		@Override
		public ICustomWidgetCreator<ITextLabel> createLabel(final String propertyName, final AlignmentHorizontal alignment) {
			return new ICustomWidgetCreator<ITextLabel>() {
				@Override
				public ITextLabel create(final ICustomWidgetFactory widgetFactory) {
					final ITextLabelBluePrint textLabelBp = BPF.textLabel(getLabel(propertyName));
					setAlignmentHorizontal(alignment, textLabelBp);
					return widgetFactory.create(textLabelBp);
				}
			};
		}

		@Override
		public boolean getScrollbarsAllowed() {
			return scrollbarsAllowed;
		}

		@Override
		public Integer getMaxWidthDefault() {
			return maxWidthDefault;
		}

		@Override
		public IBeanFormInfo getBeanFormInfo() {
			return beanFormInfo;
		}

		@Override
		public ICustomWidgetCreator<? extends IControl> createControl(final String propertyName) {
			Assert.paramNotNull(propertyName, "propertyName");

			final IAttribute<Object> attribute = attributes.get(propertyName);
			if (attribute != null) {
				final ICustomWidgetCreator<IInputControl<Object>> widgetCreator = getWidgetCreator(attribute);
				if (widgetCreator != null) {
					return new ICustomWidgetCreator<IInputControl<Object>>() {
						@Override
						public IInputControl<Object> create(final ICustomWidgetFactory widgetFactory) {
							if (controls.containsKey(propertyName)) {
								throw new IllegalStateException("Control must not be created more than once for the property '"
									+ propertyName
									+ "'.");
							}

							final IInputControl<Object> result = widgetCreator.create(widgetFactory);

							if (mandatoryValidator != null && isMandatory(propertyName)) {
								result.addValidator(mandatoryValidator);
							}
							result.setEnabled(false);
							bindingListeners.put(propertyName, new BindingListener(propertyName, result));
							validationListeners.put(propertyName, new ValidationConditionListener(propertyName));
							controls.put(propertyName, result);
							return result;
						}
					};
				}
			}
			else {
				throw new IllegalStateException("No attribute found for the property '" + propertyName + "'.");
			}
			return null;
		}

		@Override
		public ICustomWidgetCreator<? extends IControl> createPropertyValidationLabel(final String propertyName) {
			Assert.paramNotNull(propertyName, "propertyName");
			return new ICustomWidgetCreator<IControl>() {
				@Override
				public IControl create(final ICustomWidgetFactory widgetFactory) {
					if (validationLabels.containsKey(propertyName)) {
						throw new IllegalStateException("Validation label must not be created more than once for the property '"
							+ propertyName
							+ "'.");
					}
					final IValidationResultLabelBluePrint validationLabelBp = BPF.validationResultLabel();
					validationLabelBp.setSetup(propertyValidationLabelSetup);
					final IValidationResultLabel validationLabel = widgetFactory.create(validationLabelBp);
					validationLabels.put(propertyName, validationLabel);
					return validationLabel;
				}
			};
		}

		@Override
		public boolean hasPropertyValidationLabels() {
			return propertyValidationLabelSetup != null;
		}

		@Override
		public Collection<ICustomWidgetCreator<? extends IControl>> createButtons() {
			final List<ICustomWidgetCreator<? extends IControl>> result = new LinkedList<ICustomWidgetCreator<? extends IControl>>();
			ICustomWidgetCreator<? extends IControl> button;
			for (final IProvider<IAction> action : customActions) {
				button = createButton(action.get());
				if (button != null) {
					result.add(button);
				}
			}
			button = createButton(saveAction);
			if (button != null) {
				result.add(button);
			}
			button = createButton(undoAction);
			if (button != null) {
				result.add(button);
			}
			return result;
		}

		private IInputComponentValidationLabelBluePrint getMainValidationLabelBp() {
			final IInputComponentValidationLabelBluePrint validationLabelBp = BPF.inputComponentValidationLabel();
			validationLabelBp.setSetup(mainValidationLabelSetup);
			validationLabelBp.setInputComponent(BeanFormControl.this);
			return validationLabelBp;
		}

		private ICustomWidgetCreator<? extends IControl> createButton(final IAction action) {
			if (action != null) {
				return new ICustomWidgetCreator<IControl>() {
					@Override
					public IControl create(final ICustomWidgetFactory widgetFactory) {
						final IButton result = widgetFactory.create(BPF.button());
						result.setAction(action);
						return result;
					}
				};
			}
			return null;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		private ICustomWidgetCreator<IInputControl<Object>> getWidgetCreator(final IAttribute<Object> attribute) {
			final IControlPanelProvider<Object> controlPanel = attribute.getCurrentControlPanel();

			final ICustomWidgetCreator controlCreator;
			if (attribute.isCollectionType()) {
				controlCreator = controlPanel.getCollectionControlCreator();
			}
			else {
				controlCreator = controlPanel.getControlCreator();
			}
			if (controlCreator == null && controlPanel.getObjectLabelConverter() != null) {
				return new ICustomWidgetCreator<IInputControl<Object>>() {
					@Override
					public IInputControl<Object> create(final ICustomWidgetFactory widgetFactory) {
						return widgetFactory.create(BPF.inputField(controlPanel.getObjectLabelConverter()));
					}
				};
			}

			return controlCreator;
		}

		private boolean isMandatory(final String propertyName) {
			final IAttribute<Object> attribute = attributes.get(propertyName);
			if (attribute != null) {
				return attribute.isMandatory();
			}
			else {
				return false;
			}
		}

		private String getLabel(final String propertyName) {
			final String result = BeanFormControl.this.getLabel(propertyName);
			if (mandatoryLabelDecorator != null && isMandatory(propertyName)) {
				return mandatoryLabelDecorator.decorate(result);
			}
			else {
				return result;
			}
		}

		private void setAlignmentHorizontal(final AlignmentHorizontal alignment, final ITextLabelBluePrint textLabelBp) {
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

	}

	private final class ValidationConditionListener implements IValidationConditionListener {

		private final Collection<String> properties;

		private ValidationConditionListener(final String propertyName) {
			this.properties = Collections.singletonList(propertyName);
		}

		@Override
		public void validationConditionsChanged() {
			for (final IExternalBeanValidatorListener listener : externalValidatorListeners) {
				listener.validationConditionsChanged(BeanFormControl.this, properties);
			}
		}
	}

	private final class BeanPropertyChangeListenerBinding implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			final String propertyName = evt.getPropertyName();
			final IInputControl<Object> control = controls.get(propertyName);
			if (bean != null) {
				if (control != null) {
					control.removeInputListener(bindingListeners.get(propertyName));
					bean.removePropertyChangeListener(propertyChangeListenerBinding);
					if (!NullCompatibleEquivalence.equals(control.getValue(), evt.getNewValue())) {
						control.setValue(evt.getNewValue());
					}
					bean.addPropertyChangeListener(propertyChangeListenerBinding);
					control.addInputListener(bindingListeners.get(propertyName));
				}
			}
			setForeground(bean, propertyName, control);
		}
	}

	private final class BindingListener implements IInputListener {

		private final String propertyName;
		private final IInputControl<Object> control;

		private BindingListener(final String propertyName, final IInputControl<Object> control) {
			this.propertyName = propertyName;
			this.control = control;
		}

		@Override
		public void inputChanged() {
			if (bean != null) {
				control.removeInputListener(bindingListeners.get(propertyName));
				bean.removePropertyChangeListener(propertyChangeListenerBinding);
				bean.setValue(propertyName, control.getValue());
				bean.addPropertyChangeListener(propertyChangeListenerBinding);
				control.addInputListener(bindingListeners.get(propertyName));
			}
			setForeground(bean, propertyName, control);
			fireInputChanged();
		}
	}

	private final class BeanProcessStateListener implements IBeanProcessStateListener<BEAN_TYPE> {
		@Override
		public void processStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
			if (BeanFormControl.this.bean != null && BeanFormControl.this.bean == bean) {
				for (final Entry<String, IInputControl<Object>> entry : controls.entrySet()) {
					final String propertyName = entry.getKey();
					final IInputControl<Object> control = entry.getValue();
					final IAttribute<?> attribute = attributes.get(propertyName);
					control.setEnabled(!bean.hasExecution());
					control.setEditable(attribute.isEditable() && !bean.hasExecution());
					setBackgroundColor(control, propertyName);
					setForeground(bean, propertyName, control);
				}
				resetValidation();
				setValidationLabelsVisibility(BeanFormControl.this.bean);
				updateExecutionTaskText(BeanFormControl.this.bean);
				final IExecutionTask executionTask = bean.getExecutionTask();
				if (executionTask != null) {
					registerExecutionTaskListener(BeanFormControl.this.bean);
				}
				else {
					unregisterExecutionTaskListener(BeanFormControl.this.bean);
				}
				setValidationCacheDirty();
			}

		}
	}

	private final class BeanMessageStateListener implements IBeanMessageStateListener<BEAN_TYPE> {
		@Override
		public void messageStateChanged(final IBeanProxy<BEAN_TYPE> bean) {
			setValidationCacheDirty();
		}
	}

	private final class LabelWaitChangeListener implements IChangeListener {
		@Override
		public void changed() {
			final IWaitAnimationProcessor animationProcessor = Toolkit.getWaitAnimationProcessor();
			if (!isDisposed() && !processStateLabel.isDisposed()) {
				processStateLabel.setIcon(animationProcessor.getWaitIcon());
			}
			else {
				Toolkit.getWaitAnimationProcessor().removeChangeListener(labelWaitChangeListener);
			}
		}
	}

	private final class ExecutionTaskListener extends ExecutionTaskAdapter {

		private final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();

		@Override
		public void worked(final int totalWorked) {
			updateExecutionTaskLater();
		}

		@Override
		public void totalStepCountChanged(final int totalStepCount) {
			updateExecutionTaskLater();
		}

		@Override
		public void finished() {
			updateExecutionTaskLater();
		}

		@Override
		public void descriptionChanged(final String decription) {
			updateExecutionTaskLater();
		}

		private void updateExecutionTaskLater() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateExecutionTaskText(bean);
				}
			});
		}
	}

	private final class BeanProxyListener extends BeanProxyListenerAdapter<BEAN_TYPE> {
		@Override
		public void afterBeanUpdated(final IBeanProxy<BEAN_TYPE> bean) {
			validateAllProperties();
		}

		@Override
		public void afterUndoModifications(final IBeanProxy<BEAN_TYPE> bean) {
			validateAllProperties();
		}

	}

}
