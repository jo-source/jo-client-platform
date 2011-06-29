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

import java.util.Collections;

import javax.persistence.EntityManager;

import junit.framework.Assert;

import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.impl.jpa.entity.Job;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.junit.Before;
import org.junit.Test;

public class JpaDeleterServiceTest extends AbstractJpaTest {

	private JpaDeleterService personDeleterService;
	private EntityManager entityManager;

	@Before
	public void setUp() {
		entityManager = createEntityManager();
		final JpaBeanAccess<Person> personBeanProvider = new JpaBeanAccess<Person>(Person.class);
		personBeanProvider.setEntityManager(entityManager);
		personDeleterService = new JpaDeleterService(personBeanProvider);
		personDeleterService.setEntityManager(entityManager);
	}

	@Test
	public void testDeletePerson() {
		Person person = entityManager.find(Person.class, 1L);
		Job job = entityManager.find(Job.class, 2L);
		Assert.assertNotNull(person);
		Assert.assertNotNull(job);
		entityManager.getTransaction().begin();
		final SyncResultCallback<Void> result = new SyncResultCallback<Void>();
		personDeleterService.delete(result, Collections.singletonList(new BeanKey(1L, 0)), null);
		result.getResultSynchronious();
		entityManager.getTransaction().commit();
		person = entityManager.find(Person.class, 1L);
		Assert.assertNull(person);
		job = entityManager.find(Job.class, 2L);
		Assert.assertNull(job);
	}

	@Test
	public void testDeleteMissingPerson() {
		entityManager.getTransaction().begin();
		final SyncResultCallback<Void> result = new SyncResultCallback<Void>();
		personDeleterService.delete(result, Collections.singletonList(new BeanKey(6L, 0)), null);
		result.getResultSynchronious();
		entityManager.getTransaction().commit();
	}

	@Test
	public void testDeletePersonWithRollback() {
		Person person = entityManager.find(Person.class, 1L);
		Assert.assertNotNull(person);
		entityManager.getTransaction().begin();
		final SyncResultCallback<Void> result = new SyncResultCallback<Void>();
		personDeleterService.delete(result, Collections.singletonList(new BeanKey(1L, 0)), null);
		result.getResultSynchronious();
		entityManager.getTransaction().rollback();
		person = entityManager.find(Person.class, 1L);
		Assert.assertNotNull(person);
	}

}
