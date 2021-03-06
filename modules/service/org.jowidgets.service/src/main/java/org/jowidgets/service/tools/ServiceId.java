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

package org.jowidgets.service.tools;

import java.io.Serializable;

import org.jowidgets.service.api.IServiceId;
import org.jowidgets.util.Assert;

public final class ServiceId<SERVICE_TYPE> implements IServiceId<SERVICE_TYPE>, Serializable {

	private static final long serialVersionUID = 8635227860113992430L;

	private final Object id;
	private final Class<? extends SERVICE_TYPE> serviceType;

	public ServiceId(final Class<?> serviceType) {
		this(getIdFromServiceType(serviceType), serviceType);
	}

	@SuppressWarnings("unchecked")
	public ServiceId(final Object id, final Class<?> serviceType) {
		Assert.paramNotNull(serviceType, "serviceType");
		Assert.paramNotNull(id, "id");
		this.serviceType = (Class<? extends SERVICE_TYPE>) serviceType;
		this.id = id;
	}

	private static String getIdFromServiceType(final Class<?> serviceType) {
		Assert.paramNotNull(serviceType, "serviceType");
		return serviceType.getClass().getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<SERVICE_TYPE> getServiceType() {
		return (Class<SERVICE_TYPE>) serviceType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (serviceType.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ServiceId<?> other = (ServiceId<?>) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		if (serviceType == null) {
			if (other.serviceType != null) {
				return false;
			}
		}
		else if (!serviceType.getName().equals(other.serviceType.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ServiceId(id=" + id + ", serviceType=" + serviceType.getName() + ")";
	}

}
