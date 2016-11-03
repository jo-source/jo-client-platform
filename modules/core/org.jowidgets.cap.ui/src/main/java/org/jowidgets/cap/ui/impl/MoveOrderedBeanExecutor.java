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

package org.jowidgets.cap.ui.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.ordered.IOrderedBean;
import org.jowidgets.cap.common.api.sort.ISort;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.command.IMoveOrderedBeanActionBuilder.Direction;
import org.jowidgets.cap.ui.api.execution.IExecutor;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IDataModel;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.tools.command.AbstractEnabledChecker;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.event.IChangeListener;

final class MoveOrderedBeanExecutor<BEAN_TYPE extends IOrderedBean> extends AbstractEnabledChecker
		implements IExecutor<BEAN_TYPE, Void>, IEnabledChecker {

	private final IBeanListModel<BEAN_TYPE> model;
	private final IDataModel dataModel;
	private final ISortModel sortModel;
	private final Direction direction;
	private final IEnabledState moveNotPossibleSingle;
	private final IEnabledState moveNotPossibleMulti;
	private final IEnabledState moveNotPossibleWrongSorting;
	private final IEnabledState moveNotPossibleExecutionsRunning;
	private final IEnabledState moveNotPossibleUnsavedChanges;

	MoveOrderedBeanExecutor(final MoveOrderedBeanActionBuilder<BEAN_TYPE> builder) {

		Assert.paramNotNull(builder.getModel(), "builder.getModel()");
		Assert.paramNotNull(builder.getDirection(), "builder.getDirection()");

		this.model = builder.getModel();
		this.dataModel = builder.getDataModel();
		this.sortModel = builder.getSortModel();
		this.direction = builder.getDirection();

		this.moveNotPossibleSingle = createMoveNotPossibleEnabledStateSingle(direction, builder.getNotPossibleSingleMessage());

		this.moveNotPossibleMulti = createMoveNotPossibleEnabledStateMulti(direction, builder.getNotPossibleMultiMessage());

		this.moveNotPossibleWrongSorting = EnabledState.disabled(
				Messages.getString("MoveOrderedBeanExecutor.not_possible_wrong_sorting"));

		this.moveNotPossibleExecutionsRunning = EnabledState.disabled(
				Messages.getString("MoveOrderedBeanExecutor.not_possible_executions_running"));

		this.moveNotPossibleUnsavedChanges = EnabledState.disabled(
				Messages.getString("MoveOrderedBeanExecutor.not_possible_unsaved_changes"));

		model.addBeanSelectionListener(new IBeanSelectionListener<BEAN_TYPE>() {
			@Override
			public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
				fireEnabledStateChanged();
			}
		});

		if (sortModel != null) {
			sortModel.addChangeListener(new IChangeListener() {
				@Override
				public void changed() {
					fireEnabledStateChanged();
				}
			});
		}

		if (dataModel != null) {
			dataModel.addProcessStateListener(new IProcessStateListener() {
				@Override
				public void processStateChanged() {
					fireEnabledStateChanged();
				}
			});
			dataModel.addModificationStateListener(new IModificationStateListener() {
				@Override
				public void modificationStateChanged() {
					fireEnabledStateChanged();
				}
			});
		}
	}

	private static IEnabledState createMoveNotPossibleEnabledStateSingle(final Direction direction, final String configured) {
		if (!EmptyCheck.isEmpty(configured)) {
			return EnabledState.disabled(configured);
		}
		else if (Direction.UP.equals(direction)) {
			final String message = Messages.getString("MoveOrderedBeanExecutor.up_not_possible_single");
			return EnabledState.disabled(message);
		}
		else {
			final String message = Messages.getString("MoveOrderedBeanExecutor.down_not_possible_single");
			return EnabledState.disabled(message);
		}
	}

	private static IEnabledState createMoveNotPossibleEnabledStateMulti(final Direction direction, final String configured) {
		if (!EmptyCheck.isEmpty(configured)) {
			return EnabledState.disabled(configured);
		}
		else if (Direction.UP.equals(direction)) {
			final String message = Messages.getString("MoveOrderedBeanExecutor.up_not_possible_multi");
			return EnabledState.disabled(message);
		}
		else {
			final String message = Messages.getString("MoveOrderedBeanExecutor.down_not_possible_multi");
			return EnabledState.disabled(message);
		}
	}

	@Override
	public void execute(
		final IExecutionContext executionContext,
		final List<IBeanProxy<BEAN_TYPE>> beans,
		final Void defaultParameter) throws Exception {

		final ArrayList<Integer> selection = new ArrayList<Integer>(model.getSelection());
		Collections.sort(selection);

		if (isMovePossible(selection)) {
			moveSelection(selection);
			saveDataModelLater();
		}
	}

	private void moveSelection(final ArrayList<Integer> selection) {
		if (Direction.UP.equals(direction)) {
			moveSelectionUp(selection);
		}
		else {
			moveSelectionDown(selection);
		}
	}

	private void moveSelectionUp(final ArrayList<Integer> selection) {

		final int firstIndex = selection.get(0);
		final int preIndex = firstIndex - 1;

		final Long firstOrder = model.getBean(firstIndex).getBean().getOrderNumber();
		final Long preOrder = model.getBean(preIndex).getBean().getOrderNumber();

		if (firstOrder != null && preOrder != null) {
			final long diff = preOrder.longValue() - firstOrder.longValue();
			moveByDiff(selection, diff);
		}
		else {
			moveSelectionUpIterative(selection);
		}
	}

	private void moveByDiff(final ArrayList<Integer> selection, final long diff) {
		for (int i = 0; i < selection.size(); i++) {
			final Integer index = selection.get(i);
			final IOrderedBean bean = model.getBean(index).getBean();
			final Long orderNumber = bean.getOrderNumber();
			if (orderNumber != null) {
				bean.setOrderNumber(orderNumber + diff);
			}
		}
	}

	private void moveSelectionUpIterative(final ArrayList<Integer> selection) {
		for (int i = selection.size() - 1; i >= 0; i--) {
			final Integer index = selection.get(i);
			model.getBean(index).getBean().setOrderNumber(model.getBean(index - 1).getBean().getOrderNumber());
		}
	}

	private void moveSelectionDown(final ArrayList<Integer> selection) {
		final int lastIndex = selection.get(selection.size() - 1);
		final int nextIndex = lastIndex + 1;

		final Long lastOrder = model.getBean(lastIndex).getBean().getOrderNumber();
		final Long nextOrder = model.getBean(nextIndex).getBean().getOrderNumber();

		if (lastOrder != null && nextOrder != null) {
			final long diff = nextOrder.longValue() - lastOrder.longValue();
			moveByDiff(selection, diff);
		}
		else {
			moveSelectionDownIterative(selection);
		}
	}

	private void moveSelectionDownIterative(final ArrayList<Integer> selection) {
		for (int i = 0; i < selection.size(); i++) {
			final Integer index = selection.get(i);
			model.getBean(index).getBean().setOrderNumber(model.getBean(index + 1).getBean().getOrderNumber());
		}
	}

	private void saveDataModelLater() {
		if (dataModel != null) {
			Toolkit.getUiThreadAccess().invokeLater(new Runnable() {
				@Override
				public void run() {
					dataModel.save();
				}
			});
		}
	}

	@Override
	public IEnabledState getEnabledState() {
		if (dataModel != null) {
			if (dataModel.hasExecutions()) {
				return moveNotPossibleExecutionsRunning;
			}

			if (dataModel.hasModifications()) {
				return moveNotPossibleUnsavedChanges;
			}
		}

		if (sortModel != null) {
			final List<ISort> sorting = sortModel.getSorting();
			if (sorting.size() != 1 || !IOrderedBean.ORDER_NUMBER_PROPERTY.equals(sorting.get(0).getPropertyName())) {
				return moveNotPossibleWrongSorting;
			}
		}

		final ArrayList<Integer> selection = model.getSelection();
		if (!EmptyCheck.isEmpty(selection)) {
			if (!isMovePossible(selection)) {
				if (selection.size() == 1) {
					return moveNotPossibleSingle;
				}
				else {
					return moveNotPossibleMulti;
				}
			}
		}

		//do allow also for empty selection because the is another check for this
		return EnabledState.ENABLED;
	}

	private boolean isMovePossible(final ArrayList<Integer> orderedSelection) {
		if (!EmptyCheck.isEmpty(orderedSelection)) {
			if (Direction.DOWN.equals(direction)) {
				final int lastSelectedIndex = orderedSelection.get(orderedSelection.size() - 1);
				return lastSelectedIndex < model.getSize() - 1;
			}
			else if (Direction.UP.equals(direction)) {
				final int firstSelectedIndex = orderedSelection.get(0);
				return firstSelectedIndex > 0;
			}
			else {
				return true;
			}
		}
		return false;
	}

}
