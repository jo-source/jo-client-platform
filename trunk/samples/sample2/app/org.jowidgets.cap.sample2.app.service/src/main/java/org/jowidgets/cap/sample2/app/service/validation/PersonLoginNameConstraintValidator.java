/*
 * Copyright (c) 2012, grossmann
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

package org.jowidgets.cap.sample2.app.service.validation;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.jowidgets.cap.common.tools.validation.AbstractSingleConcernBeanValidator;
import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.sample2.app.service.bean.Person;
import org.jowidgets.cap.sample2.app.service.entity.EntityManagerProvider;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.ValidationResult;

public final class PersonLoginNameConstraintValidator extends AbstractSingleConcernBeanValidator<Person> {

	private static final long serialVersionUID = 3995642751853640816L;

	public PersonLoginNameConstraintValidator() {
		super(IPerson.LOGIN_NAME_PROPERTY);
	}

	@Override
	public IValidationResult validateBean(final Person bean) {
		final Person conflictedPerson = getConfictedPerson(bean.getLoginName());
		if (conflictedPerson != null && !conflictedPerson.getId().equals(bean.getId())) {
			final String conflictedPersonName = conflictedPerson.getName()
				+ " "
				+ conflictedPerson.getLastname()
				+ " ("
				+ conflictedPerson.getId()
				+ ")";
			return ValidationResult.error("The person '" + conflictedPersonName + "' has the same login name");
		}
		else {
			return ValidationResult.ok();
		}
	}

	private Person getConfictedPerson(final String loginName) {
		final EntityManager entityManager = EntityManagerProvider.get();

		//TODO MG This is a hack to avoid getting violation constraint exception on autoflush
		final FlushModeType oldFlushMode = setCommitFlushModel(entityManager);

		final TypedQuery<Person> query = entityManager.createQuery(
				"SELECT p FROM Person p WHERE p.loginName = :param",
				Person.class);
		query.setParameter("param", loginName);
		query.setFirstResult(0).setMaxResults(1);
		final List<Person> resultList = query.getResultList();

		//TODO MG This is a hack to avoid getting violation constraint exception on autoflush
		setOldFlushModel(entityManager, oldFlushMode);

		if (resultList.size() > 0) {
			return resultList.get(0);
		}
		else {
			return null;
		}
	}

	private FlushModeType setCommitFlushModel(final EntityManager entityManager) {
		final FlushModeType oldFlushMode = entityManager.getFlushMode();
		if (oldFlushMode != FlushModeType.COMMIT) {
			entityManager.setFlushMode(FlushModeType.COMMIT);
		}
		return oldFlushMode;
	}

	private void setOldFlushModel(final EntityManager entityManager, final FlushModeType oldFlushMode) {
		//TODO MG This is a hack to avoid getting violation constraint exception on autoflush
		if (oldFlushMode != FlushModeType.COMMIT) {
			entityManager.setFlushMode(oldFlushMode);
		}
	}
}
