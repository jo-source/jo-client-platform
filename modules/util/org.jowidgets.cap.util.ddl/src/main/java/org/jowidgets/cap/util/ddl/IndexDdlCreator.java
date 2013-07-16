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

package org.jowidgets.cap.util.ddl;

import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinColumn;
import javax.persistence.Persistence;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;

import org.jowidgets.cap.service.jpa.api.ddl.UpperIndex;

public final class IndexDdlCreator {

	private IndexDdlCreator() {}

	public static String createIndexDdl(final String persistenceUnitName) {
		final StringBuilder builder = new StringBuilder();

		final EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);

		for (final EntityType<?> entityType : emf.getMetamodel().getEntities()) {
			final Class<?> javaType = entityType.getJavaType();
			final Table table = javaType.getAnnotation(Table.class);
			final String tableName = table.name();
			final Field[] fields = javaType.getDeclaredFields();
			for (final Field field : fields) {
				final UpperIndex annotationUpper = field.getAnnotation(UpperIndex.class);
				if (null != annotationUpper) {
					final Column column = field.getAnnotation(Column.class);
					String columnName = null;
					if (null != column) {
						columnName = column.name();

					}
					else {
						final JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
						if (joinColumn != null) {
							columnName = joinColumn.name();
						}
					}
					if (columnName != null) {
						final boolean unique = annotationUpper.unique();
						final String indexName = annotationUpper.name();
						builder.append("CREATE ");
						if (unique) {
							builder.append("UNIQUE ");
						}
						builder.append("INDEX " + indexName + " ON " + tableName + "(UPPER(" + columnName + "));\n");
					}
				}

			}
		}

		return builder.toString();
	}
}
