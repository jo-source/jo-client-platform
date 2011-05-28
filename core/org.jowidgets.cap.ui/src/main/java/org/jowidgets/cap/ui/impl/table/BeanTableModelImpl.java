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

package org.jowidgets.cap.ui.impl.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.convert.IObjectLabelConverter;
import org.jowidgets.api.model.table.IDefaultTableColumnBuilder;
import org.jowidgets.api.model.table.IDefaultTableColumnModel;
import org.jowidgets.api.model.table.ITableCellBuilder;
import org.jowidgets.api.model.table.ITableModel;
import org.jowidgets.api.model.table.ITableModelFactory;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanModification;
import org.jowidgets.cap.common.api.execution.IExecutionTask;
import org.jowidgets.cap.common.api.filter.IFilter;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.common.api.service.IDeleterService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.common.api.service.IRefreshService;
import org.jowidgets.cap.common.api.service.IUpdaterService;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansModificationBuffer;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IReaderParameterProvider;
import org.jowidgets.common.image.IImageConstant;
import org.jowidgets.common.model.ITableCell;
import org.jowidgets.tools.model.table.AbstractTableDataModel;
import org.jowidgets.tools.model.table.DefaultTableColumnBuilder;
import org.jowidgets.tools.model.table.TableCellBuilder;
import org.jowidgets.tools.model.table.TableModel;
import org.jowidgets.util.Assert;

@SuppressWarnings("unused")
class BeanTableModelImpl<BEAN_TYPE> implements IBeanTableModel<BEAN_TYPE> {

	private static final int DEFAULT_PAGE_SIZE = 1000;
	private static final int VIEW_SIZE = 50;
	private static final int INNER_PAGE_DELAY = 100;
	private static final int MAX_PAGE_LOADER_COUNT = 2;
	private static final IDummyValue DUMMY_VALUE = new IDummyValue() {};

	private final Map<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>> data;
	private final LinkedList<PageLoader> currentPageLoaders;

	private final IBeanProxy<BEAN_TYPE> dummyBeanProxy;
	private final IBeanProxyFactory<BEAN_TYPE> beanProxyFactory;
	private final IBeansModificationBuffer<BEAN_TYPE> modificationBuffer;
	private final ArrayList<IAttribute<Object>> attributes;
	private final IReaderService<Object> readerService;
	private final IReaderParameterProvider<Object> paramProvider;

	private final ICreatorService creatorService;
	private final IRefreshService refreshService;
	private final IUpdaterService updaterService;
	private final IDeleterService deleterService;

	private final IDefaultTableColumnModel columnModel;
	private final AbstractTableDataModel dataModel;
	private final ITableModel tableModel;

	private int pageSize;
	private int rowCount;
	private int maxPageIndex;
	private int lastLoadingPage;
	private int lastRenderedRow;
	private boolean dataCleared;

