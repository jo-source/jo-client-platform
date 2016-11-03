/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.ui.api.command;

import org.jowidgets.api.command.IAction;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.command.IMoveOrderedBeanActionBuilder.Direction;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.util.Assert;

public final class MoveOrderedBeanAction {

	private MoveOrderedBeanAction() {}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> builder(
		final Direction direction,
		final IBeanListModel<BEAN_TYPE> model) {
		return CapUiToolkit.actionFactory().moveOrderedBeanActionBuilder(direction, model);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> upBuilder(
		final IBeanListModel<BEAN_TYPE> model) {
		return builder(Direction.UP, model);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> downBuilder(
		final IBeanListModel<BEAN_TYPE> model) {
		return builder(Direction.DOWN, model);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> builder(
		final Direction direction,
		final IBeanTableModel<BEAN_TYPE> model) {
		Assert.paramNotNull(model, "model");
		final IMoveOrderedBeanActionBuilder<BEAN_TYPE> builder = builder(direction, (IBeanListModel<BEAN_TYPE>) model);
		builder.setEntityLabels(model.getEntityId());
		builder.setDataModel(model);
		builder.setSortModelProvider(model.getSortModel());
		return builder.setEntityLabels(model.getEntityId()).setDataModel(model);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> builder(
		final Direction direction,
		final IBeanTable<BEAN_TYPE> table) {
		Assert.paramNotNull(table, "table");
		return builder(direction, table.getModel()).setViewport(table);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> upBuilder(
		final IBeanTableModel<BEAN_TYPE> model) {
		return builder(Direction.UP, model);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> downBuilder(
		final IBeanTableModel<BEAN_TYPE> model) {
		return builder(Direction.DOWN, model);
	}

	public static IAction up(final IBeanTableModel<? extends IOrderedBean> model) {
		return upBuilder(model).build();
	}

	public static IAction down(final IBeanTableModel<? extends IOrderedBean> model) {
		return downBuilder(model).build();
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> upBuilder(
		final IBeanTable<BEAN_TYPE> table) {
		return builder(Direction.UP, table);
	}

	public static <BEAN_TYPE extends IOrderedBean> IMoveOrderedBeanActionBuilder<BEAN_TYPE> downBuilder(
		final IBeanTable<BEAN_TYPE> table) {
		return builder(Direction.DOWN, table);
	}

	public static IAction up(final IBeanTable<? extends IOrderedBean> table) {
		return upBuilder(table).build();
	}

	public static IAction down(final IBeanTable<? extends IOrderedBean> table) {
		return downBuilder(table).build();
	}

}
