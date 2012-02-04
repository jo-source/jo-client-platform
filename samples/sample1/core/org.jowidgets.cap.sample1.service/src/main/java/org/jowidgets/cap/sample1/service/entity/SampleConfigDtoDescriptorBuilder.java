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

package org.jowidgets.cap.sample1.service.entity;

import org.jowidgets.cap.common.api.bean.IBeanPropertyBluePrint;
import org.jowidgets.cap.common.tools.bean.BeanDtoDescriptorBuilder;
import org.jowidgets.cap.sample1.common.entity.ISampleConfig;

public class SampleConfigDtoDescriptorBuilder extends BeanDtoDescriptorBuilder {

	public SampleConfigDtoDescriptorBuilder() {
		super(ISampleConfig.class);

		setLabelSingular("Sample config");

		IBeanPropertyBluePrint propertyBp = addProperty(ISampleConfig.APPLICATION_NAME_PROPERTY);
		propertyBp.setLabel("Application name");
		propertyBp.setMandatory(true);

		propertyBp = addProperty(ISampleConfig.MAX_SESSION_COUNT_PROPERTY);
		propertyBp.setLabel("Max Sessions");

		propertyBp = addProperty(ISampleConfig.MAX_CLIENT_COUNT_PROPERTY);
		propertyBp.setLabel("Max Clients");

		propertyBp = addProperty(ISampleConfig.HOST_NAME_PROPERTY);
		propertyBp.setLabel("Host");

		propertyBp = addProperty(ISampleConfig.IP_PROPERTY);
		propertyBp.setLabel("Ip");

		propertyBp = addProperty(ISampleConfig.PORT_PROPERTY);
		propertyBp.setLabel("Port");

		propertyBp = addProperty(ISampleConfig.ROOT_HOST_NAME_PROPERTY);
		propertyBp.setLabel("Root host");

		propertyBp = addProperty(ISampleConfig.ROOT_IP_PROPERTY);
		propertyBp.setLabel("Root ip");

		propertyBp = addProperty(ISampleConfig.ROOT_PORT_PROPERTY);
		propertyBp.setLabel("Root port");

		propertyBp = addProperty(ISampleConfig.SERVER_STARTED_PROPERTY);
		propertyBp.setLabel("Server started");

		propertyBp = addProperty(ISampleConfig.SERVER_UPTIME_PROPERTY);
		propertyBp.setLabel("Server uptime");

		propertyBp = addProperty(ISampleConfig.VERSION_PROPERTY);
		propertyBp.setLabel("Version");

	}

}
