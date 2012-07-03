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

package org.jowidgets.cap.service.hibernate.oracle.impl;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;

import org.hibernate.exception.ConstraintViolationException;
import org.jowidgets.cap.common.api.exception.ForeignKeyConstraintViolationException;
import org.jowidgets.cap.common.api.exception.UniqueConstraintViolationException;
import org.jowidgets.cap.service.jpa.api.EntityManagerHolder;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IDecorator;

public class HibernateOracleExceptionDecoratorImpl implements IDecorator<Throwable> {

	@Override
	public Throwable decorate(final Throwable original) {

		if (original instanceof PersistenceException) {
			final PersistenceException persistenceException = (PersistenceException) original;
			final Throwable cause = persistenceException.getCause();

			if (cause instanceof ConstraintViolationException) {
				final String constraintName = getConstraintName((ConstraintViolationException) cause);
				if (!EmptyCheck.isEmpty(constraintName)) {
					final ConstraintType constraintType = getConstraintType(constraintName);
					if (constraintType == ConstraintType.FK) {
						return new ForeignKeyConstraintViolationException();
					}
					else if (constraintType == ConstraintType.UNIQUE) {
						final List<String> violatedProperties = getViolatedProperties(constraintName);
						if (!EmptyCheck.isEmpty(violatedProperties)) {
							return new UniqueConstraintViolationException(violatedProperties);
						}
					}
				}
			}
		}

		return original;
	}

	private String getConstraintName(final ConstraintViolationException exception) {
		final String result = exception.getConstraintName();
		if (!EmptyCheck.isEmpty(result)) {
			final int separatorIndex = result.indexOf(".");
			if (separatorIndex != -1) {
				return result.substring(separatorIndex + 1);
			}
			else {
				return result;
			}
		}
		else {
			return getConstraintName(exception.getSQLException());
		}
	}

	private String getConstraintName(final SQLException sqlException) {
		if (sqlException != null) {
			return extractConstraintName(sqlException.getLocalizedMessage());
		}
		else {
			return null;
		}
	}

	private String extractConstraintName(final String message) {
		if (message != null) {
			final int dotIndex = message.indexOf(".");
			if (dotIndex != -1) {
				final String suffix = message.substring(dotIndex + 1);
				final int bracketIndex = suffix.indexOf(")");
				if (bracketIndex != -1) {
					return suffix.substring(0, bracketIndex);
				}
			}
		}
		return null;
	}

	private ConstraintType getConstraintType(final String constraintName) {
		final EntityManager em = EntityManagerHolder.get();
		final String sql = "select CONSTRAINT_TYPE from user_constraints where constraint_name ='" + constraintName + "'";
		final Query query = em.createNativeQuery(sql);
		final Object queryResult = query.getSingleResult();

		if ("U".equals(queryResult)) {
			return ConstraintType.UNIQUE;
		}
		else if ("R".equals(queryResult)) {
			return ConstraintType.FK;
		}
		else {
			return ConstraintType.UNSUPPORTED;
		}
	}

	private List<String> getViolatedProperties(final String constraintName) {
		final List<String> result = new LinkedList<String>();
		final EntityManager em = EntityManagerHolder.get();
		final String sql = "select TABLE_NAME, COLUMN_NAME from user_cons_columns where constraint_name='" + constraintName + "'";
		final Query query = em.createNativeQuery(sql);
		@SuppressWarnings("rawtypes")
		final List resultList = query.getResultList();
		for (final Object obj : resultList) {
			if (obj instanceof Object[]) {
				final Object[] array = (Object[]) obj;
				if (array[0] instanceof String && array[1] instanceof String) {
					final String tableName = (String) array[0];
					final String columnName = (String) array[1];
					final EntityType<?> entity = getEntityForTableName(em, tableName);
					if (entity != null) {
						final String propertyName = getPropertyForColumnName(entity, columnName);
						if (!EmptyCheck.isEmpty(propertyName)) {
							result.add(propertyName);
						}
					}
				}
			}
		}
		return result;
	}

	private String getPropertyForColumnName(final EntityType<?> entity, final String columnName) {
		for (final Field field : entity.getJavaType().getDeclaredFields()) {
			if (columnName.equalsIgnoreCase(getColumnName(field))) {
				return field.getName();
			}
		}
		//TODO MG consider methods (because this only works, if annotations was made on fields)
		return null;
	}

	private String getColumnName(final Field field) {
		final Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			final String nameByAnnotaion = annotation.name();
			if (!EmptyCheck.isEmpty(nameByAnnotaion)) {
				return nameByAnnotaion;
			}
		}
		return field.getName();
	}

	private EntityType<?> getEntityForTableName(final EntityManager em, final String tableName) {
		for (final EntityType<?> entityType : em.getMetamodel().getEntities()) {
			if (tableName.equalsIgnoreCase(getTableName(entityType))) {
				return entityType;
			}
		}
		return null;
	}

	private String getTableName(final EntityType<?> entityType) {
		final Class<?> javaType = entityType.getJavaType();
		final Table annotation = javaType.getAnnotation(Table.class);
		if (annotation != null) {
			final String nameByAnnotaion = annotation.name();
			if (!EmptyCheck.isEmpty(nameByAnnotaion)) {
				return nameByAnnotaion;
			}
		}
		return javaType.getSimpleName().toUpperCase();

	}
}
