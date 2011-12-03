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

import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.jowidgets.cap.sample2.app.service.bean.Country;
import org.jowidgets.cap.sample2.app.service.bean.Person;
import org.jowidgets.cap.sample2.app.service.bean.PersonRoleLink;
import org.jowidgets.cap.sample2.app.service.bean.Role;
import org.jowidgets.cap.sample2.app.service.lookup.GenderLookUpService;

public final class SampleDataGenerator {

	private static final String ADMIN_ROLE_NAME = "Admin";
	private static final String DEVELOPER_ROLE_NAME = "Developer";
	private static final String GUEST_ROLE_NAME = "Guest";
	private static final Random RANDOM = new Random();

	private SampleDataGenerator() {}

	public static void main(final String[] args) {
		final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("sample2PersistenceUnit");
		dropData(entityManagerFactory);

		createCountries(entityManagerFactory);
		createRoles(entityManagerFactory);
		createPersons(entityManagerFactory);
	}

	private static void dropData(final EntityManagerFactory entityManagerFactory) {
		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.createQuery("delete from PersonRoleLink").executeUpdate();
		entityManager.createQuery("delete from Person").executeUpdate();
		entityManager.createQuery("delete from Role").executeUpdate();
		entityManager.createQuery("delete from Country").executeUpdate();
		tx.commit();
		entityManager.close();
	}

	private static void createCountries(final EntityManagerFactory entityManagerFactory) {

		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();

		for (final String countryName : Countries.COUNTRIES) {
			final Country country = new Country();
			country.setName(countryName);
			entityManager.persist(country);
		}

		entityManager.flush();
		tx.commit();
		entityManager.close();
	}

	private static void createRoles(final EntityManagerFactory entityManagerFactory) {

		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();

		Role role = new Role();
		role.setName(ADMIN_ROLE_NAME);
		role.setDescription("The administrator role");
		entityManager.persist(role);

		role = new Role();
		role.setName(DEVELOPER_ROLE_NAME);
		role.setDescription("The developers role");
		entityManager.persist(role);

		role = new Role();
		role.setName(GUEST_ROLE_NAME);
		role.setDescription("The guest role");
		entityManager.persist(role);

		entityManager.flush();
		tx.commit();
		entityManager.close();

	}

	private static void createPersons(final EntityManagerFactory entityManagerFactory) {
		final int outerCount = 1;
		final int innerCount = 1000;
		for (int i = 0; i < outerCount; i++) {
			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			final TypedQuery<Role> roleQuery = entityManager.createQuery(
					"SELECT r FROM Role r WHERE r.name LIKE :roleName",
					Role.class);
			final Role adminRole = roleQuery.setParameter("roleName", ADMIN_ROLE_NAME).getSingleResult();
			final Role developerRole = roleQuery.setParameter("roleName", DEVELOPER_ROLE_NAME).getSingleResult();
			final Role guestRole = roleQuery.setParameter("roleName", GUEST_ROLE_NAME).getSingleResult();

			final List<Country> countries = entityManager.createQuery("SELECT c from Country c", Country.class).getResultList();
			final Country[] countriesArray = countries.toArray(new Country[countries.size()]);

			final EntityTransaction tx = entityManager.getTransaction();
			tx.begin();
			for (int j = 0; j < innerCount; j++) {
				final Person user = new Person();
				final int nr = i * innerCount + j;
				//CHECKSTYLE:OFF
				System.out.println("DATASET NR: " + nr);
				//CHECKSTYLE:ON
				if (RANDOM.nextBoolean()) {
					user.setName(getRandomMaleName(RANDOM));
					user.setGender(GenderLookUpService.MALE_KEY);
				}
				else {
					user.setName(getRandomFemaleName(RANDOM));
					user.setGender(GenderLookUpService.FEMALE_KEY);
				}
				user.setLastname(getRandomSurname(RANDOM));
				user.setLoginName("LN" + nr);
				user.setActive(Boolean.TRUE);
				user.setCountry(countriesArray[RANDOM.nextInt(countries.size())]);

				entityManager.persist(user);

				if (RANDOM.nextBoolean()) {
					linkRole(user, adminRole, entityManager);
				}
				if (RANDOM.nextBoolean()) {
					linkRole(user, developerRole, entityManager);
				}
				if (RANDOM.nextBoolean()) {
					linkRole(user, guestRole, entityManager);
				}

				entityManager.flush();
			}
			tx.commit();
			entityManager.close();
		}
	}

	private static void linkRole(final Person person, final Role role, final EntityManager entityManager) {
		final PersonRoleLink personRoleLink = new PersonRoleLink();
		personRoleLink.setPerson(person);
		personRoleLink.setRole(role);
		entityManager.persist(personRoleLink);
	}

	private static String getRandomSurname(final Random random) {
		return DataResources.SURNAMES_DE.get(random.nextInt(DataResources.SURNAMES_DE.size()));
	}

	private static String getRandomFemaleName(final Random random) {
		return DataResources.NAMES_FEMALE.get(random.nextInt(DataResources.NAMES_FEMALE.size()));
	}

	private static String getRandomMaleName(final Random random) {
		return DataResources.NAMES_MALE.get(random.nextInt(DataResources.NAMES_MALE.size()));
	}

}
