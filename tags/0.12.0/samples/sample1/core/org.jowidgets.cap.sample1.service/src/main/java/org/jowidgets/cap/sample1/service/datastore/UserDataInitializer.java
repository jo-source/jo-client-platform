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

package org.jowidgets.cap.sample1.service.datastore;

import java.util.GregorianCalendar;
import java.util.Random;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.sample1.common.entity.EntityIds;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.service.entity.User;
import org.jowidgets.cap.sample1.service.lookup.Countries;
import org.jowidgets.cap.sample1.service.lookup.Languages;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.bean.IBeanPropertyMap;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataFactory;
import org.jowidgets.cap.service.impl.dummy.datastore.EntityDataStore;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityData;
import org.jowidgets.cap.service.impl.dummy.datastore.IEntityFactory;

final class UserDataInitializer {

	private UserDataInitializer() {}

	public static void initialize() {

		final IEntityData<IUser> data = EntityDataFactory.create(new IEntityFactory<IUser>() {

			@Override
			public IUser createBean(final Long id) {
				return new User(id);
			}

			@Override
			public Class<? extends IUser> getBeanType() {
				return IUser.class;
			}
		});

		EntityDataStore.putEntityData(IUser.class, data);

		User user = new User(data.nextId());
		user.setName("Joe");
		user.setLastName("Estrada");
		user.setDateOfBirth(new GregorianCalendar(1972, 3, 22).getTime());
		user.setGender("M");
		user.setWeight(87d);
		user.setHeight((short) 174);
		user.addLanguage(Languages.ENGLISH);
		user.setCountry(Countries.SPAIN);
		user.setMarried(Boolean.TRUE);
		addRole(user, RoleInitializer.USER_ROLE);
		addRole(user, RoleInitializer.ADMIN_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Sandra");
		user.setLastName("Mayer");
		user.setDateOfBirth(new GregorianCalendar(1951, 4, 11).getTime());
		user.setGender("F");
		user.setWeight(65d);
		user.setHeight((short) 165);
		user.addLanguage(Languages.ENGLISH);
		user.addLanguage(Languages.GERMAN);
		user.setCountry(Countries.GERMANY);
		user.setMarried(Boolean.FALSE);
		addRole(user, RoleInitializer.USER_ROLE);
		addRole(user, RoleInitializer.ADMIN_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Pete");
		user.setLastName("Brown");
		user.setDateOfBirth(new GregorianCalendar(1983, 7, 13).getTime());
		user.setGender("M");
		user.setWeight(97d);
		user.setHeight((short) 189);
		user.addLanguage(Languages.ENGLISH);
		user.addLanguage(Languages.FRENCH);
		user.setAdmin(true);
		user.setCountry(Countries.USA);
		addRole(user, RoleInitializer.USER_ROLE);
		addRole(user, RoleInitializer.ADMIN_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Martin");
		user.setLastName("Smith");
		user.setDateOfBirth(new GregorianCalendar(1994, 11, 8).getTime());
		user.setGender("M");
		user.setWeight(79d);
		user.setHeight((short) 182);
		user.addLanguage(Languages.ENGLISH);
		user.addLanguage(Languages.SPANISH);
		user.setCountry(Countries.ITALY);
		addRole(user, RoleInitializer.USER_ROLE);
		addRole(user, RoleInitializer.ADMIN_ROLE);
		addRole(user, RoleInitializer.MANAGER_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Marty");
		user.setLastName("Miller");
		user.setDateOfBirth(new GregorianCalendar(1934, 12, 24).getTime());
		user.setGender("M");
		user.setWeight(85d);
		user.setHeight((short) 188);
		user.addLanguage(Languages.ENGLISH);
		user.setCountry(Countries.SPAIN);
		user.setMarried(Boolean.FALSE);
		addRole(user, RoleInitializer.USER_ROLE);
		addRole(user, RoleInitializer.ADMIN_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("April");
		user.setLastName("Winter");
		user.setDateOfBirth(new GregorianCalendar(1978, 5, 23).getTime());
		user.setGender("F");
		user.setWeight(57d);
		user.setHeight((short) 173);
		user.addLanguage(Languages.ENGLISH);
		user.addLanguage(Languages.FRENCH);
		user.addLanguage(Languages.SPANISH);
		addRole(user, RoleInitializer.USER_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("James");
		user.setLastName("Moon");
		user.setDateOfBirth(new GregorianCalendar(1943, 2, 19).getTime());
		user.setGender("M");
		user.setWeight(103d);
		user.setHeight((short) 182);
		user.addLanguage(Languages.ENGLISH);
		user.setAdmin(true);
		user.setMarried(Boolean.TRUE);
		user.setCountry(Countries.USA);
		addRole(user, RoleInitializer.USER_ROLE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Jonny");
		user.setLastName("English");
		user.setDateOfBirth(new GregorianCalendar(1961, 1, 18).getTime());
		user.setGender("M");
		user.setWeight(89d);
		user.setHeight((short) 179);
		user.addLanguage(Languages.ENGLISH);
		user.setAdmin(false);
		user.setMarried(Boolean.FALSE);
		user.setCountry(Countries.USA);
		addRole(user, RoleInitializer.USER_ROLE);
		data.add(user);

		final Random random = new Random();

		for (int i = 0; i < 20000; i++) {
			data.add(getRandomUser(random, data));
		}

	}

	private static User getRandomUser(final Random random, final IEntityData<IUser> data) {
		final User user = new User(data.nextId());
		final String gender = getRandomGender(random);
		user.setGender(gender);
		if ("M".equals(gender)) {
			user.setName(getRandomMaleName(random));
		}
		else {
			user.setName(getRandomFemaleName(random));
		}
		user.setLastName(getRandomSurname(random));
		user.setWeight(Double.valueOf(random.nextInt(70) + 40));
		user.setHeight((short) (random.nextInt(60) + 150));
		user.setDateOfBirth(new GregorianCalendar(2000 - random.nextInt(80), random.nextInt(12) + 1, random.nextInt(28) + 1).getTime());
		user.setCountry(random.nextInt(Countries.COUNTRIES.length));
		for (int i = 0; i < random.nextInt(4); i++) {
			final int language = random.nextInt(Languages.LANGUAGES.length);
			if (!user.getLanguages().contains(language)) {
				user.addLanguage(language);
			}
		}
		if (random.nextBoolean()) {
			user.setMarried(random.nextBoolean());
		}
		user.setAdmin(random.nextBoolean());
		return user;
	}

	private static String getRandomGender(final Random random) {
		if (random.nextInt(2) == 0) {
			return "M";
		}
		else {
			return "F";
		}
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

	@SuppressWarnings("unchecked")
	private static void addRole(final IUser user, final String roleName) {
		final IEntityData<IBeanPropertyMap> data = (IEntityData<IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.USER_ROLE_LINK);
		final IBeanPropertyMap bean = CapServiceToolkit.beanPropertyMap(EntityIds.USER_ROLE_LINK);
		bean.setId(data.nextId());
		bean.setValue(UserRoleLinkInitializer.USER_ID, user.getId());
		bean.setValue(UserRoleLinkInitializer.ROLE_ID, getRoleIdByName(roleName));
		data.add(bean);
	}

	@SuppressWarnings("unchecked")
	private static Long getRoleIdByName(final String roleName) {
		final IEntityData<IBeanPropertyMap> rolesData = (IEntityData<IBeanPropertyMap>) EntityDataStore.getEntityData(EntityIds.ROLE);
		for (final IBeanPropertyMap role : rolesData.getAllData()) {
			if (roleName.equals(role.getValue(RoleInitializer.NAME_PROPERTY))) {
				return (Long) role.getValue(IBean.ID_PROPERTY);
			}
		}
		return null;
	}
}
