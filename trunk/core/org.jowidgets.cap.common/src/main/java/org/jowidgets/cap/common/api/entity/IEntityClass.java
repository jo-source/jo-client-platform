/*
 * Copyright (c) 2011 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original Author: grossmann
 * Creation Date: 21.07.2011
 */

package org.jowidgets.cap.common.api.entity;

import java.util.List;

public interface IEntityClass {

	/**
	 * Gets the id of the class. This id must be unique for all classes in the same context.
	 * 
	 * @return The id, never null.
	 */
	Object getId();

	/**
	 * @return The label of the class, never null
	 */
	String getLabel();

	/**
	 * @return The decription, may be null
	 */
	String getDescription();

	/**
	 * @return The linked classes of this class
	 */
	List<IEntityClass> getLinkedClasses();

	/**
	 * @return The sub classes of this class
	 */
	List<IEntityClass> getSubClasses();

}
