/*
 * Copyright (c) 2014, grossmann
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

import java.util.Collection;

import org.jowidgets.api.clipboard.Clipboard;
import org.jowidgets.api.clipboard.IClipboardListener;
import org.jowidgets.api.command.EnabledState;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IEnabledState;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionClipboard;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.tools.command.AbstractEnabledChecker;
import org.jowidgets.util.Assert;
import org.jowidgets.util.NullCompatibleEquivalence;

final class ClipboardSelectionEnabledChecker extends AbstractEnabledChecker implements IEnabledChecker {

	private static final IMessage EMPTY_CLIPBOARD = Messages.getMessage("ClipboardSelectionEnabledChecker.empty_clipboard");

	private final Object expectedBeanTypeId;
	private final Class<?> expectedBeanType;

	private final IClipboardListener clipboardListener;

	ClipboardSelectionEnabledChecker(final Object expectedBeanTypeId, final Class<?> expectedBeanType) {

		Assert.paramNotNull(expectedBeanTypeId, "expectedBeanTypeId");
		Assert.paramNotNull(expectedBeanType, "expectedBeanType");

		this.expectedBeanTypeId = expectedBeanTypeId;
		this.expectedBeanType = expectedBeanType;

		this.clipboardListener = new IClipboardListener() {
			@Override
			public void clipboardChanged() {
				fireEnabledStateChanged();
			}
		};
		Clipboard.addClipbaordListener(clipboardListener);
	}

	@Override
	public IEnabledState getEnabledState() {
		final IBeanSelectionClipboard selection = Clipboard.getData(IBeanSelectionClipboard.TRANSFER_TYPE);
		if (selection != null) {
			final Collection<IBeanDto> beans = selection.getBeans();
			final Object selectedBeanTypeId = selection.getBeanTypeId();
			final Class<?> selectedBeanType = selection.getBeanType();
			if (beans != null
				&& !beans.isEmpty()
				&& NullCompatibleEquivalence.equals(expectedBeanTypeId, selectedBeanTypeId)
				&& NullCompatibleEquivalence.equals(expectedBeanType, selectedBeanType)) {
				return EnabledState.ENABLED;
			}
		}
		return EnabledState.disabled(EMPTY_CLIPBOARD.get());
	}

	@Override
	public void dispose() {
		super.dispose();
		Clipboard.removeClipbaordListener(clipboardListener);
	}
}
