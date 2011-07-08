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

import junit.framework.Assert;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.exception.DeletedBeanException;
import org.jowidgets.cap.common.api.exception.StaleBeanException;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.service.IExecutorService;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.executor.IBeanExecutor;
import org.jowidgets.cap.service.api.executor.IBeanListExecutor;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.junit.Before;
import org.junit.Test;

public class JpaExecutorServiceTest extends AbstractJpaTest {

	private IExecutorService<Object> singleDataExecutorService;
	private IExecutorService<Object> multiDataExecutorService;
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
		singleDataExecutorService = CapServiceToolkit.executorServiceBuilder(personBeanProvider).setPropertyNames(propertyNames).setExecutor(
				new IBeanExecutor<Person, Integer>() {
					@Override
					public Person execute(final Person data, final Integer parameter, final IExecutionCallback executionHandle) {
						data.setPoints(data.getPoints() + parameter);
						return data;
					};
				}).build();
		multiDataExecutorService = CapServiceToolkit.executorServiceBuilder(personBeanProvider).setPropertyNames(propertyNames).setExecutor(
				new IBeanListExecutor<Person, Integer>() {
					@SuppressWarnings("unchecked")
					@Override
					public List<Person> execute(
						final List<? extends Person> data,
						final Integer parameter,
						final IExecutionCallback executionHandle) {
						for (final Person person : data) {
							person.setPoints(person.getPoints() + parameter);
						}
						return (List<Person>) data;
					};
				}).build();
	}

	@Test
	public void testSingle() {
		callService(singleDataExecutorService);
	}

	@Test
	public void testMulti() {
		callService(multiDataExecutorService);
	}

	private void callService(final IExecutorService<Object> service) {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(1L, 1)), 10, null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		entityManager.getTransaction().commit();

		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
		final IBeanDto dto = dtos.get(0);
		Assert.assertEquals(10, dto.getValue("points"));
		Assert.assertEquals(2, dto.getVersion());

		final Person person = entityManager.find(Person.class, 1L);
		Assert.assertNotNull(person);
		Assert.assertEquals(10, person.getPoints());
		Assert.assertEquals(2, person.getVersion());
	}

	@Test(expected = ConstraintViolationException.class)
	public void testSingleWithInvalidParameter() {
		callServiceWithInvalidParameter(singleDataExecutorService);
	}

	@Test(expected = ConstraintViolationException.class)
	public void testMultiWithInvalidParameter() {
		callServiceWithInvalidParameter(multiDataExecutorService);
	}

	private void callServiceWithInvalidParameter(final IExecutorService<Object> service) {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(1L, 1)), 10000, null);
		result.getResultSynchronious();
	}

	@Test(expected = DeletedBeanException.class)
	public void testSingleWithDeletedPerson() {
		callServiceWithDeletedPerson(singleDataExecutorService);
	}

	@Test(expected = DeletedBeanException.class)
	public void testMultiWithDeletedPerson() {
		callServiceWithDeletedPerson(multiDataExecutorService);
	}

	private void callServiceWithDeletedPerson(final IExecutorService<Object> service) {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(6L, 1)), 10, null);
		result.getResultSynchronious();
	}

	@Test(expected = StaleBeanException.class)
	public void testSingleWithStalePerson() {
		callServiceWithStalePerson(singleDataExecutorService);
	}

	@Test(expected = StaleBeanException.class)
	public void testMultiWithStalePerson() {
		callServiceWithStalePerson(multiDataExecutorService);
	}

	private void callServiceWithStalePerson(final IExecutorService<Object> service) {
		entityManager.getTransaction().begin();
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		service.execute(result, Collections.singletonList(new BeanKey(1L, 0)), 10, null);
		result.getResultSynchronious();
	}

}
