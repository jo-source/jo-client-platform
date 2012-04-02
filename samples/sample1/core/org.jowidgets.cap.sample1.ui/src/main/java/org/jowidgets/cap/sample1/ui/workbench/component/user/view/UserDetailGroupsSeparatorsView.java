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
import org.jowidgets.cap.ui.api.form.BeanFormGroupRendering;
import org.jowidgets.cap.ui.api.form.IBeanFormGroupBuilder;
import org.jowidgets.cap.ui.api.form.IBeanFormLayoutBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.tools.layout.MigLayoutFactory;
import org.jowidgets.workbench.api.IViewContext;
import org.jowidgets.workbench.tools.AbstractView;

public class UserDetailGroupsSeparatorsView extends AbstractView {

	public static final String ID = UserDetailGroupsSeparatorsView.class.getName();
	public static final String DEFAULT_LABEL = Messages.getString("UserDetailGroupsSeparatorsView.user_details_separators"); //$NON-NLS-1$
	public static final String DEFAULT_TOOLTIP = Messages.getString("UserDetailGroupsSeparatorsView.user_details_separators_tooltip"); //$NON-NLS-1$

	private static final String USER_DETAILS = Messages.getString("UserDetailGroupsSeparatorsView.user_details"); //$NON-NLS-1$
	private static final String COUNTRY_SETTINGS = Messages.getString("UserDetailGroupsSeparatorsView.country_settings"); //$NON-NLS-1$
	private static final String ADMINISTRATION = Messages.getString("UserDetailGroupsSeparatorsView.administration"); //$NON-NLS-1$

	private final boolean useLabels = true;

	public UserDetailGroupsSeparatorsView(final IViewContext context, final IBeanTableModel<IUser> parentModel) {
		final IContainer container = context.getContainer();
		container.setLayout(MigLayoutFactory.growingCellLayout());
		final List<IAttribute<Object>> attributes = new UserAttributesFactory().formAttributes();
		final ICapApiBluePrintFactory cbpf = CapUiToolkit.bluePrintFactory();
		final IBeanFormBluePrint<IUser> formBp = cbpf.beanForm(IUser.class, attributes);

		final IBeanFormLayoutBuilder layoutBuilder = CapUiToolkit.beanFormToolkit().layoutBuilder();

		final IBeanFormGroupBuilder nameGroupBuilder = CapUiToolkit.beanFormToolkit().groupBuilder().setRendering(
				BeanFormGroupRendering.SEPARATOR);
		if (useLabels) {
			nameGroupBuilder.setLabel(Messages.getString("UserDetailGroupsSeparatorsView.user_name")); //$NON-NLS-1$
		}
		nameGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(IUser.NAME_PROPERTY));
		nameGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(IUser.LAST_NAME_PROPERTY));
		layoutBuilder.addGroup(nameGroupBuilder);

		final IBeanFormGroupBuilder detailsGroupBuilder = CapUiToolkit.beanFormToolkit().groupBuilder().setRendering(
				BeanFormGroupRendering.SEPARATOR);
		if (useLabels) {
			detailsGroupBuilder.setLabel(USER_DETAILS);
		}
		detailsGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(
				IUser.DATE_OF_BIRTH_PROPERTY));
		detailsGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(IUser.GENDER_PROPERTY));
		detailsGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(IUser.MARRIED_PROPERTY));
		layoutBuilder.addGroup(detailsGroupBuilder);

		final IBeanFormGroupBuilder countryGroupBuilder = CapUiToolkit.beanFormToolkit().groupBuilder().setRendering(
				BeanFormGroupRendering.SEPARATOR);
		if (useLabels) {
			countryGroupBuilder.setLabel(COUNTRY_SETTINGS);
		}
		countryGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(IUser.COUNTRY_PROPERTY));
		countryGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(IUser.LANGUAGES_PROPERTY));
		layoutBuilder.addGroup(countryGroupBuilder);

		final IBeanFormGroupBuilder administrationGroupBuilder = CapUiToolkit.beanFormToolkit().groupBuilder().setRendering(
				BeanFormGroupRendering.SEPARATOR);
		if (useLabels) {
			administrationGroupBuilder.setLabel(ADMINISTRATION);
		}
		administrationGroupBuilder.addProperty(CapUiToolkit.beanFormToolkit().propertyBuilder().setPropertyName(
				IUser.ADMIN_PROPERTY));
		layoutBuilder.addGroup(administrationGroupBuilder);

		formBp.setLayouter(CapUiToolkit.beanFormToolkit().layouter(layoutBuilder.build()));
		container.add(cbpf.beanTableForm(parentModel).setBeanForm(formBp), MigLayoutFactory.GROWING_CELL_CONSTRAINTS);

	}
}
