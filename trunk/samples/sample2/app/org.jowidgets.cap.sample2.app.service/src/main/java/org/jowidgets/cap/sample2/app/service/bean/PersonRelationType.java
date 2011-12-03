/*
 * Copyright (c) 2011, H.Westphal
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
package org.jowidgets.cap.sample2.app.service.bean;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.jowidgets.cap.sample2.app.common.bean.IPersonRelationType;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sourceName", "destinationName"}))
public class PersonRelationType extends Bean implements IPersonRelationType {

	@Basic
	private String sourceName;

	@Basic
	private String sourceDescription;

	@Basic
	private String destinationName;

	@Basic
	private String destinationDescription;

	@Override
	public String getSourceName() {
		return sourceName;
	}

	@Override
	public void setSourceName(final String sourceName) {
		this.sourceName = sourceName;
	}

	@Override
	public String getSourceDescription() {
		return sourceDescription;
	}

	@Override
	public void setSourceDescription(final String sourceDescription) {
		this.sourceDescription = sourceDescription;
	}

	@Override
	public String getDestinationName() {
		return destinationName;
	}

	@Override
	public void setDestinationName(final String destinationName) {
		this.destinationName = destinationName;
	}

	@Override
	public String getDestinationDescription() {
		return destinationDescription;
	}

	@Override
	public void setDestinationDescription(final String destinationDescription) {
		this.destinationDescription = destinationDescription;
	}

}
