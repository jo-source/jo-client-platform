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

import org.jowidgets.cap.common.api.entity.IEntityClass;
import org.jowidgets.workbench.api.ILayout;
import org.jowidgets.workbench.toolkit.api.IFolderLayoutBuilder;
import org.jowidgets.workbench.toolkit.api.ILayoutBuilder;
import org.jowidgets.workbench.toolkit.api.ISplitLayoutBuilder;
import org.jowidgets.workbench.tools.FolderLayoutBuilder;
import org.jowidgets.workbench.tools.LayoutBuilder;
import org.jowidgets.workbench.tools.SplitLayoutBuilder;

public class EntityComponentMasterDetailLayout {

	public static final String DEFAULT_LAYOUT_ID = "DEFAULT_LAYOUT_ID"; //$NON-NLS-1$
	public static final String MASTER_FOLDER_ID = "MASTER_FOLDER_ID"; //$NON-NLS-1$
	public static final String DETAIL_FOLDER_ID = "DETAIL_FOLDER_ID"; //$NON-NLS-1$

	private static final String DETAIL_STRING = Messages.getString("EntityComponentDefaultLayout.detail"); //$NON-NLS-1$

	private final ILayout layout;

	public EntityComponentMasterDetailLayout(final IEntityClass entityClass) {
		final ILayoutBuilder builder = new LayoutBuilder();
		builder.setId(DEFAULT_LAYOUT_ID).setLayoutContainer(createMasterDetailSplit(entityClass));
		this.layout = builder.build();
	}

	public ILayout getLayout() {
		return layout;
	}

	private ISplitLayoutBuilder createMasterDetailSplit(final IEntityClass entityClass) {
		final ISplitLayoutBuilder result = new SplitLayoutBuilder();
		result.setVertical().setWeight(0.5).setResizeFirst();
		result.setFirstContainer(createMasterFolder(entityClass));
		result.setSecondContainer(createDetailFolder(entityClass));
		return result;
	}

	private IFolderLayoutBuilder createMasterFolder(final IEntityClass entityClass) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(MASTER_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(EntityComponent.ROOT_TABLE_VIEW_ID, entityClass.getLabel(), entityClass.getDescription()); //$NON-NLS-1$
		return result;
	}

	private IFolderLayoutBuilder createDetailFolder(final IEntityClass entityClass) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(DETAIL_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(EntityDetailView.ID, entityClass.getLabel() + " " + DETAIL_STRING, entityClass.getDescription()); //$NON-NLS-1$
		return result;
	}

}
