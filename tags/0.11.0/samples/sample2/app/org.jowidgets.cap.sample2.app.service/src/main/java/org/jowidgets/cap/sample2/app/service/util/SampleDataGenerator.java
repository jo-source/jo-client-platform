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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.jowidgets.cap.sample2.app.service.bean.Country;
import org.jowidgets.cap.sample2.app.service.bean.Person;
import org.jowidgets.cap.sample2.app.service.bean.PersonRelationType;
import org.jowidgets.cap.sample2.app.service.bean.PersonRoleLink;
import org.jowidgets.cap.sample2.app.service.bean.Role;
import org.jowidgets.cap.sample2.app.service.lookup.GenderLookUpService;

public final class SampleDataGenerator {

	private static final String[] COUNTRIES = new String[] {
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antigua and Barbuda",
			"Argentina", "Armenia", "Aruba", "Ascension and Tristan da Cunha Saint Helena", "Australia", "Austria", "Azerbaijan",
			"Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan",
			"Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "British Virgin Islands", "Brunei", "Bulgaria",
			"Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands",
			"Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Cook Islands", "Costa Rica",
			"Côte d'Ivoire", "Croatia", "Cuba", "Curaçao", "Cyprus", "Czech Republic", "Dem. Rep. of the Congo", "Denmark",
			"Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea",
			"Eritrea", "Estonia", "Ethiopia", "Falkland Islands", "Faroe Islands", "Federated States of Micronesia", "Fiji",
			"Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada",
			"Guam", "Guatemala", "Guernsey", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hong Kong", "Hungary",
			"Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan",
			"Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon",
			"Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Madagascar", "Malawi",
			"Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Moldova",
			"Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal",
			"Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "North Korea", "Northern Mariana Islands",
			"Norway", "Oman", "Pakistan", "Palau", "Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines",
			"Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar", "Republic of (China Taiwan)",
			"Republic of Macedonia", "Republic of the Congo", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis",
			"Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "São Tomé and Príncipe", "Saudi Arabia",
			"Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia",
			"Solomon Islands", "Somalia", "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", "Sudan",
			"Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Tajikistan", "Tanzania", "Thailand", "Togo", "Tokelau",
			"Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu",
			"U.S. Virgin Islands", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay",
			"Uzbekistan", "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Western Sahara", "Yemen", "Zambia", "Zimbabwe"};

	private static final List<String> NAMES_MALE = readResource("data/names_male.txt");
	private static final List<String> NAMES_FEMALE = readResource("data/names_female.txt");
	private static final List<String> SURNAMES_DE = readResource("data/surnames_de.txt");

	private static final String ADMIN_ROLE_NAME = "Admin";
	private static final String DEVELOPER_ROLE_NAME = "Developer";
	private static final String GUEST_ROLE_NAME = "Guest";
	private static final Random RANDOM = new Random();

	SampleDataGenerator() {}

	void dropAndCreateAllData(final EntityManagerFactory entityManagerFactory, final int outerCount, final int innerCount) {
		dropData(entityManagerFactory);

		createPersonRelationTypes(entityManagerFactory);
		createCountries(entityManagerFactory);
		createRoles(entityManagerFactory);
		createPersons(entityManagerFactory, 0, outerCount, innerCount);
	}

	void dropData(final EntityManagerFactory entityManagerFactory) {
		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		entityManager.createQuery("delete from RoleAuthorizationLink").executeUpdate();
		entityManager.createQuery("delete from PersonPersonLink").executeUpdate();
		entityManager.createQuery("delete from PersonRoleLink").executeUpdate();
		entityManager.createQuery("delete from Person").executeUpdate();
		entityManager.createQuery("delete from Role").executeUpdate();
		entityManager.createQuery("delete from Authorization").executeUpdate();
		entityManager.createQuery("delete from Country").executeUpdate();
		entityManager.createQuery("delete from PersonRelationType").executeUpdate();
		tx.commit();
		entityManager.close();
	}

	void createPersonRelationTypes(final EntityManagerFactory entityManagerFactory) {

		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();

		PersonRelationType personRelationType = new PersonRelationType();
		personRelationType.setRelationName("Supervisors");
		personRelationType.setReverseRelationName("Subordinates");
		entityManager.persist(personRelationType);

		personRelationType = new PersonRelationType();
		personRelationType.setRelationName("Parents");
		personRelationType.setReverseRelationName("Children");
		entityManager.persist(personRelationType);

		entityManager.flush();
		tx.commit();
		entityManager.close();
	}

	void createCountries(final EntityManagerFactory entityManagerFactory) {

		final EntityManager entityManager = entityManagerFactory.createEntityManager();
		final EntityTransaction tx = entityManager.getTransaction();
		tx.begin();

		for (final String countryName : COUNTRIES) {
			final Country country = new Country();
			country.setName(countryName);
			entityManager.persist(country);
		}

		entityManager.flush();
		tx.commit();
		entityManager.close();
	}

	void createRoles(final EntityManagerFactory entityManagerFactory) {

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

	void createPersons(
		final EntityManagerFactory entityManagerFactory,
		final long startOffset,
		final int outerCount,
		final int innerCount) {
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
				final long nr = startOffset + i * innerCount + j;
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
		return SURNAMES_DE.get(random.nextInt(SURNAMES_DE.size()));
	}

	private static String getRandomFemaleName(final Random random) {
		return NAMES_FEMALE.get(random.nextInt(NAMES_FEMALE.size()));
	}

	private static String getRandomMaleName(final Random random) {
		return NAMES_MALE.get(random.nextInt(NAMES_MALE.size()));
	}

	private static List<String> readResource(final String resource) {

		final InputStream inputStream = DataResources.class.getClassLoader().getResourceAsStream(resource);
		if (inputStream == null) {
			throw new IllegalArgumentException("Could not find resource '" + resource + "' in classpath.");
		}

		try {
			try {
				return new ArrayList<String>(IOUtils.readLines(inputStream));
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public final class DataResources {

		private DataResources() {}

	}

}
