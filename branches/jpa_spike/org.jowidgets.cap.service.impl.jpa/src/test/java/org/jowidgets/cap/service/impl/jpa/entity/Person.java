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
package org.jowidgets.cap.service.impl.jpa.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.jowidgets.cap.service.impl.jpa.jpql.QueryPath;

@Entity
public class Person implements IPerson {

	@Id
	@GeneratedValue
	private Long id;

	@Version
	private long version;

	@Basic
	private String name;

	@Basic
	private int points;

	@Basic
	private Boolean triState;

	@Basic
	@Temporal(TemporalType.DATE)
	private Date birthday;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "owner")
	private Set<Job> jobs = new HashSet<Job>();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int getPoints() {
		return points;
	}

	@Override
	public void setPoints(final int points) {
		this.points = points;
	}

	@Override
	public Boolean getTriState() {
		return triState;
	}

	@Override
	public void setTriState(final Boolean triState) {
		this.triState = triState;
	}

	@Override
	public Date getBirthday() {
		return birthday;
	}

	@Override
	public void setBirthday(final Date birthday) {
		this.birthday = birthday;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public long getVersion() {
		return version;
	}

	@Override
	@QueryPath("jobs.title")
	public Set<String> getJobTitles() {
		final Set<String> jobTitles = new HashSet<String>();
		for (final Job job : jobs) {
			jobTitles.add(job.getTitle());
		}
		return jobTitles;
	}

	public Set<Job> getJobs() {
		return jobs;
	}

	public void setJobs(final Set<Job> jobs) {
		this.jobs = jobs;
	}

}
