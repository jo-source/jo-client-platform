/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 21.07.2011
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
	 * @return The children of the node
	 */
	Collection<IEntityApplicationNode> getChildren();

}
