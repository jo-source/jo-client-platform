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

package org.jowidgets.cap.ui.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyContext;
import org.jowidgets.cap.ui.tools.bean.BeanProxyListenerAdapter;
import org.jowidgets.util.Assert;
import org.jowidgets.util.Tuple;

final class BeanProxyContextImpl implements IBeanProxyContext {

	private final Map<IBeanDto, Tuple<PresentationModel, Set<Object>>> presentationModels;

	BeanProxyContextImpl() {
		this.presentationModels = new HashMap<IBeanDto, Tuple<PresentationModel, Set<Object>>>();
	}

	@Override
	public void registerBean(final IBeanProxy<?> bean, final Object owner) {
		Assert.paramNotNull(bean, "bean");
		Assert.paramNotNull(owner, "owner");
		if (!bean.isTransient() && !bean.isDummy()) {
			Tuple<PresentationModel, Set<Object>> presentationModelTuple = presentationModels.get(bean.getBeanDto());
			if (presentationModelTuple == null) {
				final Set<Object> owners = new LinkedHashSet<Object>();
				owners.add(owner);
				final PresentationModel presentationModel = new PresentationModel(bean);
				presentationModelTuple = new Tuple<PresentationModel, Set<Object>>(presentationModel, owners);
				presentationModels.put(bean.getBeanDto(), presentationModelTuple);
			}
			else {
				final PresentationModel presentationModel = presentationModelTuple.getFirst();
				presentationModel.bind(bean);
				presentationModelTuple.getSecond().add(owner);
			}
		}
	}

	@Override
	public void unregisterBean(final IBeanProxy<?> bean, final Object owner) {
		Assert.paramNotNull(bean, "bean");
		Assert.paramNotNull(owner, "owner");
		final Tuple<PresentationModel, Set<Object>> presentationModelTuple = presentationModels.get(bean.getBeanDto());
		if (presentationModelTuple != null) {
			final PresentationModel presentationModel = presentationModelTuple.getFirst();
			presentationModel.unbind(bean);
			final Set<Object> owners = presentationModelTuple.getSecond();
			owners.remove(owner);
			if (owners.size() == 0) {
				presentationModels.remove(bean.getBeanDto());
			}
		}
	}

	@Override
	public boolean isMaster(final IBeanProxy<?> bean, final Object owner) {
		Assert.paramNotNull(bean, "bean");
		Assert.paramNotNull(owner, "owner");
		final Tuple<PresentationModel, Set<Object>> presentationModelTuple = presentationModels.get(bean.getBeanDto());
		if (presentationModelTuple != null) {
			final Set<Object> owners = presentationModelTuple.getSecond();
			if (owners.size() > 0) {
				if (owners.iterator().next() == owner) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return true;
	}

	private static final class PresentationModel {

		private final Set<BeanProxyInstance> boundBeans;
		private final BindingListener bindingListener;
		private final UpdateListener updateListener;

		private PresentationModel(final IBeanProxy<?> beanProxy) {
			this.boundBeans = new LinkedHashSet<BeanProxyInstance>();
			this.bindingListener = new BindingListener();
			this.updateListener = new UpdateListener();
			bind(beanProxy);
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private void bind(final IBeanProxy bean) {
			for (final BeanProxyInstance boundBeanInstance : boundBeans) {
				final IBeanProxy boundBean = boundBeanInstance.getInstance();
				if (bean != boundBean) {
					if (!boundBean.hasModifications()) {
						boundBean.removePropertyChangeListener(bindingListener);
						boundBean.removeBeanProxyListener(updateListener);
						boundBean.update(bean.getBeanDto());
						boundBean.addBeanProxyListener(updateListener);
						boundBean.addPropertyChangeListener(bindingListener);
					}
					else {
						bean.update(boundBean.getBeanDto());
						bean.setModifications(boundBean.getModifications());
					}
				}
			}
			boundBeans.add(new BeanProxyInstance(bean));
			bean.addPropertyChangeListener(bindingListener);
			bean.addBeanProxyListener(updateListener);
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private void unbind(final IBeanProxy bean) {
			boundBeans.remove(bean);
			bean.removePropertyChangeListener(bindingListener);
			bean.addBeanProxyListener(updateListener);
		}

		private final class BindingListener implements PropertyChangeListener {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				for (final BeanProxyInstance boundBeanInstance : boundBeans) {
					final IBeanProxy<?> bean = boundBeanInstance.getInstance();
					if (bean != event.getSource()) {
						bean.removePropertyChangeListener(bindingListener);
						bean.setValue(event.getPropertyName(), event.getNewValue());
						bean.addPropertyChangeListener(bindingListener);
					}
				}
			}
		}

		private final class UpdateListener extends BeanProxyListenerAdapter<Object> {

			@Override
			public void beforeBeanUpdate(final IBeanProxy<Object> bean) {
				for (final BeanProxyInstance boundBeanInstance : boundBeans) {
					final IBeanProxy<?> boundBean = boundBeanInstance.getInstance();
					boundBean.removePropertyChangeListener(bindingListener);
				}
			}

			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public void afterBeanUpdated(final IBeanProxy<Object> bean) {
				for (final BeanProxyInstance boundBeanInstance : boundBeans) {
					final IBeanProxy boundBean = boundBeanInstance.getInstance();
					if (boundBean != bean) {
						boundBean.removeBeanProxyListener(updateListener);
						boundBean.update(bean.getBeanDto());
						boundBean.addBeanProxyListener(updateListener);
					}
					boundBean.addPropertyChangeListener(bindingListener);
				}
			}
		}
	}

	private static final class BeanProxyInstance {

		private final IBeanProxy<?> instance;

		private BeanProxyInstance(final IBeanProxy<?> instance) {
			this.instance = instance;
		}

		IBeanProxy<?> getInstance() {
			return instance;
		}

		@Override
		public int hashCode() {
			return instance.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (BeanProxyInstance.class.isAssignableFrom(obj.getClass())) {
				return instance == ((BeanProxyInstance) obj).instance;
			}
			return false;
		}

	}
}
