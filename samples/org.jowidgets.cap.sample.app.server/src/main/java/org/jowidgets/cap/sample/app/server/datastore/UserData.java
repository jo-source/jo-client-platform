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

package org.jowidgets.cap.sample.app.server.datastore;

import java.util.GregorianCalendar;

import org.jowidgets.cap.sample.app.server.entity.User;

public final class UserData extends AbstractData<User> {

	protected static final Long JOE_ESTRADA_ID = nextId();
	protected static final Long SANDRA_MAYER_ID = nextId();
	protected static final Long PETE_BROWN_ID = nextId();
	protected static final Long MARTIN_SMITH_ID = nextId();
	protected static final Long MARTY_MILLER_ID = nextId();
	protected static final Long APRIL_WINTER_ID = nextId();
	protected static final Long JAMES_MOON_ID = nextId();

	public UserData() {
		super();

		User user = new User(JOE_ESTRADA_ID);
		user.setName("Joe");
		user.setLastName("Estrada");
		user.setDateOfBirth(new GregorianCalendar(1972, 3, 22).getTime());
		user.setGender("M");
		user.addLanguage("English");
		add(user);

		user = new User(SANDRA_MAYER_ID);
		user.setName("Sandra");
		user.setLastName("Mayer");
		user.setDateOfBirth(new GregorianCalendar(1951, 4, 11).getTime());
		user.setGender("F");
		user.addLanguage("English");
		user.addLanguage("German");
		add(user);

		user = new User(PETE_BROWN_ID);
		user.setName("Pete");
		user.setLastName("Brown");
		user.setDateOfBirth(new GregorianCalendar(1983, 7, 13).getTime());
		user.setGender("M");
		user.addLanguage("English");
		user.addLanguage("French");
		add(user);

		user = new User(MARTIN_SMITH_ID);
		user.setName("Martin");
		user.setLastName("Smith");
		user.setDateOfBirth(new GregorianCalendar(1994, 11, 8).getTime());
		user.setGender("M");
		user.addLanguage("English");
		user.addLanguage("Spanish");
		add(user);

		user = new User(MARTY_MILLER_ID);
		user.setName("Marty");
		user.setLastName("Miller");
		user.setDateOfBirth(new GregorianCalendar(1934, 12, 24).getTime());
		user.setGender("M");
		user.addLanguage("English");
		add(user);

		user = new User(APRIL_WINTER_ID);
		user.setName("April");
		user.setLastName("Winter");
		user.setDateOfBirth(new GregorianCalendar(1978, 5, 23).getTime());
		user.setGender("F");
		user.addLanguage("English");
		user.addLanguage("French");
		user.addLanguage("Spanish");
		add(user);

		user = new User(JAMES_MOON_ID);
		user.setName("James");
		user.setLastName("Moon");
		user.setDateOfBirth(new GregorianCalendar(1943, 2, 19).getTime());
		user.setGender("M");
		user.addLanguage("English");
		add(user);

		for (int i = 0; i < 200000; i++) {
			user = new User(nextId());
			user.setName("Name " + i);
			user.setLastName("Lastname " + i);
			user.setDateOfBirth(new GregorianCalendar(1943, 2, 19).getTime());
			user.setGender("M");
			add(user);
		}

	}

	@Override
	public Class<User> getBeanType() {
		return User.class;
	}

	@Override
	public User createBean() {
		final User result = new User(nextId());
		add(result);
		return result;
	}

	@Override
	public void flush() {

	}

}
