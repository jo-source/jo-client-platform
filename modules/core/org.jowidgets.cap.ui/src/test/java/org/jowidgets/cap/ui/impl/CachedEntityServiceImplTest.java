/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.ui.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.EntityInfo;
import org.jowidgets.cap.common.api.service.IBeanServicesProvider;
import org.jowidgets.cap.common.api.service.IEntityInfo;
import org.jowidgets.cap.common.api.service.IEntityInfoBuilder;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.util.CollectionUtils;
import org.jowidgets.util.EmptyCheck;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class CachedEntityServiceImplTest {

	private static final String ENTITY_ID_1 = "ENTITY_ID_1";
	private static final String ENTITY_ID_2 = "ENTITY_ID_2";

	private static final IBeanServicesProvider ENTITY_1_SERVICES = Mockito.mock(IBeanServicesProvider.class);
	private static final IBeanDtoDescriptor ENTITY_1_BEAN_DTO_DESCRIPTOR = Mockito.mock(IBeanDtoDescriptor.class);
	private static final List<IEntityLinkDescriptor> ENTITY_1_LINKS = new LinkedList<IEntityLinkDescriptor>();

	private static final IEntityInfo ENTITY_1_INFO = createEntityInfo1();
	private static final List<IEntityInfo> ENTITY_INFOS = Arrays.asList(ENTITY_1_INFO);

	private IEntityService original;

	@Before
	public void setUp() {
		this.original = createOriginalEntityService();
	}

	@Test
	public void testAccessOriginalOnce() {
		final CachedEntityServiceImpl cached = new CachedEntityServiceImpl(original);

		Assert.assertTrue(CollectionUtils.elementsEqual(ENTITY_INFOS, cached.getEntityInfos()));
		Assert.assertTrue(CollectionUtils.elementsEqual(ENTITY_INFOS, cached.getEntityInfos()));

		Assert.assertEquals(ENTITY_1_INFO, cached.getEntityInfo(ENTITY_ID_1));
		Assert.assertEquals(ENTITY_1_INFO, cached.getEntityInfo(ENTITY_ID_1));

		Assert.assertEquals(ENTITY_1_SERVICES, cached.getBeanServices(ENTITY_ID_1));
		Assert.assertEquals(ENTITY_1_SERVICES, cached.getBeanServices(ENTITY_ID_1));

		Assert.assertEquals(ENTITY_1_BEAN_DTO_DESCRIPTOR, cached.getDescriptor(ENTITY_ID_1));
		Assert.assertEquals(ENTITY_1_BEAN_DTO_DESCRIPTOR, cached.getDescriptor(ENTITY_ID_1));

		Assert.assertEquals(ENTITY_1_LINKS, cached.getEntityLinks(ENTITY_ID_1));
		Assert.assertEquals(ENTITY_1_LINKS, cached.getEntityLinks(ENTITY_ID_1));

		Mockito.verify(original, Mockito.times(1)).getEntityInfos();
		//Entity info will not invoked on original because entity infos was invoked first
		Mockito.verify(original, Mockito.times(0)).getEntityInfo(ENTITY_ID_1);
	}

	@Test
	public void testReturnNullForUnkowns() {
		final CachedEntityServiceImpl cached = new CachedEntityServiceImpl(original);

		Assert.assertNull(cached.getEntityInfo(ENTITY_ID_2));
		Assert.assertNull(cached.getDescriptor(ENTITY_ID_2));
		Assert.assertNull(cached.getBeanServices(ENTITY_ID_2));
		Assert.assertTrue(EmptyCheck.isEmpty(cached.getEntityLinks(ENTITY_ID_2)));

		Mockito.verify(original, Mockito.times(1)).getEntityInfo(ENTITY_ID_2);
	}

	private IEntityService createOriginalEntityService() {
		final IEntityService result = Mockito.mock(IEntityService.class);
		Mockito.when(result.getEntityInfos()).thenReturn(ENTITY_INFOS);
		Mockito.when(result.getEntityInfo(ENTITY_ID_1)).thenReturn(ENTITY_1_INFO);
		Mockito.when(result.getDescriptor(Mockito.eq(ENTITY_ID_1))).thenReturn(ENTITY_1_BEAN_DTO_DESCRIPTOR);
		Mockito.when(result.getBeanServices(Mockito.eq(ENTITY_ID_1))).thenReturn(ENTITY_1_SERVICES);
		Mockito.when(result.getEntityLinks(Mockito.eq(ENTITY_ID_1))).thenReturn(ENTITY_1_LINKS);
		return result;
	}

	private static IEntityInfo createEntityInfo1() {
		final IEntityInfoBuilder builder = EntityInfo.builder();
		builder.setEntityId(ENTITY_ID_1);
		builder.setBeanServices(ENTITY_1_SERVICES);
		builder.setDescriptor(ENTITY_1_BEAN_DTO_DESCRIPTOR);
		builder.setEntityLinks(ENTITY_1_LINKS);
		return builder.build();
	}

}
