/*
 * Copyright (c) 2013, Michael
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

package org.jowidgets.cap.tools.starter.hibernate.validation.plugin;

import java.util.LinkedList;
import java.util.List;

import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.jowidgets.beanvalidation.bootstrap.api.BeanValidatorFactory;
import org.jowidgets.beanvalidation.bootstrap.api.IBeanValidatorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class HibernateValidationPluginActivator implements BundleActivator {

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		BeanValidatorFactory.setFactory(new IBeanValidatorFactory() {
			@Override
			public Validator create() {
				final ValidationProviderResolver validationProviderResolver = new ValidationProviderResolver() {
					@Override
					public List<ValidationProvider<?>> getValidationProviders() {
						final List<ValidationProvider<?>> result = new LinkedList<ValidationProvider<?>>();
						result.add(new HibernateValidator());
						return result;
					}
				};
				final ProviderSpecificBootstrap<HibernateValidatorConfiguration> bootstrap = Validation.byProvider(HibernateValidator.class);
				bootstrap.providerResolver(validationProviderResolver);
				final HibernateValidatorConfiguration validatorConfiguration = bootstrap.configure();
				return validatorConfiguration.buildValidatorFactory().getValidator();
			}
		});
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {

	}

}
