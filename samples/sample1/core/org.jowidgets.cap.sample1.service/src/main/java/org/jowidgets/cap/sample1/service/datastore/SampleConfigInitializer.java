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

package org.jowidgets.cap.sample1.service.datastore;

import org.jowidgets.cap.sample1.common.entity.ISampleConfig;
import org.jowidgets.cap.sample1.service.entity.SampleConfig;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataFactory;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataStore;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityFactory;

final class SampleConfigInitializer {

	private SampleConfigInitializer() {}

	public static void initialize() {

		final IEntityData<ISampleConfig> data = EntityDataFactory.create(new IEntityFactory<ISampleConfig>() {

			@Override
			public ISampleConfig createBean(final Long id) {
				return new SampleConfig(id);
			}

			@Override
			public Class<? extends ISampleConfig> getBeanType() {
				return ISampleConfig.class;
			}
		});

		EntityDataStore.putEntityData(ISampleConfig.class, data);

		final SampleConfig config = new SampleConfig(data.nextId());
		config.setApplicationName("Sample Application 1");
		config.setHostName("localhost");
		config.setIp("127.0.0.1");
		config.setPort(8081);
		config.setRootHostName("localhost");
		config.setRootIp("127.0.0.1");
		config.setRootPort(8081);

		data.add(config);

	}
}
