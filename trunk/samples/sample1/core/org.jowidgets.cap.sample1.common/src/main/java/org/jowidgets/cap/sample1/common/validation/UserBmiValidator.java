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

package org.jowidgets.cap.sample1.common.validation;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

public class UserBmiValidator implements IBeanValidator<IUser> {

	private static final Set<String> PROPERTIES = createPropertyDependencies();

	@Override
	public IValidationResult validate(final IUser user) {

		if (user != null) {
			final Double bmi = calcBmi(user);
			if (bmi != null && bmi.doubleValue() < 10) {
				DecimalFormat.getInstance().format(bmi);
				return ValidationResult.warning("The BMI of '"
					+ DecimalFormat.getInstance().format(bmi)
					+ "' seems to be very low.");
			}
		}
		return ValidationResult.ok();
	}

	Double calcBmi(final IUser user) {
		final Short height = user.getHeight();
		final Double weight = user.getWeight();
		if (height == null || weight == null) {
			return null;
		}
		final double quot = (double) (height * height) / 10000;
		if (quot != 0) {
			return weight / quot;
		}
		return null;
	}

	private static Set<String> createPropertyDependencies() {
		final Set<String> result = new HashSet<String>();
		result.add(IUser.HEIGHT_PROPERTY);
		result.add(IUser.WEIGHT_PROPERTY);
		return result;
	}

	@Override
	public Set<String> getPropertyDependencies() {
		return PROPERTIES;
	}

}
