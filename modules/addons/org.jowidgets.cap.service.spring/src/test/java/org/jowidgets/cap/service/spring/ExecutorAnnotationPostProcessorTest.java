/*
 * Copyright (c) 2011, H.Westphal
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

package org.jowidgets.cap.service.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.service.tools.ServiceId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ExecutorAnnotationPostProcessorTest {

	@Test
	public void testDoNothing() {
		final IExecutorService<Void> service = ServiceProvider.getService(new ServiceId<IExecutorService<Void>>(
			"doNothing",
			IExecutorService.class));
		Assert.assertNotNull(service);
		service.execute(new SyncResultCallback<List<IBeanDto>>(), Collections.singletonList(new BeanKey(0, 0)), null, null);
	}

	@Test
	public void testDoNothingList() {
		final IExecutorService<Void> service = ServiceProvider.getService(new ServiceId<IExecutorService<Void>>(
			"doNothingList",
			IExecutorService.class));
		Assert.assertNotNull(service);
		service.execute(new SyncResultCallback<List<IBeanDto>>(), Collections.singletonList(new BeanKey(0, 0)), null, null);
	}

	@Test
	public void testChangeName() {
		final IExecutorService<String> service = ServiceProvider.getService(new ServiceId<IExecutorService<String>>(
			"changeName",
			IExecutorService.class));
		Assert.assertNotNull(service);
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(0, 0)), "Hans", null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals(0, dto.getId());
		Assert.assertEquals("Hans", dto.getValue("name"));
	}

	@Test
	public void testChangeNameList() {
		final IExecutorService<String> service = ServiceProvider.getService(new ServiceId<IExecutorService<String>>(
			"changeNameList",
			IExecutorService.class));
		Assert.assertNotNull(service);
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(0, 0)), "Hans", null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals(0, dto.getId());
		Assert.assertEquals("Hans", dto.getValue("name"));
	}

	@Test
	public void testChangeFirstAndLastName() {
		final IExecutorService<String[]> service = ServiceProvider.getService(new ServiceId<IExecutorService<String[]>>(
			"changeFirstAndLastName",
			IExecutorService.class));
		Assert.assertNotNull(service);
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(0, 0)), new String[] {"Hans", "Hansen"}, null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals(0, dto.getId());
		Assert.assertEquals("Hans Hansen", dto.getValue("name"));
	}

	@Test
	public void testCreateUser() {
		final IExecutorService<String> service = ServiceProvider.getService(new ServiceId<IExecutorService<String>>(
			"createUser",
			IExecutorService.class));
		Assert.assertNotNull(service);
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, new ArrayList<IBeanKey>(), "Hans Meier", null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals(1, dto.getId());
		Assert.assertEquals("Hans Meier", dto.getValue("name"));
	}

	@Test
	public void testChangeFirstAndLastNameWithComplexParameter() {
		final IExecutorService<Map<String, String>> service = ServiceProvider.getService(new ServiceId<IExecutorService<Map<String, String>>>(
			"changeFirstAndLastNameWithComplexParameter",
			IExecutorService.class));
		Assert.assertNotNull(service);
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		final Map<String, String> parameter = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			private final String lastName = "Hansen";
			{
				put("firstName", "Hans");
				put("age", "36");
			}

			@SuppressWarnings("unused")
			public String getLastName() {
				return lastName;
			}
		};
		service.execute(result, Collections.singletonList(new BeanKey(0, 0)), parameter, null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals(0, dto.getId());
		Assert.assertEquals("Hans HANSEN", dto.getValue("name"));
	}

}
