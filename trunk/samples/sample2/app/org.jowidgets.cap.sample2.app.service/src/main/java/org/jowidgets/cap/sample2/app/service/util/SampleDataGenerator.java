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

package org.jowidgets.cap.sample2.app.service.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.jowidgets.cap.sample2.app.service.bean.Person;
import org.jowidgets.cap.sample2.app.service.bean.Role;

public final class SampleDataGenerator {

	private SampleDataGenerator() {}

	public static void main(final String[] args) {
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("sample2PersistenceUnit");
		dropData(entityManagerFactory);
		createPersons(entityManagerFactory);
		createRoles(entityManagerFactory);
	}

	private static void dropData(final EntityManagerFactory entityManagerFactory) {
		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.createQuery("delete from Person").executeUpdate();
		entityManager.createQuery("delete from Role").executeUpdate();
		tx.commit();
		entityManager.close();
	}

	private static void createPersons(final EntityManagerFactory entityManagerFactory) {
		final int outerCount = 1;
		final int innerCount = 1000;
		for (int i = 0; i < outerCount; i++) {
			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			final EntityTransaction tx = entityManager.getTransaction();
			tx.begin();
			for (int j = 0; j < innerCount; j++) {
				final Person user = new Person();
				final int nr = i * innerCount + j;
				//CHECKSTYLE:OFF
				System.out.println("DATASET NR: " + nr);
				//CHECKSTYLE:ON
				user.setName("Name " + nr);
				user.setLastname("Lastname " + nr);
				user.setLoginName("Login name " + nr);
				entityManager.persist(user);
				entityManager.flush();
			}
			tx.commit();
			entityManager.close();
		}
	}

	private static void createRoles(final EntityManagerFactory entityManagerFactory) {

		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		Role role = new Role();
		role.setName("Admin");
		role.setDescription("The administrator role");
		entityManager.persist(role);

		role = new Role();
		role.setName("Developer");
		role.setDescription("The developers role");
		entityManager.persist(role);

		role = new Role();
		role.setName("Guest");
		role.setDescription("The guest role");
		entityManager.persist(role);

		entityManager.flush();
		tx.commit();
		entityManager.close();

	}
}
