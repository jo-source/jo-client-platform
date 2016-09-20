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

package org.jowidgets.cap.service.tools.creator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.ExecutableCheckerComposite;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableCheckerCompositeBuilder;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.annotation.ValidatorAnnotationCache;
import org.jowidgets.cap.common.tools.annotation.ValidatorAnnotationUtil;
import org.jowidgets.cap.common.tools.validation.BeanPropertyToBeanValidatorAdapter;
import org.jowidgets.cap.common.tools.validation.BeanValidatorComposite;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanIdentityResolver;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.plugin.ICreatorServiceBuilderPlugin;
import org.jowidgets.cap.service.tools.bean.DefaultBeanIdentityResolver;
import org.jowidgets.plugin.api.IPluginProperties;
import org.jowidgets.plugin.api.IPluginPropertiesBuilder;
import org.jowidgets.plugin.api.PluginProperties;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidator;

public abstract class AbstractCreatorServiceBuilder<BEAN_TYPE> implements ICreatorServiceBuilder<BEAN_TYPE> {

	private final IBeanIdentityResolver<BEAN_TYPE> beanIdentityResolver;
	private final Class<? extends BEAN_TYPE> beanType;
	private final Object beanTypeId;
	private final List<IExecutableChecker<? extends BEAN_TYPE>> executableCheckers;
	private final List<IBeanValidator<BEAN_TYPE>> beanValidators;
	private final Map<String, List<IValidator<? extends Object>>> propertyValidators;

	private IBeanDtoFactory<BEAN_TYPE> beanDtoFactory;
	private IBeanInitializer<BEAN_TYPE> beanInitializer;
	private boolean confirmValidationWarnings;

