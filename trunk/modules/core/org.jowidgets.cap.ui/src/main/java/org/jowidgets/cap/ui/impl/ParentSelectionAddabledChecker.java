/*
 * Copyright (c) 2014, MGrossmann
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

import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.bean.IBeanTransientStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.command.AbstractEnabledChecker;

final class ParentSelectionAddabledChecker extends AbstractEnabledChecker implements IEnabledChecker {

	private static final IMessage NO_PARENT_SELECTED = Messages.getMessage("ParentSelectionAddabledChecker.noParentPelected");//new StaticMessage("Es ist kein übergordneter Datensatz ausgewählt");
	private static final IMessage PARENT_IS_NOT_PERSISTENT = Messages.getMessage("ParentSelectionAddabledChecker.parentIsNotPersistent");//new StaticMessage("Der übergordnete Datensatz ist nicht gespeichert");

	private final IBeanSelectionProvider<Object> parent;
	@SuppressWarnings("unused")
	private final LinkType linkType;

	private final IBeanSelectionListener<Object> selectionListener;

	private final IBeanTransientStateListener<Object> transientStateListener;

	private IBeanProxy<Object> lastTransientBean;

	ParentSelectionAddabledChecker(final IBeanSelectionProvider<Object> parent, final LinkType linkType) {
		this.parent = parent;
		this.linkType = linkType;

		this.transientStateListener = new IBeanTransientStateListener<Object>() {
			@Override
			public void transientStateChanged(final Object oldId, final IBeanProxy<Object> newBean) {
				fireEnabledStateChanged();
			}
		};

		this.selectionListener = new IBeanSelectionListener<Object>() {
			@Override
			public void selectionChanged(final IBeanSelectionEvent<Object> selectionEvent) {

				if (lastTransientBean != null && !lastTransientBean.isDisposed()) {
					lastTransientBean.removeTransientStateListener(transientStateListener);
				}

				final IBeanProxy<Object> firstSelected = selectionEvent.getFirstSelected();
				if (firstSelected != null && firstSelected.isTransient()) {
					lastTransientBean = firstSelected;
					lastTransientBean.addTransientStateListener(transientStateListener);
				}

				fireEnabledStateChanged();
			}
		};

		if (parent != null) {
			parent.addBeanSelectionListener(selectionListener);
		}
	}

	@Override
	public IEnabledState getEnabledState() {
		if (parent == null) {
			return EnabledState.ENABLED;
		}
		else {
			final IBeanProxy<Object> firstSelected = parent.getBeanSelection().getFirstSelected();
			if (firstSelected == null || firstSelected.isLastRowDummy() || firstSelected.isDummy()) {
				return EnabledState.disabled(NO_PARENT_SELECTED.get());
			}
			else if (firstSelected.isTransient()) {
				return EnabledState.disabled(PARENT_IS_NOT_PERSISTENT.get());
			}
			else {
				return EnabledState.ENABLED;
			}
		}
	}

	@Override
	public void dispose() {
		if (parent != null) {
			parent.addBeanSelectionListener(selectionListener);
		}
		if (lastTransientBean != null && !lastTransientBean.isDisposed()) {
			lastTransientBean.removeTransientStateListener(transientStateListener);
		}
		super.dispose();
	}
}
