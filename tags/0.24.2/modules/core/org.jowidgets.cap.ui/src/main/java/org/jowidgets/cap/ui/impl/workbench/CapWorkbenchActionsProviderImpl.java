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

package org.jowidgets.cap.ui.impl.workbench;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.command.ICapActionFactory;
import org.jowidgets.cap.ui.api.command.IDataModelAction;
import org.jowidgets.cap.ui.api.command.IDataModelActionBuilder;
import org.jowidgets.cap.ui.api.command.IRefreshLookUpsActionBuilder;
import org.jowidgets.cap.ui.api.plugin.IWorkbenchMenuInterceptorPlugin;
import org.jowidgets.cap.ui.api.workbench.ICapWorkbenchActionsProvider;
import org.jowidgets.plugin.api.PluginProvider;
import org.jowidgets.util.ITypedKey;

final class CapWorkbenchActionsProviderImpl implements ICapWorkbenchActionsProvider {

	private static final ITypedKey<IDataModelAction> LOAD_ACTION_KEY = new ITypedKey<IDataModelAction>() {};
	private static final ITypedKey<IDataModelAction> SAVE_ACTION_KEY = new ITypedKey<IDataModelAction>() {};
	private static final ITypedKey<IDataModelAction> UNDO_ACTION_KEY = new ITypedKey<IDataModelAction>() {};
	private static final ITypedKey<IDataModelAction> CANCEL_ACTION_KEY = new ITypedKey<IDataModelAction>() {};
	private static final ITypedKey<IAction> REFRESH_LOOKUPS_ACTION_KEY = new ITypedKey<IAction>() {};

	private final ICapActionFactory actionFactory;

	CapWorkbenchActionsProviderImpl() {
		this.actionFactory = CapUiToolkit.actionFactory();
	}

	@Override
	public IDataModelAction loadAction() {
		IDataModelAction result = Toolkit.getValue(LOAD_ACTION_KEY);
		if (result == null) {
			IDataModelActionBuilder builder = actionFactory.dataModelLoadActionBuilder();
			for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
				builder = plugin.getMenuInterceptor().loadAction(builder);
				if (builder == null) {
					break;
				}
			}
			if (builder != null) {
				result = builder.build();
			}
			else {
				result = null;
			}
			Toolkit.setValue(LOAD_ACTION_KEY, result);
		}
		return result;
	}

	@Override
	public IDataModelAction saveAction() {
		IDataModelAction result = Toolkit.getValue(SAVE_ACTION_KEY);
		if (result == null) {
			IDataModelActionBuilder builder = actionFactory.dataModelSaveActionBuilder();
			for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
				builder = plugin.getMenuInterceptor().saveAction(builder);
				if (builder == null) {
					break;
				}
			}
			if (builder != null) {
				result = builder.build();
			}
			else {
				result = null;
			}
			Toolkit.setValue(SAVE_ACTION_KEY, result);
		}
		return result;
	}

	@Override
	public IDataModelAction undoAction() {
		IDataModelAction result = Toolkit.getValue(UNDO_ACTION_KEY);
		if (result == null) {
			IDataModelActionBuilder builder = actionFactory.dataModelUndoActionBuilder();
			for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
				builder = plugin.getMenuInterceptor().undoAction(builder);
				if (builder == null) {
					break;
				}
			}
			if (builder != null) {
				result = builder.build();
			}
			else {
				result = null;
			}
			Toolkit.setValue(UNDO_ACTION_KEY, result);
		}
		return result;
	}

	@Override
	public IDataModelAction cancelAction() {
		IDataModelAction result = Toolkit.getValue(CANCEL_ACTION_KEY);
		if (result == null) {
			IDataModelActionBuilder builder = actionFactory.dataModelCancelActionBuilder();
			for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
				builder = plugin.getMenuInterceptor().cancelAction(builder);
				if (builder == null) {
					break;
				}
			}
			if (builder != null) {
				result = builder.build();
			}
			else {
				result = null;
			}
			Toolkit.setValue(CANCEL_ACTION_KEY, result);
		}
		return result;
	}

	@Override
	public IAction refreshLookUpsAction() {
		IAction result = Toolkit.getValue(REFRESH_LOOKUPS_ACTION_KEY);
		if (result == null) {
			IRefreshLookUpsActionBuilder builder = actionFactory.refreshLookUpsActionBuilder();
			for (final IWorkbenchMenuInterceptorPlugin plugin : PluginProvider.getPlugins(IWorkbenchMenuInterceptorPlugin.ID)) {
				builder = plugin.getMenuInterceptor().refreshLookUpsAction(builder);
				if (builder == null) {
					break;
				}
			}
			if (builder != null) {
				result = builder.build();
			}
			else {
				result = null;
			}
			Toolkit.setValue(REFRESH_LOOKUPS_ACTION_KEY, result);
		}
		return result;
	}
}
