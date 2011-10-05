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

package org.jowidgets.cap.sample1.service.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.util.Assert;

public class User extends AbstractSampleBean implements IUser {

	private String name;
	private String lastName;
	private Date dateOfBirth;
	private String gender;
	private Integer country;
	private List<String> languages;
	private boolean admin;
	private Boolean married;
	private Double weight;
	private Short height;

	public User(final Long id) {
		super(id);
		this.languages = new LinkedList<String>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
		increaseVersion();
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public void setLastName(final String lastName) {
		this.lastName = lastName;
		increaseVersion();
	}

	@Override
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	@Override
	public void setDateOfBirth(final Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
		increaseVersion();
	}

	@Override
	public Double getWeight() {
		return weight;
	}

	@Override
	public void setWeight(final Double weight) {
		this.weight = weight;
		increaseVersion();
	}

	@Override
	public Short getHeight() {
		return height;
	}

	@Override
	public void setHeight(final Short height) {
		this.height = height;
		increaseVersion();
	}

	@Override
	public Double getBmi() {
		if (height == null || weight == null) {
			return null;
		}
		final double quot = (double) height / 100;
		if (height / 100 != 0) {
			return weight / (quot * quot);
		}
		return null;
	}

	@Override
	public Integer getAge() {
		if (dateOfBirth != null) {
			final Calendar currentDate = new GregorianCalendar();
			final Calendar birthDay = new GregorianCalendar();
			birthDay.setTime(dateOfBirth);
			int result = currentDate.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR) - 1;
			if (currentDate.get(Calendar.DAY_OF_YEAR) >= birthDay.get(Calendar.DAY_OF_YEAR)) {
				result++;
			}
			return Integer.valueOf(result);
		}
		else {
			return null;
		}
	}

	@Override
	public String getGender() {
		return gender;
	}

	@Override
	public void setGender(final String gender) {
		this.gender = gender;
		increaseVersion();
	}

	@Override
	public Integer getCountry() {
		return country;
	}

	@Override
	public void setCountry(final Integer country) {
		this.country = country;
	}

	@Override
	public List<String> getLanguages() {
		return new LinkedList<String>(languages);
	}

	@Override
	public void setLanguages(final List<String> languages) {
		if (languages != null) {
			this.languages = new LinkedList<String>(languages);
		}
		else {
			this.languages.clear();
		}
		increaseVersion();
	}

	@Override
	public boolean getAdmin() {
		return admin;
	}

	@Override
	public void setAdmin(final boolean admin) {
		this.admin = admin;
	}

	@Override
	public Boolean getMarried() {
		return married;
	}

	@Override
	public void setMarried(final Boolean married) {
		this.married = married;
	}

	public void addLanguage(final String language) {
		Assert.paramNotEmpty(language, "language");
		this.languages.add(language);
		increaseVersion();
	}

	public void removeLanguage(final String language) {
		Assert.paramNotEmpty(language, "language");
		this.languages.remove(language);
		increaseVersion();
	}

}
