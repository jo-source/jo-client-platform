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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.cap.ui.api.table.ICsvExportParameter;

class CsvExportParameter implements ICsvExportParameter {

	private final ExportType exportType;
	private final boolean exportHeader;
	private final boolean exportInvisibleProperties;
	private final char separator;
	private final char mask;
	private final String encoding;
	private final String filename;

	CsvExportParameter() {
		this(ExportType.TABLE, true, false, ';', '*', "UTF-8", null);
	}

	CsvExportParameter(
		final ExportType exportType,
		final boolean exportHeader,
		final boolean exportInvisibleProperties,
		final char separator,
		final char mask,
		final String encoding,
		final String filename) {

		this.exportType = exportType;
		this.exportHeader = exportHeader;
		this.exportInvisibleProperties = exportInvisibleProperties;
		this.separator = separator;
		this.mask = mask;
		this.encoding = encoding;
		this.filename = filename;
	}

	@Override
	public ExportType getExportType() {
		return exportType;
	}

	@Override
	public boolean isExportHeader() {
		return exportHeader;
	}

	@Override
	public boolean isExportInvisibleProperties() {
		return exportInvisibleProperties;
	}

	@Override
	public char getSeparator() {
		return separator;
	}

	@Override
	public char getMask() {
		return mask;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String toString() {
		return "CsvExportParameter [exportType="
			+ exportType
			+ ", exportHeader="
			+ exportHeader
			+ ", exportInvisibleProperties="
			+ exportInvisibleProperties
			+ ", separator="
			+ separator
			+ ", mask="
			+ mask
			+ ", encoding="
			+ encoding
			+ ", filename="
			+ filename
			+ "]";
	}

}
