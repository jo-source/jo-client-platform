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

package org.jowidgets.cap.common.api.entity;

import java.util.Collection;

import org.jowidgets.i18n.api.IMessage;

public interface IEntityApplicationNode {

	/**
	 * Gets the entity id of the node. If null, the node is a folder.
	 * 
	 * @return The id, may be null if node is a folder and label is not empty.
	 */
	Object getEntityId();

	/**
	 * @return The label of the class, may be empty if the entity id is not null
	 */
	IMessage getLabel();

	/**
	 * @return The description, may be empty
	 */
	IMessage getDescription();

	/**
	 * Gets an icon descriptor for the node
	 * 
	 * To make that work in the ui, there must be an icon resolver registered in the ui
	 * for that descriptor type.
	 * 
	 * @return The icon descriptor or null
	 */
	Object getIconDescriptor();

	/**
	 * @return The children of the node
	 */
	Collection<IEntityApplicationNode> getChildren();

}
