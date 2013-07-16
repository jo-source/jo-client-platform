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

package org.jowidgets.cap.ui.tools.plugin;

import java.util.Collection;

import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttributeCollectionModifierBuilder;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.plugin.IBeanFormPlugin;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.plugin.api.IPluginProperties;

public abstract class AbstractBeanFormPlugin implements IBeanFormPlugin {

	private final IAttributeToolkit attributeToolkit = CapUiToolkit.attributeToolkit();

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public final void modifySetup(final IPluginProperties properties, final IBeanFormBluePrint<?> bluePrint) {

		modifySetup(bluePrint);

		final IAttributeCollectionModifierBuilder globalModifier = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifyAttributes(globalModifier);

		final IAttributeCollectionModifierBuilder editModifier = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifyEditAttributes(editModifier);

		final IAttributeCollectionModifierBuilder createModifier = attributeToolkit.createAttributeCollectionModifierBuilder();
		modifyCreateAttributes(createModifier);

		Collection editModeAttributes = bluePrint.getEditModeAttributes();
		Collection createModeAttributes = bluePrint.getCreateModeAttributes();

		if (editModeAttributes == createModeAttributes) {
			editModeAttributes = modifyIfNecessary(globalModifier, editModeAttributes);
			createModeAttributes = editModeAttributes;
		}
		else {
			editModeAttributes = modifyIfNecessary(globalModifier, editModeAttributes);
			createModeAttributes = modifyIfNecessary(globalModifier, createModeAttributes);
		}

		editModeAttributes = modifyIfNecessary(editModifier, editModeAttributes);
		bluePrint.setEditModeAttributes(editModeAttributes);

		createModeAttributes = modifyIfNecessary(createModifier, createModeAttributes);
		bluePrint.setCreateModeAttributes(createModeAttributes);

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Collection modifyIfNecessary(final IAttributeCollectionModifierBuilder modifier, final Collection attributes) {
		if (modifier.isModified()) {
			return attributeToolkit.createAttributesCopy(attributes, modifier.build());
		}
		else {
			return attributes;
		}
	}

	protected void modifySetup(final IBeanFormBluePrint<?> bluePrint) {}

	protected void modifyAttributes(final IAttributeCollectionModifierBuilder modifier) {}

	protected void modifyEditAttributes(final IAttributeCollectionModifierBuilder modifier) {}

	protected void modifyCreateAttributes(final IAttributeCollectionModifierBuilder modifier) {}

}
