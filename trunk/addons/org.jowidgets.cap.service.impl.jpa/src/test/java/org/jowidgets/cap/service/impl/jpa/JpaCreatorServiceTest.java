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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JpaCreatorServiceTest extends AbstractJpaTest {

	private JpaCreatorService personCreatorService;
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
		personCreatorService = new JpaCreatorService(personBeanProvider, propertyNames);
		personCreatorService.setEntityManager(entityManager);
	}

	@Test
	public void testCreatePersonWithAllProperties() {
		testCreatePerson(true);
	}

	@Test
	public void testCreatePersonWithoutOptionalProperties() {
		testCreatePerson(false);
	}

	private void testCreatePerson(final boolean withTriState) {
		Person person = entityManager.find(Person.class, 6L);
		Assert.assertNull(person);

		entityManager.getTransaction().begin();
		final Map<String, Object> beanProperties = new HashMap<String, Object>();
		beanProperties.put("name", "Claudia");
		beanProperties.put("points", 50);
		if (withTriState) {
			beanProperties.put("triState", true);
		}
		beanProperties.put("birthday", date(1975, 9, 21));
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		personCreatorService.create(
				result,
				Collections.singletonList(CapCommonToolkit.beanDataBuilder().setProperties(beanProperties).build()),
				null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		entityManager.getTransaction().commit();

		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals("Claudia", dto.getValue("name"));
		Assert.assertEquals(50, dto.getValue("points"));
		Assert.assertEquals(withTriState ? true : null, dto.getValue("triState"));
		Assert.assertEquals(date(1975, 9, 21), dto.getValue("birthday"));

		person = entityManager.find(Person.class, 6L);
		Assert.assertNotNull(person);
		Assert.assertEquals("Claudia", person.getName());
		Assert.assertEquals(50, person.getPoints());
		Assert.assertEquals(withTriState ? Boolean.TRUE : null, person.getTriState());
		Assert.assertEquals(date(1975, 9, 21), person.getBirthday());
	}

	@Test(expected = ConstraintViolationException.class)
	public void testCreateInvalidPerson() {
		entityManager.getTransaction().begin();
		final Map<String, Object> beanProperties = new HashMap<String, Object>();
		beanProperties.put("name", "Claudia");
		beanProperties.put("points", 500);
		beanProperties.put("birthday", date(1975, 9, 21));
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		personCreatorService.create(
				result,
				Collections.singletonList(CapCommonToolkit.beanDataBuilder().setProperties(beanProperties).build()),
				null);
		result.getResultSynchronious();
	}

}
