/*
 * Copyright (c) 2016, NBeuck
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

package org.jowidgets.cap.ui.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanDtosUpdate;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IUpdateCallback;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.common.tools.bean.BeanDtosChangeUpdate;
import org.jowidgets.cap.common.tools.bean.BeanDtosClearUpdate;
import org.jowidgets.cap.common.tools.bean.BeanDtosDeletionUpdate;
import org.jowidgets.cap.common.tools.bean.BeanDtosInsertionUpdate;
import org.jowidgets.cap.common.tools.sort.Sort;
import org.jowidgets.cap.ui.api.attribute.Attribute;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.logging.api.TestLoggerProvider;
import org.jowidgets.test.tools.TestToolkit;
import org.jowidgets.util.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BeanTableModelImplTest {

	private BeanTableModelImpl<ITestBean> tableModel;

	private IReaderService<Void> readerService;

	private final Object entityId = new Object();
	private final Object beanTypeId = new Object();

	private final TestBean bean1 = new TestBean(1, "1_foo");
	private final TestBean bean1a = new TestBean(1, "4_foo");
	private final TestBean bean2 = new TestBean(2, "2_bar");
	private final TestBean bean3 = new TestBean(3, "3_baz");

	private final Queue<Runnable> scheduledRunnables = new LinkedList<Runnable>();

	private IUpdateCallback<IBeanDtosUpdate> updateCallback;
	private final List<IExecutionCallback> updateExecutionCallbacks = new LinkedList<IExecutionCallback>();
	private IUpdateCallback<Integer> countCallback;
	private List<? extends ISort> mostRecentSorting;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		TestLoggerProvider.setConsoleLoggerForCurrentThread();
		TestToolkit.setUpToolkitBeforeTest();

		readerService = createReaderService();
		final IAttribute<?> attribute = createAttribute();
		tableModel = ((BeanTableModelImpl<BeanTableModelImplTest.ITestBean>) new BeanTableModelBuilderImpl<BeanTableModelImplTest.ITestBean>(
			entityId,
			beanTypeId,
			ITestBean.class).setReaderService(readerService).setAttributes(Arrays.asList(attribute)).build());
	}

	private IAttribute<?> createAttribute() {
		final IAttributeBuilder<Integer> attributeBuilder = Attribute.builder(Integer.class);
		attributeBuilder.setPropertyName("value");
		attributeBuilder.setLabel("value");
		final IAttribute<?> attribute = attributeBuilder.build();
		return attribute;
	}

	private void loadAllBeansAtOnce() {
		countCallback.finished(Integer.valueOf(3));

		if (!mostRecentSorting.isEmpty() && mostRecentSorting.get(0).getSortOrder().equals(SortOrder.DESC)) {
			updateCallback.finished(
					new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean3, (IBeanDto) bean2, (IBeanDto) bean1)));
		}
		else {
			updateCallback.finished(
					new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean1, (IBeanDto) bean2, (IBeanDto) bean3)));
		}
	}

	private IReaderService<Void> createReaderService() {
		return new IReaderService<Void>() {
			@Override
			public void read(
				final IUpdateCallback<IBeanDtosUpdate> result,
				final List<? extends IBeanKey> parentBeanKeys,
				final IFilter filter,
				final List<? extends ISort> sorting,
				final int firstRow,
				final int maxRows,
				final Void parameter,
				final IExecutionCallback executionCallback) {

				setUpdateCallback(result);
				setMostRecentSorting(sorting);
				addUpdateExecutionCallback(executionCallback);
			}

			@Override
			public void count(
				final IUpdateCallback<Integer> result,
				final List<? extends IBeanKey> parentBeanKeys,
				final IFilter filter,
				final Void parameter,
				final IExecutionCallback executionCallback) {

				setCountCallback(result);
			}
		};
	}

	private void setUpdateCallback(final IUpdateCallback<IBeanDtosUpdate> updateCallback) {
		this.updateCallback = updateCallback;
	}

	private void setCountCallback(final IUpdateCallback<Integer> countCallback) {
		this.countCallback = countCallback;
	}

	public void setMostRecentSorting(final List<? extends ISort> mostRecentSorting) {
		this.mostRecentSorting = mostRecentSorting;
	}

	public void addUpdateExecutionCallback(final IExecutionCallback updateExecutionCallback) {
		this.updateExecutionCallbacks.add(updateExecutionCallback);
	}

	@After
	public void tearDown() {
		TestToolkit.tearDownToolkitAfterTest();
		TestLoggerProvider.clearLoggerForCurrentThread();
	}

	@Test
	public void testLoadBeans() {
		tableModel.load();

		triggerPageLoading();
		loadAllBeansAtOnce();

		assertTrue("3 beans should be loaded", tableModel.getSize() == 3);
	}

	@Test
	public void testScheduledLoadBeans() {
		initExecutor();
		tableModel.loadScheduled(1);

		triggerPageLoading();
		scheduledRunnables.poll().run();
		loadAllBeansAtOnce();

		assertTrue("3 beans should be loaded", tableModel.getSize() == 3);
	}

	@Test
	public void testSelectionCanBeRetrieved() {
		tableModel.load();
		triggerPageLoading();
		loadAllBeansAtOnce();

		tableModel.setSelection(Arrays.asList(0, 2));
		final List<IBeanProxy<ITestBean>> selectedBeans = tableModel.getSelectedBeans();

		assertTrue("2 beans should be selected", selectedBeans.size() == 2);
		assertEquals("Bean1 should be selected!", bean1, selectedBeans.get(0).getBean());
		assertEquals("Bean3 should be selected!", bean3, selectedBeans.get(1).getBean());
	}

	@Test
	public void testSelectionSurvivesSorting() {
		tableModel.load();
		triggerPageLoading();
		loadAllBeansAtOnce();

		tableModel.setSelection(Arrays.asList(1, 2));
		tableModel.getSortModel().setCurrentSorting(Arrays.asList(new Sort("value", SortOrder.DESC)));
		triggerPageLoading();
		loadAllBeansAtOnce();

		final List<IBeanProxy<ITestBean>> selectedBeans = tableModel.getSelectedBeans();
		assertTrue("2 beans should still be selected", selectedBeans.size() == 2);
		assertEquals("Bean3 should be selected!", bean3, selectedBeans.get(0).getBean());
		assertEquals("Bean2 should be selected!", bean2, selectedBeans.get(1).getBean());
	}

	@Test
	public void testSelectionSurvivesRepeatedOverlappingSorting_Issue41() {
		tableModel.load();
		triggerPageLoading();
		loadAllBeansAtOnce();

		tableModel.setSelection(Arrays.asList(1, 2));
		tableModel.getSortModel().setCurrentSorting(Arrays.asList(new Sort("value", SortOrder.DESC)));
		tableModel.getSortModel().setCurrentSorting(Arrays.asList(new Sort("value", SortOrder.ASC)));
		triggerPageLoading();
		triggerPageLoading();
		loadAllBeansAtOnce();

		final List<IBeanProxy<ITestBean>> selectedBeans = tableModel.getSelectedBeans();
		assertTrue("2 beans should still be selected", selectedBeans.size() == 2);
		assertEquals("Bean2 should be selected!", bean2, selectedBeans.get(0).getBean());
		assertEquals("Bean3 should be selected!", bean3, selectedBeans.get(1).getBean());
	}

	@Test
	public void testLoadInBackground() {
		tableModel.updateInBackground(new Interval<Integer>(0, 10));

		triggerPageLoading();
		loadAllBeansAtOnce();

		assertTrue("3 beans should be loaded", tableModel.getSize() == 3);
	}

	@Test
	public void testBeanInsertionUpdate() {
		tableModel.load();

		triggerPageLoading();
		updateCallback.finished(new BeanDtosInsertionUpdate(new ArrayList<IBeanDto>()));
		updateCallback.update(new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean1, (IBeanDto) bean2)));

		final int size = tableModel.getSize();
		assertTrue("2 beans should be loaded, but was " + size, size == 2);
	}

	@Test
	public void testBeanDeletionUpdate() {
		tableModel.load();

		triggerPageLoading();
		updateCallback.finished(new BeanDtosInsertionUpdate(new ArrayList<IBeanDto>()));
		updateCallback.update(new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean1, (IBeanDto) bean2)));
		updateCallback.update(new BeanDtosDeletionUpdate(Arrays.asList(bean1.getId())));

		final int size = tableModel.getSize();
		assertTrue("Exactly one bean should be remaining after deletion update, but was " + size, size == 1);
	}

	@Test
	public void testBeanChangeUpdate() {
		tableModel.getSortModel().setCurrentSorting(Arrays.asList(new ISort() {
			@Override
			public SortOrder getSortOrder() {
				return SortOrder.ASC;
			}

			@Override
			public String getPropertyName() {
				return "key";
			}
		}));

		tableModel.load();

		triggerPageLoading();
		updateCallback.finished(new BeanDtosInsertionUpdate(new ArrayList<IBeanDto>()));
		updateCallback.update(new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean1, (IBeanDto) bean2)));
		updateCallback.update(new BeanDtosChangeUpdate(Arrays.asList((IBeanDto) bean1a)));

		assertTrue("2 beans should be loaded", tableModel.getSize() == 2);
		assertTrue(
				"first bean should be changed by update",
				tableModel.getBean(0).getValue("value").equals(bean1a.getValue("value")));
		assertTrue(
				"second bean should be unchanged by update",
				tableModel.getBean(1).getValue("value").equals(bean2.getValue("value")));
	}

	@Test
	public void testBeanChangeUpdateWithChangedPositionInSorting() {
		tableModel.getSortModel().setCurrentSorting(Arrays.asList(new ISort() {
			@Override
			public SortOrder getSortOrder() {
				return SortOrder.DESC;
			}

			@Override
			public String getPropertyName() {
				return "value";
			}
		}));

		tableModel.load();

		triggerPageLoading();
		updateCallback.finished(new BeanDtosInsertionUpdate(new ArrayList<IBeanDto>()));
		updateCallback.update(new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean2, (IBeanDto) bean1)));
		updateCallback.update(new BeanDtosChangeUpdate(Arrays.asList((IBeanDto) bean1a)));

		assertTrue("2 beans should be loaded, but was " + tableModel.getSize(), tableModel.getSize() == 2);
		assertTrue(
				"the updated bean should be first now",
				tableModel.getBean(0).getValue("value").equals(bean1a.getValue("value")));
		assertTrue(
				"the previously first bean should now be second",
				tableModel.getBean(1).getValue("value").equals(bean2.getValue("value")));
	}

	@Test
	public void testClearUpdateClears() {
		tableModel.load();

		triggerPageLoading();
		updateCallback.finished(new BeanDtosInsertionUpdate(new ArrayList<IBeanDto>()));
		updateCallback.update(new BeanDtosInsertionUpdate(Arrays.asList((IBeanDto) bean1, (IBeanDto) bean2)));
		updateCallback.update(new BeanDtosClearUpdate());

		final int size = tableModel.getSize();
		assertTrue("table should be clear but " + size + " bean(s) remained", size == 0);
	}

	@Test
	public void testCancelCallbackOnLoad() {
		tableModel.load();
		triggerPageLoading();

		tableModel.load();
		triggerPageLoading();

		assertTrue(updateExecutionCallbacks.size() == 2);
		assertTrue(updateExecutionCallbacks.get(0).isCanceled());
		assertFalse(updateExecutionCallbacks.get(1).isCanceled());
	}

	private void triggerPageLoading() {
		tableModel.getTableModel().getCell(0, 0);
	}

	private static interface ITestBean extends IBeanDto {
		String getValue();
	}

	private class TestBean implements ITestBean {
		private final Integer key;
		private final String value;

		TestBean(final int key, final String value) {
			this.key = Integer.valueOf(key);
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + key;
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ITestBean))
				return false;
			final ITestBean other = (ITestBean) obj;
			final Object key2 = other.getId();
			if (key != key2)
				return false;
			return true;
		}

		private BeanTableModelImplTest getOuterType() {
			return BeanTableModelImplTest.this;
		}

		@Override
		public Object getValue(final String propertyName) {
			if (propertyName.equals("key")) {
				return key;
			}
			else {
				return value;
			}
		}

		@Override
		public Object getId() {
			return key;
		}

		@Override
		public long getVersion() {
			return 0;
		}

		@Override
		public String toString() {
			return String.format("TestBean(%d, %s)", key, value);
		}
	}

	private void initExecutor() {
		tableModel.setScheduledExecutorService(new ScheduledExecutorService() {
			@Override
			public void execute(final Runnable command) {
				scheduledRunnables.add(command);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> Future<T> submit(final Runnable task, final T result) {
				scheduledRunnables.add(task);
				return ((Future<T>) createFuture(task, result));
			}

			@Override
			public Future<?> submit(final Runnable task) {
				scheduledRunnables.add(task);
				return createFuture(task, null);
			}

			@Override
			public <T> Future<T> submit(final Callable<T> task) {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<Runnable> shutdownNow() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void shutdown() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isTerminated() {
				return false;
			}

			@Override
			public boolean isShutdown() {
				return false;
			}

			@Override
			public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
					throws InterruptedException, ExecutionException {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> List<Future<T>> invokeAll(
				final Collection<? extends Callable<T>> tasks,
				final long timeout,
				final TimeUnit unit) throws InterruptedException {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
				throw new UnsupportedOperationException();
			}

			@Override
			public ScheduledFuture<?> scheduleWithFixedDelay(
				final Runnable command,
				final long initialDelay,
				final long delay,
				final TimeUnit unit) {
				scheduledRunnables.add(command);
				return createFuture(command, null);
			}

			@Override
			public ScheduledFuture<?> scheduleAtFixedRate(
				final Runnable command,
				final long initialDelay,
				final long period,
				final TimeUnit unit) {
				scheduledRunnables.add(command);
				return createFuture(command, null);
			}

			@Override
			public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
				throw new UnsupportedOperationException();
			}

			@Override
			public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
				scheduledRunnables.add(command);
				return createFuture(command, null);
			}
		});
	}

	private ScheduledFuture<?> createFuture(final Runnable task, final Object result) {
		return new ScheduledFuture<Object>() {
			boolean canceled = false;

			@Override
			public boolean cancel(final boolean mayInterruptIfRunning) {
				canceled = true;
				return scheduledRunnables.remove(task);
			}

			@Override
			public boolean isCancelled() {
				return canceled;
			}

			@Override
			public boolean isDone() {
				return scheduledRunnables.contains(task);
			}

			@Override
			public Object get() throws InterruptedException, ExecutionException {
				return result;
			}

			@Override
			public Object get(final long timeout, final TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return result;
			}

			@Override
			public long getDelay(final TimeUnit unit) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int compareTo(final Delayed o) {
				throw new UnsupportedOperationException();
			}
		};
	}

}
