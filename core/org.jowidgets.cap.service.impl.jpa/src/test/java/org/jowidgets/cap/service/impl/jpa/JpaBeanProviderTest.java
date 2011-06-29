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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.service.impl.jpa.entity.Job;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JpaBeanProviderTest extends AbstractJpaTest {

	private JpaBeanAccess<Person> personBeanProvider;
	private JpaBeanAccess<Job> jobBeanProvider;

	@Before
	public void setUp() {
		final EntityManager entityManager = createEntityManager();
		personBeanProvider = new JpaBeanAccess<Person>(Person.class);
		personBeanProvider.setEntityManager(entityManager);
		jobBeanProvider = new JpaBeanAccess<Job>(Job.class);
		jobBeanProvider.setEntityManager(entityManager);
	}

	@Test
	public void testGetOnePerson() {
		final List<Person> persons = personBeanProvider.getBeans(Collections.singleton(new BeanKey(1L, 0)), null);
		Assert.assertNotNull(persons);
		Assert.assertEquals(1, persons.size());
		final Person person = persons.get(0);
		Assert.assertNotNull(person);
		Assert.assertEquals("Harald", person.getName());
		Assert.assertEquals(0, person.getPoints());
		Assert.assertEquals(null, person.getTriState());
		Assert.assertEquals(date(1975, 6, 9), person.getBirthday());
		Assert.assertEquals(2, person.getJobs().size());
		Assert.assertNotNull(person.getJobTitles());
		final Set<String> jobTitles = new HashSet<String>();
		jobTitles.add("Software Developer");
		jobTitles.add("Husband");
		Assert.assertEquals(jobTitles, person.getJobTitles());
	}

	@Test
	public void testGetJobs() {
		final Collection<IBeanKey> beanInfos = new ArrayList<IBeanKey>();
		beanInfos.add(new BeanKey(2L, 0));
		beanInfos.add(new BeanKey(3L, 0));
		final Collection<Job> jobs = jobBeanProvider.getBeans(beanInfos, null);
		Assert.assertNotNull(jobs);
		Assert.assertEquals(2, jobs.size());
		for (final Job job : jobs) {
			Assert.assertEquals("Harald", job.getPersonName());
		}
	}

	@Test
	public void testGetTwoPersons() {
		final Collection<IBeanKey> beanInfos = new ArrayList<IBeanKey>();
		beanInfos.add(new BeanKey(4L, 0));
		beanInfos.add(new BeanKey(5L, 0));
		final Collection<Person> persons = personBeanProvider.getBeans(beanInfos, null);
		Assert.assertNotNull(persons);
		Assert.assertEquals(2, persons.size());
	}

	@Test
	public void testGetNoPersons() {
		final Collection<Person> persons = personBeanProvider.getBeans(Collections.singleton(new BeanKey(6L, 0)), null);
		Assert.assertNotNull(persons);
		Assert.assertEquals(0, persons.size());
	}

}
