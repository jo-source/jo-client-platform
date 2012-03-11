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
package org.jowidgets.cap.service.impl.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.BeanException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JpaUpdaterServiceTest extends AbstractJpaTest {

	private IUpdaterService dataUpdaterService;
	private EntityManager entityManager;

	@Before
	public void setUp() {
		entityManager = createEntityManager();
		final JpaBeanAccess<Person> personBeanProvider = new JpaBeanAccess<Person>(Person.class);
		personBeanProvider.setEntityManager(entityManager);
		final List<String> propertyNames = new ArrayList<String>();
		propertyNames.add("name");
		propertyNames.add("points");
		propertyNames.add("triState");
		propertyNames.add("birthday");
		dataUpdaterService = CapServiceToolkit.updaterServiceBuilder(personBeanProvider).setBeanDtoFactoryAndBeanModifier(
				propertyNames).build();
	}

	@Test
	public void testUpdatePerson() {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		dataUpdaterService.update(
				result,
				Collections.singleton(CapCommonToolkit.beanModificationBuilder().setId(1L).setVersion(1).setPropertyName("name").setNewValue(
						"Harald-René").setOldValue("Harald").build()),
				null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		entityManager.getTransaction().commit();

		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals("Harald-René", dto.getValue("name"));
		Assert.assertEquals(2, dto.getVersion());

		final Person person = entityManager.find(Person.class, 1L);
		Assert.assertNotNull(person);
		Assert.assertEquals("Harald-René", person.getName());
		Assert.assertEquals(2, person.getVersion());
	}

	@Test(expected = ConstraintViolationException.class)
	public void testUpdateInvalidPropertyValue() {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		dataUpdaterService.update(
				result,
				Collections.singleton(CapCommonToolkit.beanModificationBuilder().setId(1L).setVersion(1).setPropertyName("points").setNewValue(
						1000).setOldValue(0).build()),
				null);
		result.getResultSynchronious();
	}

	@Test(expected = DeletedBeanException.class)
	public void testUpdateDeletedPerson() {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		dataUpdaterService.update(
				result,
				Collections.singleton(CapCommonToolkit.beanModificationBuilder().setId(6L).setVersion(1).setPropertyName("points").setNewValue(
						0).build()),
				null);
		result.getResultSynchronious();
	}

	@Test(expected = StaleBeanException.class)
	public void testUpdateStalePerson() {
		entityManager.getTransaction().begin();
		SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		dataUpdaterService.update(
				result,
				Collections.singleton(CapCommonToolkit.beanModificationBuilder().setId(1L).setVersion(1).setPropertyName("name").setNewValue(
						"Harald-René").build()),
				null);
		result.getResultSynchronious();
		entityManager.getTransaction().commit();
		entityManager.getTransaction().begin();
		result = new SyncResultCallback<List<IBeanDto>>();
		dataUpdaterService.update(
				result,
				Collections.singleton(CapCommonToolkit.beanModificationBuilder().setId(1L).setVersion(1).setPropertyName("name").setNewValue(
						"Harry").build()),
				null);
		result.getResultSynchronious();
	}

	@Test(expected = BeanException.class)
	public void testUpdateInvalidProperty() {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		dataUpdaterService.update(
				result,
				Collections.singleton(CapCommonToolkit.beanModificationBuilder().setId(1L).setVersion(1).setPropertyName("foo").setNewValue(
						1000).build()),
				null);
		result.getResultSynchronious();
	}

}
