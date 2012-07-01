/*
 * Copyright (c) 2011, grossmann, Nikolaus Moll
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

package org.jowidgets.cap.sample1.ui.workbench.component.user.view;

import java.util.List;

import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.cap.sample1.common.entity.IUser;
import org.jowidgets.cap.sample1.ui.attribute.UserAttributesFactory;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.form.IBeanFormGroupBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormLayoutBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormPropertyBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.types.AlignmentVertical;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class UserDetailThreeColumnView extends AbstractView {

	public static final String ID = UserDetailThreeColumnView.class.getName();
	public static final String DEFAULT_LABEL = Messages.getString("UserDetailThreeColumnView.user_details_3columns"); //$NON-NLS-1$
	public static final String DEFAULT_TOOLTIP = Messages.getString("UserDetailThreeColumnView.user_details_3columns_tooltip"); //$NON-NLS-1$

	public UserDetailThreeColumnView(final IViewContext context, final IBeanTableModel<IUser> parentModel) {
		final IContainer container = context.getContainer();
		container.setLayout(MigLayoutFactory.growingCellLayout());
		final List<IAttribute<Object>> attributes = new UserAttributesFactory().formAttributes();
		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
		final IBeanFormBluePrint<IUser> formBp = cbpf.beanForm(IUser.class, attributes);

		final IBeanFormGroupBuilder groupBuilder = CapUiToolkit.beanFormToolkit().groupBuilder();
		for (final IAttribute<Object> attribute : attributes) {
			final IBeanFormPropertyBuilder propertyBuilder = CapUiToolkit.beanFormToolkit().propertyBuilder();
			propertyBuilder.setPropertyName(attribute.getPropertyName());
			if (attribute.getPropertyName().equals(IUser.NAME_PROPERTY)) {
				propertyBuilder.setLabelAlignmentVertical(AlignmentVertical.TOP);
				propertyBuilder.setRowCount(2);
				propertyBuilder.setRowSpan(2);
			}
			if (attribute.getPropertyName().equals(IUser.LAST_NAME_PROPERTY)) {
				propertyBuilder.setLabelAlignmentVertical(AlignmentVertical.TOP);
				propertyBuilder.setRowCount(2);
				propertyBuilder.setRowSpan(2);
			}
			if (attribute.getPropertyName().equals(IUser.GENDER_PROPERTY)) {
				propertyBuilder.setShowLabel(false);
			}

			groupBuilder.addProperty(propertyBuilder);
		}

		final IBeanFormLayoutBuilder layoutBuilder = CapUiToolkit.beanFormToolkit().layoutBuilder().setColumnCount(3).addGroup(
				groupBuilder);
		layoutBuilder.setControlMinWidth(0, 100).setControlMaxWidth(0, 200);
		layoutBuilder.setControlMinWidth(1, 100).setControlMaxWidth(1, 300);
		layoutBuilder.setControlMinWidth(2, 100).setControlMaxWidth(2, 500);

		formBp.setLayouter(CapUiToolkit.beanFormToolkit().layouter(layoutBuilder.build()));
		container.add(cbpf.beanSelectionForm(parentModel).setBeanForm(formBp), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);
	}
}
