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

public interface IEntityClassBuilder {

	/**
	 * Sets the id of the class. This id must be unique for all classes in the same context.
	 * The id must not be null.
	 * 
	 * @param the id to set
	 * @return the builder
	 */
	IEntityClassBuilder setId(Object id);

	/**
	 * Sets the label of the class, never null
	 * 
	 * @param the label to set
	 * @return the builder
	 */
	IEntityClassBuilder setLabel(String label);

	/**
	 * Sets the decription, may be null
	 * 
	 * @param description The description to set
	 * @return the builder
	 */
	IEntityClassBuilder setDescription(final String description);

	/**
	 * Sets the sub classes of this class, may be empty but not null.
	 * By default, an empty List is set.
	 * 
	 * @param subClasses The sub clases to set
	 * @return the builder
	 */
	IEntityClassBuilder setSubClasses(Collection<? extends IEntityClass> subClasses);

	/**
	 * Adds a sub class to this class
	 * 
	 * @param subClass The sub class to add
	 * @return the builder
	 */
	IEntityClassBuilder addSubClass(IEntityClass subClass);

	/**
	 * Builds the entity class. This builder is a single use builder so this
	 * method could only be invoked once
	 * 
	 * @return The builded entity class
	 */
	IEntityClass build();

}
