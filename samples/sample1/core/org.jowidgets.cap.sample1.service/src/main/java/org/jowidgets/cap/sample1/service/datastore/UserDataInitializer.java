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

import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.service.entity.User;
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
		user.addLanguage("English");
		user.setCountry("Spain");
		user.setMarried(Boolean.TRUE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Sandra");
		user.setLastName("Mayer");
		user.setDateOfBirth(new GregorianCalendar(1951, 4, 11).getTime());
		user.setGender("F");
		user.setWeight(65d);
		user.setHeight((short) 165);
		user.addLanguage("English");
		user.addLanguage("German");
		user.setCountry("Germany");
		user.setMarried(Boolean.FALSE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("Pete");
		user.setLastName("Brown");
		user.setDateOfBirth(new GregorianCalendar(1983, 7, 13).getTime());
		user.setGender("M");
		user.setWeight(97d);
		user.setHeight((short) 189);
		user.addLanguage("English");
		user.addLanguage("French");
		user.setAdmin(true);
		user.setCountry("USA");
		data.add(user);

		user = new User(data.nextId());
		user.setName("Martin");
		user.setLastName("Smith");
		user.setDateOfBirth(new GregorianCalendar(1994, 11, 8).getTime());
		user.setGender("M");
		user.setWeight(79d);
		user.setHeight((short) 182);
		user.addLanguage("English");
		user.addLanguage("Spanish");
		user.setCountry("Italy");
		data.add(user);

		user = new User(data.nextId());
		user.setName("Marty");
		user.setLastName("Miller");
		user.setDateOfBirth(new GregorianCalendar(1934, 12, 24).getTime());
		user.setGender("M");
		user.setWeight(85d);
		user.setHeight((short) 188);
		user.addLanguage("English");
		user.setCountry("Spain");
		user.setMarried(Boolean.FALSE);
		data.add(user);

		user = new User(data.nextId());
		user.setName("April");
		user.setLastName("Winter");
		user.setDateOfBirth(new GregorianCalendar(1978, 5, 23).getTime());
		user.setGender("F");
		user.setWeight(57d);
		user.setHeight((short) 173);
		user.addLanguage("English");
		user.addLanguage("French");
		user.addLanguage("Spanish");
		data.add(user);

		user = new User(data.nextId());
		user.setName("James");
		user.setLastName("Moon");
		user.setDateOfBirth(new GregorianCalendar(1943, 2, 19).getTime());
		user.setGender("M");
		user.setWeight(103d);
		user.setHeight((short) 182);
		user.addLanguage("English");
		user.setAdmin(true);
		user.setMarried(Boolean.TRUE);
		user.setCountry("USA");
		data.add(user);

		user = new User(data.nextId());
		user.setName("Jonny");
		user.setLastName("English");
		user.setDateOfBirth(new GregorianCalendar(1961, 1, 18).getTime());
		user.setGender("M");
		user.setWeight(89d);
		user.setHeight((short) 179);
		user.addLanguage("English");
		user.setAdmin(false);
		user.setMarried(Boolean.FALSE);
		user.setCountry("USA");
		data.add(user);

		for (int i = 0; i < 20000; i++) {
			user = new User(data.nextId());
			user.setName("Name " + i);
			user.setLastName("Lastname " + i);
			user.setDateOfBirth(new GregorianCalendar(1943, 2, 19).getTime());
			user.setGender("M");
			data.add(user);
		}

	}
}