	@SuppressWarnings("unchecked")
	BeanTableModelImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final List<IAttribute<Object>> attributes,
		final IReaderService<? extends Object> readerService,
		final IReaderParameterProvider<? extends Object> paramProvider,
		final ICreatorService creatorService,
		final IRefreshService refreshService,
		final IUpdaterService updaterService,
		final IDeleterService deleterService,
		final IBeanListModel<?> parent,
		final LinkType linkType) {

		//arguments checks
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(attributes, "attributes");
		Assert.paramNotNull(readerService, "readerService");
		Assert.paramNotNull(paramProvider, "paramProvider");
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
		}

		//arguments initialize
		this.attributes = new ArrayList<IAttribute<Object>>(attributes);
		this.readerService = (IReaderService<Object>) readerService;
		this.paramProvider = (IReaderParameterProvider<Object>) paramProvider;
		this.creatorService = creatorService;
		this.refreshService = refreshService;
		this.updaterService = updaterService;
		this.deleterService = deleterService;

		//fields initialize
		this.data = new HashMap<Integer, ArrayList<IBeanProxy<BEAN_TYPE>>>();
		this.currentPageLoaders = new LinkedList<PageLoader>();
		this.dataCleared = true;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.rowCount = 0;
		this.maxPageIndex = 0;
		this.modificationBuffer = CapUiToolkit.createBeansModificationBuffer();
		this.beanProxyFactory = CapUiToolkit.createBeanProxyFactory(beanType, modificationBuffer);
		this.dummyBeanProxy = beanProxyFactory.createProxy(createDummyBeanDto());
		this.dummyBeanProxy.setInProcess(true);

		//model creation
		this.columnModel = createColumnModel(attributes);
		this.dataModel = createDataModel();
		this.tableModel = new TableModel(columnModel, dataModel);
	}

	@Override
	public void clearData() {
		rowCount = 0;
		maxPageIndex = 0;
		dataCleared = true;
		data.clear();
		modificationBuffer.clear();
		dataModel.fireDataChanged();
	}

	@Override
	public void loadData() {
		modificationBuffer.clear();
		//rowCount = readerService.count(null, null, null, null);
		rowCount = 0;
		dataCleared = false;
		maxPageIndex = 0;
		data.clear();
		loadPage(0);
		dataModel.fireDataChanged();
	}

	@Override
	public void saveModifications() {
		final Set<IBeanProxy<BEAN_TYPE>> modifiedBeans = modificationBuffer.getModifiedBeans();

		final List<IBeanModification> modifications = new LinkedList<IBeanModification>();

		for (final IBeanProxy<BEAN_TYPE> bean : modifiedBeans) {
			modifications.addAll(bean.getModifications());
		}

		final List<IBeanDto> updateResult = updaterService.update(modifications, CapUiToolkit.getExecutionTaskFactory().create());
		final Map<Object, IBeanDto> updateMap = new HashMap<Object, IBeanDto>();
		for (final IBeanDto beanDto : updateResult) {
			updateMap.put(beanDto.getId(), beanDto);
		}

		for (final IBeanProxy<BEAN_TYPE> bean : new HashSet<IBeanProxy<BEAN_TYPE>>(modifiedBeans)) {
			final IBeanDto updatedBean = updateMap.get(bean.getId());
			if (updatedBean != null) {
				bean.update(updatedBean);
			}
			//TODO MG //else {}
		}

		dataModel.fireDataChanged();
	}

	@Override
	public void undoModifications() {
		for (final IBeanProxy<BEAN_TYPE> bean : new HashSet<IBeanProxy<BEAN_TYPE>>(modificationBuffer.getModifiedBeans())) {
			bean.undoModifications();
		}
		modificationBuffer.clear();
		dataModel.fireDataChanged();
	}

	@Override
	public boolean hasModifications() {
		return modificationBuffer.hasModifications();
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		modificationBuffer.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		modificationBuffer.removeModificationStateListener(listener);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean(final int rowIndex) {
		final int pageIndex = getPage(rowIndex);
		final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));
		if (page != null) {
			final int startIndex = pageIndex * pageSize;
			return page.get(rowIndex - startIndex);
		}
		else {
			return null;
		}
	}

	@Override
	public void addBean(final int index, final IBeanProxy<BEAN_TYPE> bean) {
		// TODO MG implement addBean
	}

	@Override
	public void removeBean(final int index) {
		// TODO MG implement removeBean
	}

	@Override
	public IAttribute<Object> getAttribute(final int columnIndex) {
		return attributes.get(columnIndex);
	}

	@Override
	public void addBeanListModelListener(final IBeanListModelListener listener) {}

	@Override
	public void removeBeanListModelListener(final IBeanListModelListener listener) {}

	@Override
	public ITableModel getTableModel() {
		return tableModel;
	}

	@Override
	public int getSize() {
		return rowCount;
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return tableModel.getSelection();
	}

	@Override
	public void setSelection(final List<Integer> selection) {
		tableModel.setSelection(selection);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getFirstSelectedBean() {
		final ArrayList<Integer> selection = getSelection();
		if (selection != null && selection.size() > 0) {
			return getBean(selection.get(0).intValue());
		}
		return null;
	}

	@Override
	public void setFilter(final String id, final IFilter filter) {}

	@Override
	public void setSorting(final List<? extends ISort> sorting) {}

	@Override
	public void setPageSize(final int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public void setActive(final boolean active) {}

	private int getPage(final int rowIndex) {
		return rowIndex / pageSize;
	}

	private void loadPage(final int pageIndex) {
		lastLoadingPage = pageIndex;
		final PageLoader pageLoader = new PageLoader(pageIndex);
		currentPageLoaders.add(pageLoader);
		pageLoader.loadPage();

		while (currentPageLoaders.size() > MAX_PAGE_LOADER_COUNT) {
			final PageLoader pageLoaderToRemove = currentPageLoaders.removeFirst();
			pageLoaderToRemove.cancel();
		}
	}

	private IDefaultTableColumnModel createColumnModel(final List<IAttribute<Object>> attributes) {
		final ITableModelFactory tableModelFactory = Toolkit.getModelFactoryProvider().getTableModelFactory();

		final IDefaultTableColumnModel result = tableModelFactory.columnModel();
		for (final IAttribute<Object> attribute : attributes) {
			final IDefaultTableColumnBuilder columnBuilder = new DefaultTableColumnBuilder();
			columnBuilder.setText(attribute.getLabel());
			columnBuilder.setToolTipText(attribute.getDescription());
			columnBuilder.setWidth(attribute.getTableWidth());
			columnBuilder.setAlignment(attribute.getTableAlignment());
			result.addColumn(columnBuilder);
		}

		return result;
	}

	private AbstractTableDataModel createDataModel() {
		final AbstractTableDataModel result = new AbstractTableDataModel() {

			@Override
			public int getRowCount() {
				return rowCount;
			}

			@Override
			public ITableCell getCell(final int rowIndex, final int columnIndex) {
				if (dataCleared) {
					return new TableCellBuilder().build();
				}

				lastRenderedRow = rowIndex;

				final int pageIndex = getPage(rowIndex);
				final ArrayList<IBeanProxy<BEAN_TYPE>> page = data.get(Integer.valueOf(pageIndex));

				if (page == null) {
					loadPage(pageIndex);
					return createDummyCellBuilder(rowIndex, columnIndex).build();
				}
				else {
					final IAttribute<Object> attribute = attributes.get(columnIndex);
					final int startIndex = pageIndex * pageSize;
					final IBeanProxy<BEAN_TYPE> bean = page.get(rowIndex - startIndex);
					final Object value = bean.getValue(attribute.getPropertyName());

					final ITableCellBuilder cellBuilder;
					if (value instanceof IDummyValue) {
						cellBuilder = createDummyCellBuilder(rowIndex, columnIndex);
					}
					else {
						cellBuilder = createCellBuilder(rowIndex, columnIndex, attribute, value);
					}

					if (bean.isInProcess()) {
						cellBuilder.setForegroundColor(Colors.DISABLED);
					}

					return cellBuilder.build();
				}
			}

			private ITableCellBuilder createCellBuilder(
				final int rowIndex,
				final int columnIndex,
				final IAttribute<Object> attribute,
				final Object value) {

				final ITableCellBuilder cellBuilder = createDefaultCellBuilder(rowIndex, columnIndex);
				final IObjectLabelConverter<Object> converter = attribute.getDefaultControlPanel().getObjectLabelConverter();

				String text;
				String toolTipText;
				IImageConstant icon;

				if (value instanceof Collection<?>) {
					final Collection<?> collection = (Collection<?>) value;
					final int collectionSize = collection.size();
					if (collectionSize > 0) {
						final Object firstElement = collection.iterator().next();
						if (collectionSize > 1) {
							text = converter.convertToString(firstElement) + " [" + collectionSize + "]";
						}
						else {
							text = converter.convertToString(firstElement);
						}
						toolTipText = converter.getDescription(firstElement);
					}
					else {
						text = null;
						toolTipText = null;
					}
					icon = null;
				}
				else {
					text = converter.convertToString(value);
					toolTipText = converter.getDescription(value);
					icon = converter.getIcon(value);
				}

				cellBuilder.setText(text).setToolTipText(toolTipText).setIcon(icon);
				return cellBuilder;
			}

			private ITableCellBuilder createDummyCellBuilder(final int rowIndex, final int columnIndex) {
				final ITableCellBuilder cellBuilder = createDefaultCellBuilder(rowIndex, columnIndex);
				cellBuilder.setText("...").setToolTipText("Data will be loaded in background");
				return cellBuilder;
			}

			private ITableCellBuilder createDefaultCellBuilder(final int rowIndex, final int columnIndex) {
				final ITableCellBuilder cellBuilder = new TableCellBuilder();
				if (rowIndex % 2 == 0) {
					cellBuilder.setBackgroundColor(Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR);
				}

				final IAttribute<Object> attribute = attributes.get(columnIndex);
				boolean editable = attribute.isEditable();
				editable = editable && !attribute.isCollectionType();
				editable = editable && attribute.getDefaultControlPanel().getStringObjectConverter() != null;
				cellBuilder.setEditable(editable);

				return cellBuilder;
			}
		};
		return result;
	}

	private class PageLoader {

		private final int pageIndex;
		private final IUiThreadAccess uiThreadAccess;

		private Object parameter;
		private boolean innerPage;
		private boolean canceled;
		private ArrayList<IBeanProxy<BEAN_TYPE>> page;

		private IExecutionTask executionTask;

		PageLoader(final int pageIndex) {
			this.pageIndex = pageIndex;
			this.uiThreadAccess = Toolkit.getUiThreadAccess();
		}

		void loadPage() {
			page = new ArrayList<IBeanProxy<BEAN_TYPE>>();
			data.put(Integer.valueOf(pageIndex), page);

			for (int i = 0; i < pageSize; i++) {
				page.add(dummyBeanProxy);
			}

			final int count = ((pageIndex) * pageSize) + pageSize;
			innerPage = rowCount > count;
			rowCount = Math.max(rowCount, count);

			dataModel.fireDataChanged();

			this.parameter = paramProvider.getParameter();

			final Thread thread = new Thread(createRunnable());
			thread.setDaemon(true);
			thread.start();
		}

		void cancel() {
			this.canceled = true;
			if (executionTask != null) {
				executionTask.cancel();
			}
		}

		private void removePageLater() {
			uiThreadAccess.invokeLater(new Runnable() {
				@Override
				public void run() {
					data.remove(Integer.valueOf(pageIndex));
					final int pageStart = (pageIndex) * pageSize;
					final int[] changedIndices = new int[pageSize];
					for (int i = 0; i < changedIndices.length; i++) {
						changedIndices[i] = pageStart + i;
					}
					dataModel.fireRowsChanged(changedIndices);

					currentPageLoaders.remove(this);
				}
			});
		}

		private Runnable createRunnable() {
			return new Runnable() {

				@Override
				public void run() {
					if (innerPage) {
						try {
							Thread.sleep(INNER_PAGE_DELAY);
						}
						catch (final InterruptedException e) {
							return;
						}
					}

					if (canceled) {
						removePageLater();
						return;
					}

					if (lastLoadingPage != pageIndex) {
						final int pageStart = (pageIndex) * pageSize;
						final int pageEnd = pageStart + pageSize;
						if (pageStart - VIEW_SIZE > lastRenderedRow || lastRenderedRow > pageEnd + VIEW_SIZE) {
							removePageLater();
							return;
						}
					}

					//CHECKSTYLE:OFF
					System.out.println("Load page start: " + pageIndex);
					//CHECKSTYLE:ON

					executionTask = CapUiToolkit.getExecutionTaskFactory().create();

					final List<IBeanDto> beanDtos;
					try {
						beanDtos = BeanTableModelImpl.this.readerService.read(
								null,
								null,
								null,
								pageIndex * pageSize,
								pageSize + 1,
								parameter,
								executionTask);
					}
					catch (final Exception e) {
						removePageLater();
						//TODO to proper error handling here
						throw (new RuntimeException(e));
					}
					finally {
						executionTask.dispose();
					}

					if (canceled || executionTask.isCanceled()) {
						removePageLater();
					}
					else {
						uiThreadAccess.invokeLater(new Runnable() {
							@Override
							public void run() {

								currentPageLoaders.remove(this);

								if (beanDtos.size() > pageSize && pageIndex >= maxPageIndex) {
									rowCount = Math.max(rowCount, ((pageIndex + 1) * pageSize + pageSize - 1));
									maxPageIndex = pageIndex;
								}
								else if (pageIndex >= maxPageIndex) {
									rowCount = (pageIndex * pageSize + beanDtos.size());
									maxPageIndex = pageIndex;
								}

								page.clear();
								int index = 0;
								for (final IBeanDto beanDto : beanDtos) {
									if (index < pageSize) {
										page.add(beanProxyFactory.createProxy(beanDto));
										index++;
									}
								}
								dataModel.fireDataChanged();

								//CHECKSTYLE:OFF
								System.out.println("Load page finished: " + pageIndex);
								//CHECKSTYLE:ON
							}
						});
					}

				}
			};
		}

	}

	private interface IDummyValue {}

	private static IBeanDto createDummyBeanDto() {
		return new IBeanDto() {
			@Override
			public Object getValue(final String propertyName) {
				return DUMMY_VALUE;
			}

			@Override
			public Object getId() {
				return DUMMY_VALUE;
			}

			@Override
			public long getVersion() {
				return 0;
			}
		};
	}

}
