/*
 * Copyright (c) 2017, grossmann
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

package org.jowidgets.cap.ui.impl.widgets;

import java.util.ArrayList;
import java.util.List;

import org.jowidgets.api.model.item.ICheckedItemModel;
import org.jowidgets.api.model.item.IMenuItemModel;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IFrame;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.attribute.Attribute;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeBuilder;
import org.jowidgets.cap.ui.api.table.BeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.tools.table.BeanTableMenuInterceptorAdapter;
import org.jowidgets.cap.ui.tools.widgets.CBPF;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.types.VirtualKey;
import org.jowidgets.common.widgets.controller.IKeyEvent;
import org.jowidgets.spi.impl.dummy.tools.DummyKeyEvent;
import org.jowidgets.spi.impl.dummy.ui.UIDContainer;
import org.jowidgets.test.tools.TestToolkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public class BeanTableTest {

	private static final IBluePrintFactory BPF = Toolkit.getBluePrintFactory();

	private IFrame rootFrame;

	@Before
	public void setUp() {
		TestToolkit.setUpToolkitBeforeTest();
		rootFrame = Toolkit.createRootFrame(BPF.frame());
		rootFrame.setVisible(true);
	}

	@After
	public void tearDown() {
		rootFrame.dispose();
		TestToolkit.tearDownToolkitAfterTest();
	}

	@Test
	public void testCreateBeanTable() {
		final IBeanTable<ITestBean> beanTable = rootFrame.add(createBeanTableBluePrint());
		Assert.assertTrue(beanTable.isVisible());
	}

	@Test
	public void testSearchFilterToolbarEnabledByDefault() {
		final IBeanTable<ITestBean> beanTable = rootFrame.add(createBeanTableBluePrint());
		Assert.assertTrue(beanTable.isSearchFilterToolbarEnabled());
	}

	@Test
	public void testEnableSearchFilterToolbarWithKeyEvent() {
		final IBeanTableBluePrint<ITestBean> tableBp = createBeanTableBluePrint();

		tableBp.setStatusBarVisible(false);

		final IBeanTable<ITestBean> beanTable = rootFrame.add(tableBp);
		Assert.assertFalse(beanTable.getConfig().isSearchFilterToolbarVisible());

		final UIDContainer uiReference = (UIDContainer) beanTable.getUiReference();
		final IKeyEvent keyEvent = new DummyKeyEvent(VirtualKey.F, Modifier.CTRL);
		uiReference.keyPressed(keyEvent);
		uiReference.keyReleased(keyEvent);
		Assert.assertTrue(beanTable.getConfig().isSearchFilterToolbarVisible());
	}

	@Test
	public void testInterceptSearchFilterToolbarActions() {
		final IBeanTableBluePrint<ITestBean> tableBp = createBeanTableBluePrint();

		final SearchFilterItemsCheckInterceptor menuInterceptor = new SearchFilterItemsCheckInterceptor();
		tableBp.addMenuInterceptor(menuInterceptor);

		rootFrame.add(tableBp);

		Assert.assertTrue(menuInterceptor.hasAllSearchFilterItems());
	}

	@Test
	public void testDisableSearchFilterToolbar() {
		final IBeanTableBluePrint<ITestBean> tableBp = createBeanTableBluePrint();
		tableBp.setStatusBarVisible(true);
		tableBp.setSearchFilterToolbarEnabled(false);

		final SearchFilterItemsCheckInterceptor menuInterceptor = new SearchFilterItemsCheckInterceptor();
		tableBp.addMenuInterceptor(menuInterceptor);

		final IBeanTable<ITestBean> beanTable = rootFrame.add(tableBp);

		Assert.assertFalse(beanTable.isSearchFilterToolbarEnabled());
		Assert.assertFalse(beanTable.getConfig().isSearchFilterToolbarVisible());
		Assert.assertTrue(menuInterceptor.hasNoSearchFilterItems());

		beanTable.setSearchFilterToolbarVisible(true);
		Assert.assertFalse(beanTable.getConfig().isSearchFilterToolbarVisible());

		beanTable.getSearchFilterToolbarItemModel().setEnabled(true);
		Assert.assertFalse(beanTable.getConfig().isSearchFilterToolbarVisible());

		final UIDContainer uiReference = (UIDContainer) beanTable.getUiReference();
		final IKeyEvent keyEvent = new DummyKeyEvent(VirtualKey.F, Modifier.CTRL);
		uiReference.keyPressed(keyEvent);
		uiReference.keyReleased(keyEvent);
		Assert.assertFalse(beanTable.getConfig().isSearchFilterToolbarVisible());
	}

	private static IBeanTableBluePrint<ITestBean> createBeanTableBluePrint() {
		return CBPF.beanTable(createTableModel());
	}

	private static IBeanTableModel<ITestBean> createTableModel() {
		final IBeanTableModelBuilder<ITestBean> builder = BeanTableModel.builder(ITestBean.class);
		builder.setAttributes(createAttributes());
		return builder.build();
	}

	private static List<IAttribute<?>> createAttributes() {
		final List<IAttribute<?>> result = new ArrayList<IAttribute<?>>(1);
		result.add(createValueAttribute());
		return result;
	}

	private static IAttribute<String> createValueAttribute() {
		final IAttributeBuilder<String> builder = Attribute.builder(String.class);
		builder.setPropertyName("value");
		builder.setLabel("Value");
		return builder.build();
	}

	private static interface ITestBean extends IBeanDto {
		String getValue();
	}

	private static final class SearchFilterItemsCheckInterceptor extends BeanTableMenuInterceptorAdapter<ITestBean> {

		private Boolean searchFilterItemInFilterMenu;
		private Boolean searchFilterItemInFilterCellMenu;
		private Boolean searchFilterItemInFilterHeaderMenu;

		@Override
		public IMenuModel filterMenu(final IBeanTable<?> table, final IMenuModel menuModel) {
			searchFilterItemInFilterMenu = Boolean.valueOf(hasEnableSearchFilterItem(table, menuModel));

			//TODO to make this interceptions work, dummy table must support popup events and popupmenu
			//so assume the other menus has the same interception than this menu!!!
			searchFilterItemInFilterCellMenu = searchFilterItemInFilterMenu;
			searchFilterItemInFilterHeaderMenu = searchFilterItemInFilterMenu;
			return super.filterMenu(table, menuModel);
		}

		@Override
		public IMenuModel filterCellMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
			searchFilterItemInFilterCellMenu = Boolean.valueOf(hasEnableSearchFilterItem(table, menuModel));
			return super.filterCellMenu(table, columnIndex, menuModel);
		}

		@Override
		public IMenuModel filterHeaderMenu(final IBeanTable<?> table, final int columnIndex, final IMenuModel menuModel) {
			searchFilterItemInFilterHeaderMenu = Boolean.valueOf(hasEnableSearchFilterItem(table, menuModel));
			return super.filterHeaderMenu(table, columnIndex, menuModel);
		}

		boolean hasInterception() {
			return searchFilterItemInFilterMenu != null
				&& searchFilterItemInFilterCellMenu != null
				&& searchFilterItemInFilterHeaderMenu != null;
		}

		boolean hasNoSearchFilterItems() {
			if (!hasInterception()) {
				throw new AssertionFailedError("Interceptor was not yet invoked");
			}
			else {
				return !(searchFilterItemInFilterCellMenu.booleanValue()
					|| searchFilterItemInFilterCellMenu.booleanValue()
					|| searchFilterItemInFilterHeaderMenu.booleanValue());
			}
		}

		boolean hasAllSearchFilterItems() {
			if (!hasInterception()) {
				throw new AssertionFailedError("Interceptor was not yet invoked");
			}
			else {
				return searchFilterItemInFilterCellMenu.booleanValue()
					&& searchFilterItemInFilterCellMenu.booleanValue()
					&& searchFilterItemInFilterHeaderMenu.booleanValue();
			}
		}

		private boolean hasEnableSearchFilterItem(final IBeanTable<?> table, final IMenuModel menuModel) {
			final ICheckedItemModel toolbarItemModel = table.getSearchFilterToolbarItemModel();
			for (final IMenuItemModel item : menuModel.getChildren()) {
				if (toolbarItemModel == item) {
					return true;
				}
			}
			return false;
		}
	}

}
