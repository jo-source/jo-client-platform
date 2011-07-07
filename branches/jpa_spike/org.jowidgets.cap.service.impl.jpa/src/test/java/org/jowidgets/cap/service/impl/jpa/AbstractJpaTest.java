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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.LogManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jowidgets.cap.service.impl.jpa.entity.Job;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.junit.After;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

public abstract class AbstractJpaTest {

	static {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
	}

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void initEntityManagerFactory() throws SQLException {
		entityManagerFactory = Persistence.createEntityManagerFactory("test");
		final EntityManager em = createEntityManager();
		try {
			initData(em);
		}
		finally {
			em.close();
		}
		entityManagerFactory.getCache().evictAll();
	}

	private void initData(final EntityManager em) {
		em.getTransaction().begin();

		// 1
		final Person harald = new Person();
		harald.setName("Harald");
		harald.setPoints(0);
		harald.setBirthday(date(1975, 6, 9));
		// 2
		final Job developer = new Job(harald);
		developer.setTitle("Software Developer");
		developer.setSalary(1000000);
		harald.getJobs().add(developer);
		// 3
		final Job husband = new Job(harald);
		husband.setTitle("Husband");
		husband.setSalary(0);
		harald.getJobs().add(husband);
		em.persist(harald);

		// 4
		final Person ingo = new Person();
		ingo.setName("Ingo");
		ingo.setPoints(20);
		ingo.setBirthday(date(2005, 11, 23));
		ingo.setTriState(true);
		ingo.setTags(new HashSet<String>(Arrays.asList("lego", "playmobil")));
		em.persist(ingo);

		// 5
		final Person jan = new Person();
		jan.setName("Jan");
		jan.setPoints(-20);
		jan.setBirthday(date(2007, 11, 25));
		jan.setTriState(false);
		em.persist(jan);

		em.getTransaction().commit();
	}

	@After
	public void closeEntityManagerFactory() {
		entityManagerFactory.close();
	}

	protected EntityManager createEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

	protected static final Date date(final int year, final int month, final int day) {
		final Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(year, month - 1, day);
		return cal.getTime();
	}

}
