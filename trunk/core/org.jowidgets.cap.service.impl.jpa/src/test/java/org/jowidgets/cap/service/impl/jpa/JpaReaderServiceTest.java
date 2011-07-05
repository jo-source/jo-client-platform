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
package org.jowidgets.cap.service.impl.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.filter.ArithmeticOperator;
import org.jowidgets.cap.common.api.filter.BooleanOperator;
import org.jowidgets.cap.common.api.filter.IArithmeticFilter;
import org.jowidgets.cap.common.api.filter.IBooleanFilter;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.common.tools.bean.BeanKey;
import org.jowidgets.cap.common.tools.execution.SyncResultCallback;
import org.jowidgets.cap.service.impl.jpa.entity.Job;
import org.jowidgets.cap.service.impl.jpa.entity.Person;
import org.jowidgets.cap.service.impl.jpa.jpql.CriteriaQueryCreator;
import org.jowidgets.cap.service.impl.jpa.jpql.IPredicateCreator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JpaReaderServiceTest extends AbstractJpaTest {

	private static final IFilter ALL = new IBooleanFilter() {
		@Override
		public boolean isInverted() {
			return false;
		}

		@Override
		public BooleanOperator getOperator() {
			return BooleanOperator.AND;
		}

		@Override
		public List<IFilter> getFilters() {
			return Collections.emptyList();
		}
	};

	private JpaReaderService<Object> allPersonsReader;
	private JpaReaderService<Object> allJobsReader;
	private JpaReaderService<Object> currentUserJobsReader;
	private String currentUser;

	@Before
	public void setUp() {
		final EntityManager entityManager = createEntityManager();

		final List<String> personPropertyNames = new ArrayList<String>();
		personPropertyNames.add("name");
		personPropertyNames.add("points");
		personPropertyNames.add("triState");
		personPropertyNames.add("birthday");
		personPropertyNames.add("jobTitles");
		final CriteriaQueryCreator allPersonsQueryCreator = new CriteriaQueryCreator(Person.class);
		allPersonsReader = new JpaReaderService<Object>(allPersonsQueryCreator, personPropertyNames);
		allPersonsReader.setEntityManager(entityManager);

		final List<String> jobPropertyNames = new ArrayList<String>();
		personPropertyNames.add("title");
		personPropertyNames.add("salary");
		personPropertyNames.add("personName");
		final CriteriaQueryCreator allJobsQueryCreator = new CriteriaQueryCreator(Job.class);
		allJobsQueryCreator.setParentPropertyName("owner");
		allJobsReader = new JpaReaderService<Object>(allJobsQueryCreator, jobPropertyNames);
		allJobsReader.setEntityManager(entityManager);

		currentUser = null;
		final CriteriaQueryCreator currentUserJobsQueryCreator = new CriteriaQueryCreator(Job.class);
		currentUserJobsQueryCreator.setParentPropertyName("owner");
		currentUserJobsQueryCreator.setPredicateCreator(new IPredicateCreator() {
			@Override
			public Predicate createPredicate(
				final CriteriaBuilder criteriaBuilder,
				final Root<?> bean,
				final CriteriaQuery<?> query) {
				return criteriaBuilder.equal(bean.get("owner").get("name"), currentUser);
			}
		});
		currentUserJobsReader = new JpaReaderService<Object>(currentUserJobsQueryCreator, jobPropertyNames);
		currentUserJobsReader.setEntityManager(entityManager);
	}

	@Test
	public void testCountAllPersons() {
		final SyncResultCallback<Integer> result = new SyncResultCallback<Integer>();
		allPersonsReader.count(result, null, ALL, null, null);
		final int num = result.getResultSynchronious();
		Assert.assertEquals(3, num);
	}

	@Test
	public void testReadAllPersons() {
		final SyncResultCallback<List<IBeanDto>> result = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(result, null, ALL, null, 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = result.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(3, dtos.size());
	}

	@Test
	public void testReadAllPersonsWithPaging() {
		final Set<String> expected = new HashSet<String>();
		expected.add("Harald");
		expected.add("Ingo");
		expected.add("Jan");
		final Set<String> result = new HashSet<String>();
		for (int i = 0; i < 3; i++) {
			final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
			allPersonsReader.read(res, null, ALL, null, i, 1, null, null);
			final List<IBeanDto> dtos = res.getResultSynchronious();
			Assert.assertNotNull(dtos);
			Assert.assertEquals(1, dtos.size());
			result.add((String) dtos.get(0).getValue("name"));
		}
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testReadAllPersonsSortedByNameAsc() {
		final List<String> expected = new ArrayList<String>();
		expected.add("Harald");
		expected.add("Ingo");
		expected.add("Jan");
		final List<String> result = new ArrayList<String>();
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(res, null, ALL, Collections.singletonList(new ISort() {
			@Override
			public String getPropertyName() {
				return "name";
			}

			@Override
			public SortOrder getSortOrder() {
				// TODO Auto-generated method stub
				return SortOrder.ASC;
			}
		}), 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		for (final IBeanDto dto : dtos) {
			result.add((String) dto.getValue("name"));
		}
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testReadAllPersonsSortedByNameDesc() {
		final List<String> expected = new ArrayList<String>();
		expected.add("Jan");
		expected.add("Ingo");
		expected.add("Harald");
		final List<String> result = new ArrayList<String>();
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(res, null, ALL, Collections.singletonList(new ISort() {
			@Override
			public String getPropertyName() {
				return "name";
			}

			@Override
			public SortOrder getSortOrder() {
				return SortOrder.DESC;
			}
		}), 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		for (final IBeanDto dto : dtos) {
			result.add((String) dto.getValue("name"));
		}
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testReadAllPersonsForJobPrefix() {
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(res, null, new IArithmeticFilter() {
			@Override
			public boolean isInverted() {
				return false;
			}

			@Override
			public String getPropertyName() {
				return "jobTitles";
			}

			@Override
			public ArithmeticOperator getOperator() {
				return ArithmeticOperator.EQUAL;
			}

			@Override
			public Object[] getParameters() {
				return new Object[] {"Soft*"};
			}
		}, null, 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
	}

	@Test
	public void testReadAllJobsForPersonPrefix() {
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allJobsReader.read(res, null, new IArithmeticFilter() {
			@Override
			public boolean isInverted() {
				return false;
			}

			@Override
			public String getPropertyName() {
				return "personName";
			}

			@Override
			public ArithmeticOperator getOperator() {
				return ArithmeticOperator.EQUAL;
			}

			@Override
			public Object[] getParameters() {
				return new Object[] {"Ha*"};
			}
		}, null, 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());
	}

	@Test
	public void testReadParentJobs() {
		SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allJobsReader.read(res, Collections.singletonList(new BeanKey(1L, 0)), ALL, null, 0, Integer.MAX_VALUE, null, null);
		List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());
		res = new SyncResultCallback<List<IBeanDto>>();
		allJobsReader.read(res, Collections.singletonList(new BeanKey(4L, 0)), ALL, null, 0, Integer.MAX_VALUE, null, null);
		dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(0, dtos.size());
	}

	@Test
	public void testReadCurrentUsersJobs() {
		SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		currentUserJobsReader.read(res, null, ALL, null, 0, Integer.MAX_VALUE, null, null);
		List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(0, dtos.size());

		currentUser = "Harald";
		res = new SyncResultCallback<List<IBeanDto>>();
		currentUserJobsReader.read(res, null, ALL, null, 0, Integer.MAX_VALUE, null, null);
		dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());

		currentUser = "Ingo";
		res = new SyncResultCallback<List<IBeanDto>>();
		currentUserJobsReader.read(res, null, ALL, null, 0, Integer.MAX_VALUE, null, null);
		dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(0, dtos.size());
	}

	@Test
	@Ignore
	public void testReadAllPersonsWithoutJob() {
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(res, null, new IArithmeticFilter() {
			@Override
			public boolean isInverted() {
				return false;
			}

			@Override
			public String getPropertyName() {
				return "jobTitles";
			}

			@Override
			public ArithmeticOperator getOperator() {
				return ArithmeticOperator.EMPTY;
			}

			@Override
			public Object[] getParameters() {
				return new Object[0];
			}
		}, null, 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(2, dtos.size());
	}

	@Test
	public void testReadAllPersonsWithAnyJob() {
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(res, null, new IArithmeticFilter() {
			@Override
			public boolean isInverted() {
				return true;
			}

			@Override
			public String getPropertyName() {
				return "jobTitles";
			}

			@Override
			public ArithmeticOperator getOperator() {
				return ArithmeticOperator.EMPTY;
			}

			@Override
			public Object[] getParameters() {
				return new Object[0];
			}
		}, null, 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
	}

	@Test
	public void testReadAllPersonsWithAnyJobWildcard() {
		final SyncResultCallback<List<IBeanDto>> res = new SyncResultCallback<List<IBeanDto>>();
		allPersonsReader.read(res, null, new IArithmeticFilter() {
			@Override
			public boolean isInverted() {
				return false;
			}

			@Override
			public String getPropertyName() {
				return "jobTitles";
			}

			@Override
			public ArithmeticOperator getOperator() {
				return ArithmeticOperator.EQUAL;
			}

			@Override
			public Object[] getParameters() {
				return new Object[] {"%"};
			}
		}, null, 0, Integer.MAX_VALUE, null, null);
		final List<IBeanDto> dtos = res.getResultSynchronious();
		Assert.assertNotNull(dtos);
		Assert.assertEquals(1, dtos.size());
	}

}
