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

package org.jowidgets.cap.security.ui.impl;

import java.util.HashSet;
import java.util.Set;

import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.security.ui.api.DecorationStrategy;
import org.jowidgets.cap.security.ui.api.IDecorationStrategySelector;
import org.jowidgets.service.api.IServiceId;

final class DefaultDecorationStrategySelector implements IDecorationStrategySelector {

	private static final Set<Class<?>> FILTER_SERVICES = createFilterServices();

	@Override
	public <SERVICE_TYPE> DecorationStrategy getStrategy(final IServiceId<SERVICE_TYPE> id, final SERVICE_TYPE service) {
		if (FILTER_SERVICES.contains(id.getServiceType())) {
			return DecorationStrategy.FILTER;
		}
		else {
			return DecorationStrategy.ADD_AUTHORIZATION;
		}
	}

	private static Set<Class<?>> createFilterServices() {
		final Set<Class<?>> result = new HashSet<Class<?>>();
		result.add(ICreatorService.class);
		result.add(IUpdaterService.class);
		result.add(IDeleterService.class);
		result.add(ILinkCreatorService.class);
		result.add(ILinkDeleterService.class);
		return result;
	}
}
