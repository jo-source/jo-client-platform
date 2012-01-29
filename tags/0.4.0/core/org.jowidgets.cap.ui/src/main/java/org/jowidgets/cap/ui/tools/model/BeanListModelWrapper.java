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

package org.jowidgets.cap.ui.tools.model;

import java.util.ArrayList;
import java.util.Collection;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IBeanListModelListener;

public class BeanListModelWrapper<BEAN_TYPE> implements IBeanListModel<BEAN_TYPE> {

	private final IBeanListModel<BEAN_TYPE> beanListModel;

	public BeanListModelWrapper(final IBeanListModel<BEAN_TYPE> beanListModel) {
		this.beanListModel = beanListModel;
	}

	@Override
	public void addBeanListModelListener(final IBeanListModelListener listener) {
		beanListModel.addBeanListModelListener(listener);
	}

	@Override
	public void removeBeanListModelListener(final IBeanListModelListener listener) {
		beanListModel.removeBeanListModelListener(listener);
	}

	@Override
	public int getSize() {
		return beanListModel.getSize();
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getBean(final int index) {
		return beanListModel.getBean(index);
	}

	@Override
	public void removeBeans(final Collection<? extends IBeanProxy<BEAN_TYPE>> beans) {
		beanListModel.removeBeans(beans);
	}

	@Override
	public void addBean(final IBeanProxy<BEAN_TYPE> bean) {
		beanListModel.addBean(bean);
	}

	@Override
	public ArrayList<Integer> getSelection() {
		return beanListModel.getSelection();
	}

	@Override
	public void setSelection(final Collection<Integer> selection) {
		beanListModel.setSelection(selection);
	}

	@Override
	public void fireBeansChanged() {
		beanListModel.fireBeansChanged();
	}

}
