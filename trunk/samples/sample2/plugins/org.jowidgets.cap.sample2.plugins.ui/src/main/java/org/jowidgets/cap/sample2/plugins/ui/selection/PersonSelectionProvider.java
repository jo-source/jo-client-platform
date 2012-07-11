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

package org.jowidgets.cap.sample2.plugins.ui.selection;

import org.jowidgets.cap.sample2.app.common.bean.IPerson;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.plugin.IBeanSelectionProviderPlugin;
import org.jowidgets.plugin.api.IPluginProperties;

public final class PersonSelectionProvider implements IBeanSelectionProviderPlugin<IPerson> {

	@Override
	public void selectionChanged(final IBeanSelectionEvent<IPerson> selectionEvent, final IPluginProperties pluginProperties) {
		//CHECKSTYLE:OFF
		System.out.println("Selection plugin selected: "
			+ getPersonName(selectionEvent)
			+ " / SOURCE: "
			+ selectionEvent.getSource());
		//CHECKSTYLE:ON
	}

	private String getPersonName(final IBeanSelectionEvent<IPerson> selectionEvent) {
		final IBeanProxy<IPerson> selected = selectionEvent.getFirstSelected();
		if (selected == null) {
			return "nothing";
		}
		else {
			final IPerson person = selected.getBean();
			return person.getName() + " " + person.getLastname();
		}
	}

}
