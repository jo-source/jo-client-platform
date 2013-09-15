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

package org.jowidgets.cap.ui.impl.workbench;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityApplicationNode;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.workbench.api.ILayout;
import org.jowidgets.workbench.toolkit.api.IFolderLayoutBuilder;
import org.jowidgets.workbench.toolkit.api.ILayoutBuilder;
import org.jowidgets.workbench.toolkit.api.ISplitLayoutBuilder;
import org.jowidgets.workbench.tools.FolderLayoutBuilder;
import org.jowidgets.workbench.tools.LayoutBuilder;
import org.jowidgets.workbench.tools.SplitLayoutBuilder;

class EntityComponentMasterDetailLayout {

	public static final String DEFAULT_LAYOUT_ID = "DEFAULT_LAYOUT_ID";
	public static final String MASTER_FOLDER_ID = "MASTER_FOLDER_ID";
	public static final String DETAIL_FOLDER_ID = "DETAIL_FOLDER_ID";

	private static final IMessage DETAIL_STRING = Messages.getMessage("EntityComponentDefaultLayout.detail");

	private final ILayout layout;

	EntityComponentMasterDetailLayout(final IEntityApplicationNode applicationNode) {
		final ILayoutBuilder builder = new LayoutBuilder();
		builder.setId(DEFAULT_LAYOUT_ID).setLayoutContainer(createMasterDetailSplit(applicationNode));
		this.layout = builder.build();
	}

	ILayout getLayout() {
		return layout;
	}

	private ISplitLayoutBuilder createMasterDetailSplit(final IEntityApplicationNode applicationNode) {
		final ISplitLayoutBuilder result = new SplitLayoutBuilder();
		result.setVertical().setWeight(0.5).setResizeFirst();
		result.setFirstContainer(createMasterFolder(applicationNode));
		result.setSecondContainer(createDetailFolder(applicationNode));
		return result;
	}

	private IFolderLayoutBuilder createMasterFolder(final IEntityApplicationNode applicationNode) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(MASTER_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(
				EntityComponent.ROOT_TABLE_VIEW_ID,
				getEntityLabel(applicationNode, true),
				applicationNode.getDescription().get());
		return result;
	}

	private IFolderLayoutBuilder createDetailFolder(final IEntityApplicationNode applicationNode) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(DETAIL_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(
				EntityDetailView.ID,
				getEntityLabel(applicationNode, false) + " " + DETAIL_STRING.get(),
				applicationNode.getDescription().get());
		return result;
	}

	private static String getEntityLabel(final IEntityApplicationNode applicationNode, final boolean plural) {
		final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
		if (entityService != null) {
			final IBeanDtoDescriptor descriptor = entityService.getDescriptor(applicationNode.getEntityId());
			if (descriptor != null) {
				if (plural) {
					return descriptor.getLabelPlural().get();
				}
				else {
					return descriptor.getLabelSingular().get();
				}
			}
		}
		return applicationNode.getLabel().get();
	}
}