	public AbstractCreatorServiceBuilder(final Class<? extends IBean> beanType) {
		this(beanType, beanType);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public AbstractCreatorServiceBuilder(final Class<? extends IBean> beanType, final Object beanTypeId) {
		this(new DefaultBeanIdentityResolver(beanType, beanTypeId));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public AbstractCreatorServiceBuilder(final IBeanIdentityResolver<BEAN_TYPE> beanIdentityResolver) {
		Assert.paramNotNull(beanIdentityResolver, "beanIdentityResolver");
		this.beanIdentityResolver = beanIdentityResolver;
		this.beanType = beanIdentityResolver.getBeanType();
		this.beanTypeId = beanIdentityResolver.getBeanTypeId();
		this.executableCheckers = new LinkedList<IExecutableChecker<? extends BEAN_TYPE>>();
		this.beanValidators = new LinkedList<IBeanValidator<BEAN_TYPE>>();
		this.propertyValidators = new HashMap<String, List<IValidator<? extends Object>>>();
		beanValidators.addAll(ValidatorAnnotationUtil.getBeanValidators(beanType));
		this.confirmValidationWarnings = false;

		final Map validatorsMap = ValidatorAnnotationCache.getPropertyValidators(beanType);
		propertyValidators.putAll(validatorsMap);
	}

	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> addExecutableChecker(
		final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		Assert.paramNotNull(executableChecker, "executableChecker");
		this.executableCheckers.add(executableChecker);
		return this;
	}

	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> setExecutableChecker(
		final IExecutableChecker<? extends BEAN_TYPE> executableChecker) {
		this.executableCheckers.clear();
		if (executableChecker != null) {
			addExecutableChecker(executableChecker);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> addBeanValidator(final IBeanValidator<? extends BEAN_TYPE> validator) {
		Assert.paramNotNull(validator, "validator");
		beanValidators.add((IBeanValidator<BEAN_TYPE>) validator);
		return this;
	}

	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> addPropertyValidator(
		final String propertyName,
		final IValidator<? extends Object> validator) {

		Assert.paramNotNull(propertyName, "propertyName");
		Assert.paramNotNull(validator, "validator");

		List<IValidator<? extends Object>> validators = propertyValidators.get(propertyName);
		if (validators == null) {
			validators = new LinkedList<IValidator<? extends Object>>();
			propertyValidators.put(propertyName, validators);
		}
		validators.add(validator);
		return this;
	}

	@Override
	public ICreatorServiceBuilder<BEAN_TYPE> setConfirmValidationWarnings(final boolean confirmValidationWarnings) {
		this.confirmValidationWarnings = confirmValidationWarnings;
		return this;
	}

	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> setBeanDtoFactory(final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		Assert.paramNotNull(beanDtoFactory, "beanDtoFactory");
		this.beanDtoFactory = beanDtoFactory;
		return this;
	}

	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> setBeanInitializer(final IBeanInitializer<BEAN_TYPE> beanInitializer) {
		Assert.paramNotNull(beanInitializer, "beanInitializer");
		this.beanInitializer = beanInitializer;
		return this;
	}

	@Override
	public final ICreatorServiceBuilder<BEAN_TYPE> setBeanDtoFactoryAndBeanInitializer(final Collection<String> propertyNames) {
		this.beanDtoFactory = CapServiceToolkit.dtoFactory(beanIdentityResolver, propertyNames);
		this.beanInitializer = CapServiceToolkit.beanInitializer(beanType, propertyNames);
		return this;
	}

	protected final Class<? extends BEAN_TYPE> getBeanType() {
		return beanType;
	}

	protected final Object getBeanTypeId() {
		return beanTypeId;
	}

	protected final IBeanDtoFactory<BEAN_TYPE> getBeanDtoFactory() {
		return beanDtoFactory;
	}

	protected final IBeanInitializer<BEAN_TYPE> getBeanInitializer() {
		return beanInitializer;
	}

	protected boolean isConfirmValidationWarnings() {
		return confirmValidationWarnings;
	}

	@SuppressWarnings("unchecked")
	protected final IExecutableChecker<BEAN_TYPE> getExecutableChecker() {
		if (executableCheckers.size() == 1) {
			return (IExecutableChecker<BEAN_TYPE>) executableCheckers.iterator().next();
		}
		else if (executableCheckers.size() > 1) {
			final IExecutableCheckerCompositeBuilder<BEAN_TYPE> builder = ExecutableCheckerComposite.builder();
			builder.set(executableCheckers);
			return builder.build();
		}
		else {
			return null;
		}
	}

	protected final IBeanValidator<BEAN_TYPE> getBeanValidator() {
		final Collection<IBeanValidator<BEAN_TYPE>> validators = getBeanValidators();
		if (validators.size() == 1) {
			return validators.iterator().next();
		}
		else if (validators.size() > 1) {
			return new BeanValidatorComposite<BEAN_TYPE>(validators);
		}
		else {
			return null;
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void applyPlugins() {
		PluginProperties.builder();
		final IPluginProperties properties = createPluginProperties();
		for (final ICreatorServiceBuilderPlugin plugin : PluginProvider.getPlugins(ICreatorServiceBuilderPlugin.ID, properties)) {
			plugin.modify(this);
		}
	}

	private IPluginProperties createPluginProperties() {
		final IPluginPropertiesBuilder builder = PluginProperties.builder();
		builder.add(ICreatorServiceBuilderPlugin.BEAN_TYPE_PROPERTY_KEY, beanType);
		builder.add(ICreatorServiceBuilderPlugin.BEAN_TYPE_ID_PROPERTY_KEY, beanTypeId);
		return builder.build();
	}

	private Collection<IBeanValidator<BEAN_TYPE>> getBeanValidators() {
		final List<IBeanValidator<BEAN_TYPE>> result = new LinkedList<IBeanValidator<BEAN_TYPE>>();
		for (final Entry<String, List<IValidator<? extends Object>>> entry : propertyValidators.entrySet()) {
			beanValidators.add(new BeanPropertyToBeanValidatorAdapter<BEAN_TYPE>(beanType, entry.getKey(), entry.getValue()));
		}
		result.addAll(beanValidators);
		return result;
	}

}
