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

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.types.AutoPackPolicy;
import org.jowidgets.api.widgets.blueprint.defaults.IDefaultInitializer;
import org.jowidgets.cap.ui.api.types.AutoScrollPolicy;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSetupBuilder;
import org.jowidgets.common.types.TableSelectionPolicy;
import org.jowidgets.tools.widgets.blueprint.BPF;

final class BeanTableDefaults implements IDefaultInitializer<IBeanTableSetupBuilder<?, ?>> {

	@Override
	public void initialize(final IBeanTableSetupBuilder<?, ?> bluePrint) {
		bluePrint.setSelectionPolicy(TableSelectionPolicy.MULTI_ROW_SELECTION);
		bluePrint.setColumnsMoveable(true);
		bluePrint.setColumnsResizeable(true);
		bluePrint.setDefaultMenus(true);
		bluePrint.setDefaultCreatorAction(true);
		bluePrint.setDefaultDeleterAction(true);
		bluePrint.setDefaultEditAction(true);
		bluePrint.setDefaultCopyAction(false);
		bluePrint.setDefaultPasteAction(false);
		bluePrint.setDefaultMoveOrderedBeanAction(false);
		bluePrint.setEditable(false);
		bluePrint.setSearchFilterToolbarVisible(false);
		bluePrint.setStatusBarVisible(true);
		bluePrint.setAutoUpdateInterval(1);
		bluePrint.setAutoScrollPolicy(AutoScrollPolicy.OFF);
		bluePrint.setAutoPackPolicy(AutoPackPolicy.OFF);
		bluePrint.setAutoUpdateConfigurable(false);
		bluePrint.setAutoKeyBinding(true);
		bluePrint.setValidationLabel(BPF.validationResultLabel().setEmptyIcon(IconsSmall.OK_GREYED));
		bluePrint.setValidationLabelVisible(false);
	}
}
